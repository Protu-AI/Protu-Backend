package models

import (
	"time"

	"go.mongodb.org/mongo-driver/bson/primitive"
)

type UserStats struct {
	ID               primitive.ObjectID `bson:"_id,omitempty" json:"id,omitempty"`
	UserID           string             `bson:"userId" json:"userId"`
	TotalQuizzes     int                `bson:"totalQuizzes" json:"totalQuizzes"`
	CompletedQuizzes int                `bson:"completedQuizzes" json:"completedQuizzes"`
	AverageScore     float64            `bson:"averageScore" json:"averageScore"`
	SuccessRate      float64            `bson:"successRate" json:"successRate"`
	QuizzesByTopic   map[string]int     `bson:"quizzesByTopic" json:"quizzesByTopic"`
	LastUpdated      time.Time          `bson:"lastUpdated" json:"lastUpdated"`
}
