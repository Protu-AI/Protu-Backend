package request

type CreateAttemptRequest struct {
	QuizID string `json:"quizId" binding:"required"`
}

type UpdateQuizTitleRequest struct {
	Title string `json:"title" binding:"required"`
}

type SubmitAttemptRequest struct {
	QuizID  string          `json:"quizId"`
	Answers []AnswerRequest `json:"answers" binding:"required"`
}

type AnswerRequest struct {
	QuestionID string `json:"questionId" binding:"required"`
	Selected   int    `json:"selected" binding:"required"`
}
