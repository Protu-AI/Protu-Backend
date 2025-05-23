package response

import (
	"fmt"
	"net/http"

	"github.com/gin-gonic/gin"
	"protu.ai/quiz-service/pkg/errors"
)

type Response struct {
	Status  string      `json:"status"`
	Message string      `json:"message,omitempty"`
	Data    interface{} `json:"data,omitempty"`
	Error   interface{} `json:"error,omitempty"`
}

func Success(c *gin.Context, statusCode int, message string, data interface{}) {
	c.JSON(statusCode, Response{
		Status:  "success",
		Message: message,
		Data:    data,
	})
}

func Error(c *gin.Context, err error) {
	var statusCode int
	var errorResponse interface{}

	switch e := err.(type) {
	case *errors.AppError:
		statusCode = e.StatusCode
		errorResponse = gin.H{
			"code":    e.Code,
			"message": e.Message,
			"details": e.Details,
		}
	default:
		statusCode = http.StatusInternalServerError
		errorResponse = gin.H{
			"code":    "INTERNAL_ERROR",
			"message": "An unexpected error occurred",
		}
	}

	c.JSON(statusCode, Response{
		Status: "error",
		Error:  errorResponse,
	})
}

func Created(c *gin.Context, message string, data interface{}) {
	Success(c, http.StatusCreated, message, data)
}

func OK(c *gin.Context, message string, data interface{}) {
	Success(c, http.StatusOK, message, data)
}

func NoContent(c *gin.Context) {
	c.Status(http.StatusNoContent)
}

func QuizCreated(c *gin.Context, data interface{}) {
	Created(c, "Quiz created successfully", data)
}

func QuizUpdated(c *gin.Context, data interface{}) {
	OK(c, "Quiz updated successfully", data)
}

func QuizFound(c *gin.Context, data interface{}) {
	OK(c, "Quiz retrieved successfully", data)
}

func QuizDeleted(c *gin.Context) {
	NoContent(c)
}

func QuizzesListed(c *gin.Context, data interface{}) {
	OK(c, "Quizzes retrieved successfully", data)
}

func QuizStageCompleted(c *gin.Context, stage int, data interface{}) {
	OK(c, fmt.Sprintf("Quiz stage %d completed successfully", stage), data)
}

func AttemptCreated(c *gin.Context, data interface{}) {
	Created(c, "Quiz attempt created successfully", data)
}

func AttemptUpdated(c *gin.Context, data interface{}) {
	OK(c, "Quiz attempt updated successfully", data)
}

func AttemptFound(c *gin.Context, data interface{}) {
	OK(c, "Quiz attempt retrieved successfully", data)
}

func AttemptSubmitted(c *gin.Context, data interface{}) {
	OK(c, "Quiz attempt submitted successfully", data)
}
