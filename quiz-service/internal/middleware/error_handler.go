package middleware

import (
	"fmt"
	"runtime/debug"

	"github.com/gin-gonic/gin"
	"protu.ai/quiz-service/pkg/errors"
	"protu.ai/quiz-service/pkg/response"
)

func ErrorHandler() gin.HandlerFunc {
	return func(c *gin.Context) {
		defer func() {
			if err := recover(); err != nil {
				fmt.Printf("PANIC: %v\n%s\n", err, debug.Stack())

				var appErr *errors.AppError
				switch e := err.(type) {
				case string:
					appErr = errors.InternalError(e)
				case error:
					appErr = errors.InternalError(e.Error())
				default:
					appErr = errors.InternalError("An unexpected error occurred")
				}

				response.Error(c, appErr)
				c.Abort()
			}
		}()

		c.Next()
	}
}

func ValidationMiddleware() gin.HandlerFunc {
	return func(c *gin.Context) {
		c.Next()

		if len(c.Errors) > 0 {
			var validationErrors []string
			for _, err := range c.Errors {
				validationErrors = append(validationErrors, err.Error())
			}

			appErr := errors.ValidationError("Request validation failed", validationErrors)

			response.Error(c, appErr)
			c.Abort()
			return
		}
	}
}
