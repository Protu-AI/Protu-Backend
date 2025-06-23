package models

import (
	"time"

	"go.mongodb.org/mongo-driver/bson/primitive"
)

const (
	AttemptStatusInProgress = "in_progress"
	AttemptStatusCompleted  = "completed"
	AttemptStatusAbandoned  = "abandoned"
)

type QuizAttempt struct {
	ID          primitive.ObjectID `bson:"_id,omitempty" json:"id,omitempty"`
	QuizID      primitive.ObjectID `bson:"quizId" json:"quizId"`
	UserID      string             `bson:"userId" json:"userId"`
	StartedAt   time.Time          `bson:"startedAt" json:"startedAt"`
	CompletedAt time.Time          `bson:"completedAt,omitempty" json:"completedAt,omitempty"`
	Answers     []Answer           `bson:"answers" json:"answers"`
	Score       float64            `bson:"score" json:"score"`
	Passed      bool               `bson:"passed" json:"passed"`
	TimeTaken   int                `bson:"timeTaken" json:"timeTaken"`
	Status      string             `bson:"status" json:"status"`
}

type Answer struct {
	QuestionID primitive.ObjectID `bson:"questionId" json:"questionId"`
	Selected   interface{}        `bson:"selected" json:"selected"`
	IsCorrect  bool               `bson:"isCorrect" json:"isCorrect"`
}
