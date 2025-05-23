package response

import (
	"time"
)

type DashboardSummaryResponse struct {
	TotalQuizzes int     `json:"totalQuizzes"`
	AverageScore float64 `json:"averageScore"`
	SuccessRate  float64 `json:"successRate"`
}

type QuizListResponse struct {
	Quizzes    []QuizSummaryResponse `json:"quizzes"`
	Pagination PaginationMetadata    `json:"pagination,omitempty"`
}

type QuizSummaryResponse struct {
	ID        string    `json:"id"`
	Title     string    `json:"title"`
	Score     float64   `json:"score"`
	DateTaken time.Time `json:"dateTaken,omitempty"`
	Topic     string    `json:"topic"`
	TimeTaken int       `json:"timeTaken,omitempty"`
}

type PaginationMetadata struct {
	CurrentPage int `json:"currentPage"`
	PageSize    int `json:"pageSize"`
	TotalItems  int `json:"totalItems"`
	TotalPages  int `json:"totalPages"`
}
