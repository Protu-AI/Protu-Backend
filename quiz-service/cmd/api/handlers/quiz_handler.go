package handlers

import (
	"log"

	"github.com/gin-gonic/gin"
	"go.mongodb.org/mongo-driver/mongo"
	"protu.ai/quiz-service/internal/dto/request"
	"protu.ai/quiz-service/internal/dto/response"
	"protu.ai/quiz-service/internal/middleware"
	"protu.ai/quiz-service/internal/models"
	"protu.ai/quiz-service/internal/service"
	"protu.ai/quiz-service/pkg/errors"
	apiResponse "protu.ai/quiz-service/pkg/response"
	"protu.ai/quiz-service/pkg/validator"
)

type QuizHandler struct {
	quizService *service.QuizService
	aiService   *service.AIService
}

func NewQuizHandler(quizService *service.QuizService, aiService *service.AIService) *QuizHandler {
	return &QuizHandler{
		quizService: quizService,
		aiService:   aiService,
	}
}

func (h *QuizHandler) UpdateQuizTitle(c *gin.Context) {
	quizID := c.Param("quizId")
	if quizID == "" {
		apiResponse.Error(c, errors.BadRequestError("Quiz ID is required", nil))
		return
	}

	userID, err := middleware.GetUserIDFromContext(c)
	if err != nil {
		apiResponse.Error(c, errors.AuthenticationError("Failed to get user ID from token"))
		return
	}

	var titleReq request.UpdateQuizTitleRequest
	if err := c.ShouldBindJSON(&titleReq); err != nil {
		apiResponse.Error(c, errors.ValidationError("Invalid request body", err.Error()))
		return
	}

	if err := validator.Validate(titleReq); err != nil {
		apiResponse.Error(c, errors.ValidationError("Invalid input data", err.Error()))
		return
	}

	quiz, err := h.quizService.GetQuizByID(c, quizID)
	if err != nil {
		apiResponse.Error(c, errors.NotFoundError("Quiz not found"))
		return
	}

	if quiz.UserID != userID {
		apiResponse.Error(c, errors.AuthorizationError("You are not authorized to update this quiz"))
		return
	}

	quiz.Title = titleReq.Title
	updatedQuiz, err := h.quizService.UpdateQuiz(c, quizID, quiz)
	if err != nil {
		apiResponse.Error(c, errors.QuizCreationError("Failed to update quiz title", err.Error()))
		return
	}

	titleResponse := response.QuizTitleResponse{
		ID:    updatedQuiz.ID.Hex(),
		Title: updatedQuiz.Title,
	}

	apiResponse.OK(c, "Quiz title updated successfully", titleResponse)
}

func (h *QuizHandler) CreateQuizStage1(c *gin.Context) {
	var stage1Req request.QuizStage1Request

	if err := c.ShouldBindJSON(&stage1Req); err != nil {
		apiResponse.Error(c, errors.ValidationError("Invalid request body", err.Error()))
		return
	}

	if err := validator.Validate(stage1Req); err != nil {
		apiResponse.Error(c, errors.ValidationError("Invalid input data", err.Error()))
		return
	}

	userID, err := middleware.GetUserIDFromContext(c)
	if err != nil {
		apiResponse.Error(c, errors.AuthenticationError("Failed to get user ID from token"))
		return
	}

	quiz := &models.Quiz{
		UserID:            userID,
		Prompt:            stage1Req.Prompt,
		DifficultyLevel:   stage1Req.DifficultyLevel,
		NumberOfQuestions: stage1Req.NumberOfQuestions,
		QuestionTypes:     stage1Req.QuestionTypes,
		TimeLimit:         stage1Req.TimeLimit,
		Status:            "draft_stage1",
	}

	createdQuiz, err := h.quizService.CreateQuiz(c, quiz)
	if err != nil {
		apiResponse.Error(c, errors.QuizCreationError("Failed to create quiz", err.Error()))
		return
	}

	questionType := "multiple_choice"
	if len(createdQuiz.QuestionTypes) > 0 {
		questionType = createdQuiz.QuestionTypes[0]
	}

	subtopicSuggestions, err := h.aiService.GetSubtopicSuggestions(
		c,
		createdQuiz.Prompt,
		createdQuiz.DifficultyLevel,
		questionType,
		createdQuiz.TimeLimit,
	)

	if err != nil {
		if err == service.ErrInvalidPrompt {
			apiResponse.Error(c, errors.BadRequestError("The provided prompt is not relevant for a quiz", nil))
			return
		}
		apiResponse.Error(c, errors.InternalError("Failed to generate subtopic suggestions: "+err.Error()))
		return
	}

	stage1Response := response.QuizStage1Response{
		ID:                  createdQuiz.ID.Hex(),
		Prompt:              createdQuiz.Prompt,
		DifficultyLevel:     createdQuiz.DifficultyLevel,
		NumberOfQuestions:   createdQuiz.NumberOfQuestions,
		QuestionTypes:       createdQuiz.QuestionTypes,
		TimeLimit:           createdQuiz.TimeLimit,
		SubtopicSuggestions: subtopicSuggestions,
		CreatedAt:           createdQuiz.CreatedAt,
	}

	apiResponse.QuizCreated(c, stage1Response)
}

func (h *QuizHandler) CompleteQuizStage2(c *gin.Context) {
	var stage2Req request.QuizStage2Request

	if err := c.ShouldBindJSON(&stage2Req); err != nil {
		apiResponse.Error(c, errors.ValidationError("Invalid request body", err.Error()))
		return
	}

	if err := validator.Validate(stage2Req); err != nil {
		apiResponse.Error(c, errors.ValidationError("Invalid input data", err.Error()))
		return
	}

	quiz, err := h.quizService.GetQuizByID(c, stage2Req.QuizID)
	if err != nil {
		if err == mongo.ErrNoDocuments {
			apiResponse.Error(c, errors.NotFoundError("Quiz not found"))
			return
		}
		apiResponse.Error(c, errors.InternalError("Failed to retrieve quiz: "+err.Error()))
		return
	}

	if quiz.Status != "draft_stage1" {
		apiResponse.Error(c, errors.BadRequestError("Quiz is not in stage 1 draft status", nil))
		return
	}

	quiz.Subtopics = stage2Req.Subtopics
	quiz.AdditionalSubtopics = stage2Req.AdditionalSubtopics
	quiz.AdditionalPrefs = stage2Req.AdditionalPrefs

	questionType := "multiple_choice"
	if len(quiz.QuestionTypes) > 0 {
		questionType = quiz.QuestionTypes[0]
	}

	log.Printf("Calling AI service to generate questions for quiz ID: %s", stage2Req.QuizID)
	quizResult, err := h.aiService.GenerateQuizQuestions(
		c,
		quiz.Prompt,
		quiz.DifficultyLevel,
		questionType,
		quiz.TimeLimit,
		quiz.Subtopics,
		quiz.AdditionalSubtopics,
		quiz.AdditionalPrefs,
	)

	if err != nil {
		apiResponse.Error(c, errors.InternalError("Failed to generate quiz questions: "+err.Error()))
		return
	}

	quiz.Title = quizResult.Title
	quiz.Topic = quizResult.Topic
	quiz.Questions = quizResult.Questions
	quiz.Status = "draft"

	updatedQuiz, err := h.quizService.UpdateQuiz(c, stage2Req.QuizID, quiz)
	if err != nil {
		apiResponse.Error(c, errors.QuizCreationError("Failed to update quiz with generated questions", err.Error()))
		return
	}

	var publicQuestions []response.PublicQuestionDetail
	for _, q := range updatedQuiz.Questions {
		var publicOptions []response.PublicOption
		for _, opt := range q.Options {
			publicOptions = append(publicOptions, response.PublicOption{
				Text: opt.Text,
			})
		}

		publicQuestions = append(publicQuestions, response.PublicQuestionDetail{
			ID:           q.ID.Hex(),
			QuestionText: q.QuestionText,
			QuestionType: q.QuestionType,
			Options:      publicOptions,
			CodeSnippet:  "",
			Order:        q.Order,
		})
	}

	stage2Response := response.QuizStage2Response{
		ID:                  updatedQuiz.ID.Hex(),
		Title:               updatedQuiz.Title,
		Topic:               updatedQuiz.Topic,
		Prompt:              updatedQuiz.Prompt,
		Subtopics:           updatedQuiz.Subtopics,
		AdditionalSubtopics: updatedQuiz.AdditionalSubtopics,
		DifficultyLevel:     updatedQuiz.DifficultyLevel,
		NumberOfQuestions:   updatedQuiz.NumberOfQuestions,
		QuestionTypes:       updatedQuiz.QuestionTypes,
		TimeLimit:           updatedQuiz.TimeLimit,
		AdditionalPrefs:     updatedQuiz.AdditionalPrefs,
		Status:              updatedQuiz.Status,
		Questions:           publicQuestions,
		CreatedAt:           updatedQuiz.CreatedAt,
		UpdatedAt:           updatedQuiz.UpdatedAt,
	}

	apiResponse.QuizStageCompleted(c, 2, stage2Response)
}
