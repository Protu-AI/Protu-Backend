package models

import (
	"time"

	"go.mongodb.org/mongo-driver/bson/primitive"
)

// Quiz status constants
const (
	QuizStatusDraftStage1 = "draft_stage1"
	QuizStatusDraft       = "draft"
	QuizStatusPublished   = "published"
	QuizStatusActive      = "active"
	QuizStatusCompleted   = "completed"
	QuizStatusArchived    = "archived"
)

type Quiz struct {
	ID                  primitive.ObjectID `bson:"_id,omitempty" json:"id,omitempty"`
	UserID              string             `bson:"userId" json:"userId"`
	Title               string             `bson:"title" json:"title"`
	Prompt              string             `bson:"prompt" json:"prompt"`
	Subtopics           []string           `bson:"subtopics" json:"subtopics"`
	AdditionalSubtopics []string           `bson:"additionalSubtopics" json:"additionalSubtopics"`
	DifficultyLevel     string             `bson:"difficultyLevel" json:"difficultyLevel"`
	NumberOfQuestions   int                `bson:"numberOfQuestions" json:"numberOfQuestions"`
	QuestionTypes       []string           `bson:"questionTypes" json:"questionTypes"`
	TimeLimit           int                `bson:"timeLimit" json:"timeLimit"`
	AdditionalPrefs     string             `bson:"additionalPrefs" json:"additionalPrefs"`
	Questions           []Question         `bson:"questions" json:"questions"`
	Topic               string             `bson:"topic" json:"topic"`
	Status              string             `bson:"status" json:"status"`
	AttemptCount        int                `bson:"attemptCount" json:"attemptCount"`
	CreatedAt           time.Time          `bson:"createdAt" json:"createdAt"`
	UpdatedAt           time.Time          `bson:"updatedAt" json:"updatedAt"`
	PublishedAt         *time.Time         `bson:"publishedAt,omitempty" json:"publishedAt,omitempty"`
}

type Question struct {
	ID           primitive.ObjectID `bson:"_id,omitempty" json:"id,omitempty"`
	QuestionText string             `bson:"questionText" json:"questionText"`
	QuestionType string             `bson:"questionType" json:"questionType"`
	Options      []Option           `bson:"options" json:"options"`
	Order        int                `bson:"order" json:"order"`
}

type Option struct {
	Text      string `bson:"text" json:"text"`
	IsCorrect bool   `bson:"isCorrect" json:"isCorrect"`
}
