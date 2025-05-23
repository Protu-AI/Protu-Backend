package service

import (
	"context"
	"errors"
	"time"

	"go.mongodb.org/mongo-driver/bson/primitive"
	"protu.ai/quiz-service/internal/models"
	"protu.ai/quiz-service/internal/repository"
)

var (
	ErrAttemptNotFound        = errors.New("attempt not found")
	ErrQuizNotFoundForAttempt = errors.New("quiz not found for this attempt")
	ErrCreateAttemptFailed    = errors.New("failed to create attempt")
	ErrSubmitAttemptFailed    = errors.New("failed to submit attempt")
)

type AttemptService struct {
	attemptRepo *repository.AttemptRepository
	quizRepo    *repository.QuizRepository
}

func NewAttemptService(
	attemptRepo *repository.AttemptRepository,
	quizRepo *repository.QuizRepository,
) *AttemptService {
	return &AttemptService{
		attemptRepo: attemptRepo,
		quizRepo:    quizRepo,
	}
}

func (s *AttemptService) GetAttemptsByUserID(ctx context.Context, userID string) ([]*models.QuizAttempt, error) {
	return s.attemptRepo.GetAttemptsByUserID(ctx, userID)
}

func (s *AttemptService) GetAttemptByID(ctx context.Context, id string) (*models.QuizAttempt, error) {
	attempt, err := s.attemptRepo.GetAttemptByID(ctx, id)
	if err != nil {
		if errors.Is(err, repository.ErrAttemptNotFound) {
			return nil, ErrAttemptNotFound
		}
		return nil, err
	}
	return attempt, nil
}

func (s *AttemptService) CreateAttempt(ctx context.Context, attempt *models.QuizAttempt) (*models.QuizAttempt, error) {
	if _, err := s.quizRepo.GetQuizByID(ctx, attempt.QuizID.Hex()); err != nil {
		if errors.Is(err, repository.ErrQuizNotFound) {
			return nil, ErrQuizNotFoundForAttempt
		}
		return nil, err
	}

	attempt.ID = primitive.NewObjectID()
	attempt.StartedAt = time.Now()
	attempt.Answers = []models.Answer{}
	attempt.Status = "in_progress"

	if err := s.attemptRepo.CreateAttempt(ctx, attempt); err != nil {
		return nil, ErrCreateAttemptFailed
	}

	return attempt, nil
}

func (s *AttemptService) SubmitAttempt(ctx context.Context, id string, answers []models.Answer) (*models.QuizAttempt, error) {
	attempt, err := s.attemptRepo.GetAttemptByID(ctx, id)
	if err != nil {
		if errors.Is(err, repository.ErrAttemptNotFound) {
			return nil, ErrAttemptNotFound
		}
		return nil, err
	}

	quiz, err := s.quizRepo.GetQuizByID(ctx, attempt.QuizID.Hex())
	if err != nil {
		if errors.Is(err, repository.ErrQuizNotFound) {
			return nil, ErrQuizNotFoundForAttempt
		}
		return nil, err
	}

	s.scoreAttempt(quiz, attempt, answers)

	if err := s.attemptRepo.UpdateAttempt(ctx, attempt); err != nil {
		return nil, ErrSubmitAttemptFailed
	}

	return attempt, nil
}

func (s *AttemptService) scoreAttempt(quiz *models.Quiz, attempt *models.QuizAttempt, answers []models.Answer) {
	questionMap := make(map[string]*models.Question, len(quiz.Questions))
	for i := range quiz.Questions {
		questionMap[quiz.Questions[i].ID.Hex()] = &quiz.Questions[i]
	}

	totalQuestions := len(quiz.Questions)
	correctAnswers := 0

	for i := range answers {
		answerQuestionID := answers[i].QuestionID.Hex()
		if question, exists := questionMap[answerQuestionID]; exists {
			isCorrect := evaluateAnswer(question, answers[i])
			answers[i].IsCorrect = isCorrect
			if isCorrect {
				correctAnswers++
			}
		}
	}

	var score float64 = 0
	if totalQuestions > 0 {
		score = float64(correctAnswers) / float64(totalQuestions) * 100
	}

	now := time.Now()
	attempt.CompletedAt = now
	attempt.Answers = answers
	attempt.Score = score
	attempt.Passed = score >= 60
	attempt.TimeTaken = int(now.Sub(attempt.StartedAt).Seconds())
	attempt.Status = "completed"
}

func evaluateAnswer(question *models.Question, answer models.Answer) bool {
	switch question.QuestionType {
	case "multiple_choice", "true_false":
		if selectedIndex, ok := answer.Selected.(float64); ok {
			index := int(selectedIndex)
			return index >= 0 && index < len(question.Options) && question.Options[index].IsCorrect
		} else if selectedIndex, ok := answer.Selected.(int); ok {
			return selectedIndex >= 0 && selectedIndex < len(question.Options) && question.Options[selectedIndex].IsCorrect
		} else if selectedText, ok := answer.Selected.(string); ok {
			for _, option := range question.Options {
				if option.Text == selectedText && option.IsCorrect {
					return true
				}
			}
		}
	}
	return false
}
