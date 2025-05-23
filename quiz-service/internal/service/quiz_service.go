package service

import (
	"context"
	"errors"

	"protu.ai/quiz-service/internal/models"
	"protu.ai/quiz-service/internal/repository"
)

var (
	ErrQuizNotFound       = errors.New("quiz not found")
	ErrInvalidQuizData    = errors.New("invalid quiz data")
	ErrUpdateFailed       = errors.New("failed to update quiz")
	ErrQuizCreationFailed = errors.New("failed to create quiz")
)

type QuizService struct {
	quizRepo *repository.QuizRepository
}

func NewQuizService(quizRepo *repository.QuizRepository) *QuizService {
	return &QuizService{quizRepo: quizRepo}
}

func (s *QuizService) GetQuizByID(ctx context.Context, id string) (*models.Quiz, error) {
	quiz, err := s.quizRepo.GetQuizByID(ctx, id)
	if err != nil {
		if errors.Is(err, repository.ErrQuizNotFound) {
			return nil, ErrQuizNotFound
		}
		return nil, err
	}
	return quiz, nil
}

func (s *QuizService) CreateQuiz(ctx context.Context, quiz *models.Quiz) (*models.Quiz, error) {
	if err := s.validateQuiz(quiz); err != nil {
		return nil, err
	}

	if quiz.Status == "" {
		quiz.Status = "draft"
	}

	if err := s.quizRepo.CreateQuiz(ctx, quiz); err != nil {
		return nil, ErrQuizCreationFailed
	}

	return quiz, nil
}

func (s *QuizService) UpdateQuiz(ctx context.Context, id string, updates *models.Quiz) (*models.Quiz, error) {
	existingQuiz, err := s.quizRepo.GetQuizByID(ctx, id)
	if err != nil {
		if errors.Is(err, repository.ErrQuizNotFound) {
			return nil, ErrQuizNotFound
		}
		return nil, err
	}

	updateFields(existingQuiz, updates)

	if err := s.validateQuiz(existingQuiz); err != nil {
		return nil, err
	}

	if err := s.quizRepo.UpdateQuiz(ctx, existingQuiz); err != nil {
		return nil, ErrUpdateFailed
	}

	return existingQuiz, nil
}

func updateFields(existingQuiz *models.Quiz, updates *models.Quiz) {
	if updates.Title != "" {
		existingQuiz.Title = updates.Title
	}
	if updates.Prompt != "" {
		existingQuiz.Prompt = updates.Prompt
	}
	if updates.Subtopics != nil {
		existingQuiz.Subtopics = updates.Subtopics
	}
	if updates.AdditionalSubtopics != nil {
		existingQuiz.AdditionalSubtopics = updates.AdditionalSubtopics
	}
	if updates.DifficultyLevel != "" {
		existingQuiz.DifficultyLevel = updates.DifficultyLevel
	}
	if updates.NumberOfQuestions != 0 {
		existingQuiz.NumberOfQuestions = updates.NumberOfQuestions
	}
	if updates.QuestionTypes != nil {
		existingQuiz.QuestionTypes = updates.QuestionTypes
	}
	if updates.TimeLimit != 0 {
		existingQuiz.TimeLimit = updates.TimeLimit
	}
	if updates.AdditionalPrefs != "" {
		existingQuiz.AdditionalPrefs = updates.AdditionalPrefs
	}
	if updates.Topic != "" {
		existingQuiz.Topic = updates.Topic
	}
	if updates.Status != "" {
		existingQuiz.Status = updates.Status
	}
	if updates.Questions != nil {
		existingQuiz.Questions = updates.Questions
	}
}

func (s *QuizService) validateQuiz(quiz *models.Quiz) error {
	// Title is only required for published quizzes and complete drafts, not for stage 1 drafts
	if quiz.Title == "" && quiz.Status != "draft_stage1" {
		return errors.New("quiz title is required")
	}
	if quiz.UserID == "" {
		return errors.New("user ID is required")
	}

	validDifficulties := map[string]bool{
		"easy":   true,
		"medium": true,
		"hard":   true,
	}

	if quiz.DifficultyLevel != "" && !validDifficulties[quiz.DifficultyLevel] {
		return errors.New("invalid difficulty level: must be easy, medium, or hard")
	}
	if quiz.NumberOfQuestions < 0 {
		return errors.New("number of questions cannot be negative")
	}
	if quiz.TimeLimit < 0 {
		return errors.New("time limit cannot be negative")
	}

	return nil
}
