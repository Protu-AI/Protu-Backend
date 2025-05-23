package response

import (
	"time"
)

type QuizPreviewResponse struct {
	ID                string    `json:"id"`
	Title             string    `json:"title"`
	Topic             string    `json:"topic"`
	DifficultyLevel   string    `json:"difficultyLevel"`
	NumberOfQuestions int       `json:"numberOfQuestions"`
	TimeLimit         int       `json:"timeLimit"`
	CreatedAt         time.Time `json:"createdAt"`
}

type QuizStartResponse struct {
	AttemptID string           `json:"attemptId"`
	QuizID    string           `json:"quizId"`
	Title     string           `json:"title"`
	TimeLimit int              `json:"timeLimit"`
	Questions []QuestionDetail `json:"questions"`
	StartedAt time.Time        `json:"startedAt"`
}

type QuizReviewResponse struct {
	AttemptID   string    `json:"attemptId"`
	QuizID      string    `json:"quizId"`
	QuizTitle   string    `json:"quizTitle"`
	QuizTopic   string    `json:"quizTopic"`
	Score       float64   `json:"score"`
	Passed      bool      `json:"passed"`
	TimeTaken   int       `json:"timeTaken"`
	CompletedAt time.Time `json:"completedAt"`
	AIFeedback  string    `json:"aiFeedback,omitempty"`
}
