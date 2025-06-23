package service

import (
	"bytes"
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"log"
	"net/http"
	"strings"
	"time"

	"go.mongodb.org/mongo-driver/bson/primitive"
	"protu.ai/quiz-service/config"
	"protu.ai/quiz-service/internal/dto/response"
	"protu.ai/quiz-service/internal/models"
)

type AIService struct {
	config               *config.Config
	client               *http.Client
	subtopicsGenEndpoint string
	quizGenEndpoint      string
	feedbackEndpoint     string
}

type TagsRequest struct {
	Prompt       string `json:"prompt"`
	Difficulty   string `json:"difficulty"`
	QuestionType string `json:"question_type"`
	Time         int    `json:"time"`
	NumberOfTags int    `json:"number_of_tags,omitempty"`
}

type TagsResponse struct {
	Signal     string   `json:"signal"`
	Tags       []string `json:"tags"`
	Message    string   `json:"message"`
	IsRelevant bool     `json:"is_relevant"`
}

type QuizGenerationRequest struct {
	Prompt                string   `json:"prompt"`
	Difficulty            string   `json:"difficulty"`
	QuestionType          string   `json:"question_type"`
	Time                  int      `json:"time"`
	NumberOfQuestions     int      `json:"number_of_questions"`
	Tags                  []string `json:"tags"`
	AdditionalTags        []string `json:"additional_tags,omitempty"`
	AdditionalPreferences string   `json:"additional_preferences,omitempty"`
}

type QuizGenerationResponse struct {
	Signal string          `json:"signal"`
	Quiz   AIGeneratedQuiz `json:"quiz"`
}

type AIGeneratedQuiz struct {
	QuizTitle string                `json:"quiz_title"`
	Topic     string                `json:"topic"`
	Questions []AIGeneratedQuestion `json:"questions"`
}

type AIGeneratedQuestion struct {
	Question          string   `json:"question"`
	Options           []string `json:"options"`
	CorrectAnswerText string   `json:"correct_answer_text"`
}

// QuizFeedbackRequest represents a single question for AI feedback
type QuizQuestionFeedback struct {
	Question      string   `json:"question"`
	Options       []string `json:"options"`
	CorrectAnswer string   `json:"correct_answer"`
	UserAnswer    string   `json:"user_answer"`
}

// QuizFeedbackRequest is the request body for getting AI feedback
type QuizFeedbackRequest struct {
	Quiz []QuizQuestionFeedback `json:"quiz"`
}

// QuizFeedbackResponse is the AI's response to a feedback request
type QuizFeedbackResponse struct {
	Signal   string `json:"signal"`
	Feedback string `json:"feedback"`
}

// QuizGenerationResult holds the complete result from AI quiz generation
type QuizGenerationResult struct {
	Title     string
	Topic     string
	Questions []models.Question
}

var (
	ErrAIServiceUnavailable     = errors.New("AI service is unavailable")
	ErrInvalidPrompt            = errors.New("invalid prompt")
	ErrQuizGenerationFailed     = errors.New("failed to generate quiz questions")
	ErrFeedbackGenerationFailed = errors.New("failed to generate quiz feedback")
)

func NewAIService(config *config.Config) *AIService {
	return &AIService{
		config: config,
		client: &http.Client{
			Timeout: 30 * time.Second,
		},
		subtopicsGenEndpoint: fmt.Sprintf("%s/protu/ai/data/quiz-tags", config.AIBaseURL),
		quizGenEndpoint:      fmt.Sprintf("%s/protu/ai/data/quiz-generation", config.AIBaseURL),
		feedbackEndpoint:     fmt.Sprintf("%s/protu/ai/data/quiz-feedback", config.AIBaseURL),
	}
}

// GetSubtopicSuggestions calls the AI service to get subtopic suggestions
func (s *AIService) GetSubtopicSuggestions(ctx context.Context, prompt, difficulty string, questionType string, timeLimit int) ([]response.SubtopicSuggestion, error) {
	aiQuestionType := mapQuestionType(questionType)
	aiDifficulty := mapDifficulty(difficulty)

	reqBody := TagsRequest{
		Prompt:       prompt,
		Difficulty:   aiDifficulty,
		QuestionType: aiQuestionType,
		Time:         timeLimit,
	}

	jsonBody, err := json.Marshal(reqBody)
	if err != nil {
		log.Printf("[AIService] Failed to marshal request body: %v", err)
		return nil, err
	}

	log.Printf("[AIService] Sending request to AI endpoint %s with body: %s", s.subtopicsGenEndpoint, string(jsonBody))

	req, err := http.NewRequestWithContext(ctx, "POST", s.subtopicsGenEndpoint, bytes.NewBuffer(jsonBody))
	if err != nil {
		log.Printf("[AIService] Failed to create HTTP request: %v", err)
		return nil, err
	}
	req.Header.Set("Content-Type", "application/json")

	resp, err := s.client.Do(req)
	if err != nil {
		log.Printf("[AIService] HTTP request failed: %v", err)
		return nil, ErrAIServiceUnavailable
	}
	defer resp.Body.Close()

	log.Printf("[AIService] Received response with status code: %d", resp.StatusCode)

	if resp.StatusCode != http.StatusOK {
		log.Printf("[AIService] Non-OK status code: %d", resp.StatusCode)
		return nil, fmt.Errorf("AI service returned status code %d", resp.StatusCode)
	}

	var aiResp TagsResponse
	if err := json.NewDecoder(resp.Body).Decode(&aiResp); err != nil {
		log.Printf("[AIService] Failed to decode response: %v", err)
		return nil, err
	}

	log.Printf("[AIService] AI response: %+v", aiResp)

	if !aiResp.IsRelevant {
		log.Printf("[AIService] AI response marked prompt as not relevant")
		return nil, ErrInvalidPrompt
	}

	var suggestions []response.SubtopicSuggestion
	for i, tag := range aiResp.Tags {
		suggestions = append(suggestions, response.SubtopicSuggestion{
			ID:   fmt.Sprintf("%d", i+1),
			Text: tag,
		})
	}

	log.Printf("[AIService] Returning %d subtopic suggestions", len(suggestions))
	return suggestions, nil
}

// GenerateQuizQuestions calls the AI service to generate quiz questions along with title and topic
func (s *AIService) GenerateQuizQuestions(ctx context.Context, prompt, difficulty string, questionType string,
	timeLimit int, numberOfQuestions int, subtopics []string, additionalSubtopics []string, additionalPrefs string) (*QuizGenerationResult, error) {

	aiQuestionType := mapQuestionType(questionType)
	aiDifficulty := mapDifficulty(difficulty)

	reqBody := QuizGenerationRequest{
		Prompt:                prompt,
		Difficulty:            aiDifficulty,
		QuestionType:          aiQuestionType,
		Time:                  timeLimit,
		NumberOfQuestions:     numberOfQuestions,
		Tags:                  subtopics,
		AdditionalTags:        additionalSubtopics,
		AdditionalPreferences: additionalPrefs,
	}

	jsonBody, err := json.Marshal(reqBody)
	if err != nil {
		log.Printf("[AIService] Failed to marshal quiz generation request: %v", err)
		return nil, err
	}

	log.Printf("[AIService] Sending quiz generation request to AI endpoint %s", s.quizGenEndpoint)
	log.Printf("[AIService] Request details - numberOfQuestions: %d, questionType: %s, difficulty: %s", numberOfQuestions, aiQuestionType, aiDifficulty)
	log.Printf("[AIService] Request body: %s", string(jsonBody))

	req, err := http.NewRequestWithContext(ctx, "POST", s.quizGenEndpoint, bytes.NewBuffer(jsonBody))
	if err != nil {
		log.Printf("[AIService] Failed to create HTTP request for quiz generation: %v", err)
		return nil, err
	}
	req.Header.Set("Content-Type", "application/json")

	resp, err := s.client.Do(req)
	if err != nil {
		log.Printf("[AIService] Quiz generation HTTP request failed: %v", err)
		return nil, ErrAIServiceUnavailable
	}
	defer resp.Body.Close()

	log.Printf("[AIService] Received quiz generation response with status code: %d", resp.StatusCode)

	if resp.StatusCode != http.StatusOK {
		log.Printf("[AIService] Non-OK status code for quiz generation: %d", resp.StatusCode)
		return nil, fmt.Errorf("AI service returned status code %d", resp.StatusCode)
	}

	var aiResp QuizGenerationResponse
	if err := json.NewDecoder(resp.Body).Decode(&aiResp); err != nil {
		log.Printf("[AIService] Failed to decode quiz generation response: %v", err)
		return nil, err
	}

	log.Printf("[AIService] Quiz generation successful, received %d questions (requested %d)", len(aiResp.Quiz.Questions), numberOfQuestions)

	// Limit the number of questions to what was requested
	questionsToProcess := aiResp.Quiz.Questions
	if len(questionsToProcess) > numberOfQuestions {
		log.Printf("[AIService] Limiting questions from %d to %d as requested", len(questionsToProcess), numberOfQuestions)
		questionsToProcess = questionsToProcess[:numberOfQuestions]
	}

	// Convert AI generated questions to our model
	questions := make([]models.Question, 0, len(questionsToProcess))
	for i, q := range questionsToProcess {
		// Detect actual question type based on the question structure
		detectedType := detectQuestionType(q)

		question := models.Question{
			ID:           primitive.NewObjectID(),
			QuestionText: q.Question,
			QuestionType: detectedType,
			Order:        i + 1,
			Options:      make([]models.Option, 0, len(q.Options)),
		}

		// Add options and mark the correct one
		for _, optText := range q.Options {
			isCorrect := optText == q.CorrectAnswerText
			question.Options = append(question.Options, models.Option{
				Text:      optText,
				IsCorrect: isCorrect,
			})
		}

		questions = append(questions, question)
	}

	log.Printf("[AIService] Converted AI questions to %d model questions", len(questions))

	return &QuizGenerationResult{
		Title:     aiResp.Quiz.QuizTitle,
		Topic:     aiResp.Quiz.Topic,
		Questions: questions,
	}, nil
}

// mapQuestionType converts our internal question types to the AI service format
func mapQuestionType(questionType string) string {
	switch questionType {
	case "multiple_choice":
		return "MCQ"
	case "true_false":
		return "True/False"
	case "mixed":
		return "Combination between both"
	default:
		return "MCQ" // Default to MCQ
	}
}

// aiQuestionTypeToModelType converts AI question type back to our model's format
func aiQuestionTypeToModelType(aiType string) string {
	switch aiType {
	case "MCQ":
		return "multiple_choice"
	case "True/False":
		return "true_false"
	case "Combination between both":
		return "mixed"
	default:
		return "multiple_choice"
	}
}

// mapDifficulty converts our internal difficulty levels to the AI service format
func mapDifficulty(difficulty string) string {
	switch difficulty {
	case "easy":
		return "beginner"
	case "medium":
		return "intermediate"
	case "hard":
		return "advanced"
	default:
		return "intermediate" // Default to intermediate
	}
}

// detectQuestionType analyzes the question structure to determine its type
func detectQuestionType(q AIGeneratedQuestion) string {
	// Check if it's a True/False question
	if len(q.Options) == 2 {
		option1 := strings.ToLower(strings.TrimSpace(q.Options[0]))
		option2 := strings.ToLower(strings.TrimSpace(q.Options[1]))

		// Check for True/False patterns
		if (option1 == "true" && option2 == "false") || (option1 == "false" && option2 == "true") {
			return "true_false"
		}

		// Check for Yes/No patterns
		if (option1 == "yes" && option2 == "no") || (option1 == "no" && option2 == "yes") {
			return "true_false"
		}
	}

	// Default to multiple choice
	return "multiple_choice"
}

// GetQuizFeedback calls the AI service to get personalized feedback on quiz results
func (s *AIService) GetQuizFeedback(ctx context.Context, quiz *models.Quiz, userAnswers []models.Answer) (string, error) {
	// Create the quiz feedback request
	feedbackRequest := QuizFeedbackRequest{
		Quiz: make([]QuizQuestionFeedback, 0, len(userAnswers)),
	}

	// Map of question IDs to questions for faster lookup
	questionMap := make(map[string]models.Question)
	for _, q := range quiz.Questions {
		questionMap[q.ID.Hex()] = q
	}

	// Create feedback items for each answer
	for _, answer := range userAnswers {
		question, exists := questionMap[answer.QuestionID.Hex()]
		if !exists {
			continue
		}

		// Get the text of the selected option
		var selectedOptionText string
		var correctOptionText string

		// Handle the Selected field which can be either int or string
		switch selected := answer.Selected.(type) {
		case int:
			if selected >= 0 && selected < len(question.Options) {
				selectedOptionText = question.Options[selected].Text
			}
		case float64:
			// Convert float to int (MongoDB might store numbers as float64)
			selectedIndex := int(selected)
			if selectedIndex >= 0 && selectedIndex < len(question.Options) {
				selectedOptionText = question.Options[selectedIndex].Text
			}
		case string:
			// If the selected value is a string (option text)
			selectedOptionText = selected
		}

		// Find the correct option
		for _, opt := range question.Options {
			if opt.IsCorrect {
				correctOptionText = opt.Text
				break
			}
		}

		// Build list of option texts
		options := make([]string, 0, len(question.Options))
		for _, opt := range question.Options {
			options = append(options, opt.Text)
		}

		feedbackRequest.Quiz = append(feedbackRequest.Quiz, QuizQuestionFeedback{
			Question:      question.QuestionText,
			Options:       options,
			CorrectAnswer: correctOptionText,
			UserAnswer:    selectedOptionText,
		})
	}

	// Don't send request if there are no questions to get feedback on
	if len(feedbackRequest.Quiz) == 0 {
		return "No valid answers to provide feedback on.", nil
	}

	jsonBody, err := json.Marshal(feedbackRequest)
	if err != nil {
		log.Printf("[AIService] Failed to marshal quiz feedback request: %v", err)
		return "", err
	}

	log.Printf("[AIService] Sending quiz feedback request to AI endpoint %s with %d questions",
		s.feedbackEndpoint, len(feedbackRequest.Quiz))

	req, err := http.NewRequestWithContext(ctx, "POST", s.feedbackEndpoint, bytes.NewBuffer(jsonBody))
	if err != nil {
		log.Printf("[AIService] Failed to create HTTP request for quiz feedback: %v", err)
		return "", err
	}
	req.Header.Set("Content-Type", "application/json")

	resp, err := s.client.Do(req)
	if err != nil {
		log.Printf("[AIService] Quiz feedback HTTP request failed: %v", err)
		return "", ErrAIServiceUnavailable
	}
	defer resp.Body.Close()

	log.Printf("[AIService] Received quiz feedback response with status code: %d", resp.StatusCode)

	if resp.StatusCode != http.StatusOK {
		log.Printf("[AIService] Non-OK status code for quiz feedback: %d", resp.StatusCode)
		return "", fmt.Errorf("AI service returned status code %d", resp.StatusCode)
	}

	var aiResp QuizFeedbackResponse
	if err := json.NewDecoder(resp.Body).Decode(&aiResp); err != nil {
		log.Printf("[AIService] Failed to decode quiz feedback response: %v", err)
		return "", err
	}

	log.Printf("[AIService] Successfully received quiz feedback")
	return aiResp.Feedback, nil
}
