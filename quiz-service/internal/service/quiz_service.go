package service

import (
	"context"
	"errors"
	"time"

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
	quizRepo    *repository.QuizRepository
	attemptRepo *repository.AttemptRepository
}

func NewQuizService(quizRepo *repository.QuizRepository) *QuizService {
	return &QuizService{
		quizRepo: quizRepo,
	}
}

func (s *QuizService) SetAttemptRepo(attemptRepo *repository.AttemptRepository) {
	s.attemptRepo = attemptRepo
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
		quiz.Status = models.QuizStatusDraft
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

func (s *QuizService) ValidateStatusTransition(currentStatus, newStatus string) error {
	validTransitions := map[string][]string{
		models.QuizStatusDraftStage1: {models.QuizStatusActive},
		models.QuizStatusDraft:       {models.QuizStatusPublished, models.QuizStatusActive},
		models.QuizStatusPublished:   {models.QuizStatusActive, models.QuizStatusArchived},
		models.QuizStatusActive:      {models.QuizStatusCompleted, models.QuizStatusArchived},
		models.QuizStatusCompleted:   {models.QuizStatusArchived},
		models.QuizStatusArchived:    {},
	}

	allowedTransitions, exists := validTransitions[currentStatus]
	if !exists {
		return errors.New("invalid current status")
	}

	for _, allowed := range allowedTransitions {
		if allowed == newStatus {
			return nil
		}
	}

	return errors.New("invalid status transition from " + currentStatus + " to " + newStatus)
}

func (s *QuizService) TransitionQuizStatus(ctx context.Context, quizID string, newStatus string) (*models.Quiz, error) {
	quiz, err := s.quizRepo.GetQuizByID(ctx, quizID)
	if err != nil {
		return nil, err
	}

	if err := s.ValidateStatusTransition(quiz.Status, newStatus); err != nil {
		return nil, err
	}

	if err := s.applyBusinessRules(ctx, quiz, newStatus); err != nil {
		return nil, err
	}

	s.updateQuizForStatusTransition(quiz, newStatus)

	if err := s.quizRepo.UpdateQuiz(ctx, quiz); err != nil {
		return nil, err
	}

	return quiz, nil
}

func (s *QuizService) PublishQuiz(ctx context.Context, quizID string) (*models.Quiz, error) {
	return s.TransitionQuizStatus(ctx, quizID, models.QuizStatusActive)
}

func (s *QuizService) MarkQuizAsCompleted(ctx context.Context, quizID string) (*models.Quiz, error) {
	quiz, err := s.quizRepo.GetQuizByID(ctx, quizID)
	if err != nil {
		return nil, err
	}

	if quiz.Status != models.QuizStatusActive {
		return quiz, nil
	}

	return s.TransitionQuizStatus(ctx, quizID, models.QuizStatusCompleted)
}

func (s *QuizService) IncrementAttemptCount(ctx context.Context, quizID string) error {
	return s.quizRepo.IncrementAttemptCount(ctx, quizID)
}

func (s *QuizService) applyBusinessRules(ctx context.Context, quiz *models.Quiz, newStatus string) error {
	switch newStatus {
	case models.QuizStatusPublished, models.QuizStatusActive:
		if quiz.Title == "" {
			return errors.New("quiz title is required for publication")
		}
		if len(quiz.Questions) == 0 {
			return errors.New("quiz must have questions to be published")
		}
		if quiz.TimeLimit <= 0 {
			return errors.New("quiz must have a valid time limit")
		}
	}
	return nil
}

func (s *QuizService) updateQuizForStatusTransition(quiz *models.Quiz, newStatus string) {
	now := time.Now()
	quiz.Status = newStatus
	quiz.UpdatedAt = now

	switch newStatus {
	case models.QuizStatusPublished, models.QuizStatusActive:
		if quiz.PublishedAt == nil {
			quiz.PublishedAt = &now
		}
	}
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
	if quiz.Title == "" && quiz.Status != models.QuizStatusDraftStage1 {
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

func (s *QuizService) DeleteQuiz(ctx context.Context, quizID string, userID string) error {
	quiz, err := s.quizRepo.GetQuizByID(ctx, quizID)
	if err != nil {
		if errors.Is(err, repository.ErrQuizNotFound) {
			return ErrQuizNotFound
		}
		return err
	}

	if quiz.UserID != userID {
		return errors.New("you are not authorized to delete this quiz")
	}

	if quiz.Status != models.QuizStatusDraftStage1 && quiz.Status != models.QuizStatusDraft {
		return errors.New("only draft quizzes can be deleted")
	}

	if s.attemptRepo != nil {
		attempts, err := s.attemptRepo.GetAttemptsByQuizID(ctx, quizID)
		if err != nil {
			return err
		}
		if len(attempts) > 0 {
			return errors.New("cannot delete quiz that has attempts")
		}
	}

	return s.quizRepo.DeleteQuiz(ctx, quizID)
}
