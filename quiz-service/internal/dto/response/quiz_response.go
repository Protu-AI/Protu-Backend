package response

import (
	"time"
)

type QuizTitleResponse struct {
	ID    string `json:"id"`
	Title string `json:"title"`
}

type QuestionDetail struct {
	ID           string   `json:"id"`
	QuestionText string   `json:"questionText"`
	QuestionType string   `json:"questionType"`
	Options      []Option `json:"options"`
	CodeSnippet  string   `json:"codeSnippet,omitempty"`
	Order        int      `json:"order"`
}

// Option type used internally that includes the isCorrect flag
type Option struct {
	Text      string `json:"text"`
	IsCorrect bool   `json:"isCorrect,omitempty"`
}

// PublicOption is used for client responses without exposing correct answers
type PublicOption struct {
	Text string `json:"text"`
}

// PublicQuestionDetail is used for client responses without exposing correct answers
type PublicQuestionDetail struct {
	ID           string         `json:"id"`
	QuestionText string         `json:"questionText"`
	QuestionType string         `json:"questionType"`
	Options      []PublicOption `json:"options"`
	CodeSnippet  string         `json:"codeSnippet,omitempty"`
	Order        int            `json:"order"`
}

type QuizStage1Response struct {
	ID                  string               `json:"id"`
	Prompt              string               `json:"prompt"`
	DifficultyLevel     string               `json:"difficultyLevel"`
	NumberOfQuestions   int                  `json:"numberOfQuestions"`
	QuestionTypes       []string             `json:"questionTypes"`
	TimeLimit           int                  `json:"timeLimit"`
	SubtopicSuggestions []SubtopicSuggestion `json:"subtopicSuggestions"`
	CreatedAt           time.Time            `json:"createdAt"`
}

type SubtopicSuggestion struct {
	ID   string `json:"id"`
	Text string `json:"text"`
}

type QuizStage2Response struct {
	ID                  string                 `json:"id"`
	Title               string                 `json:"title"`
	Topic               string                 `json:"topic"`
	Prompt              string                 `json:"prompt"`
	Subtopics           []string               `json:"subtopics"`
	AdditionalSubtopics []string               `json:"additional_subtopics"`
	DifficultyLevel     string                 `json:"difficultyLevel"`
	NumberOfQuestions   int                    `json:"numberOfQuestions"`
	QuestionTypes       []string               `json:"questionTypes"`
	TimeLimit           int                    `json:"timeLimit"`
	AdditionalPrefs     string                 `json:"additionalPrefs"`
	Status              string                 `json:"status"`
	Questions           []PublicQuestionDetail `json:"questions"`
	CreatedAt           time.Time              `json:"createdAt"`
	UpdatedAt           time.Time              `json:"updatedAt"`
}
