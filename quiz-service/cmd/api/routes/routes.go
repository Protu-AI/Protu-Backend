package routes

import (
	"github.com/gin-gonic/gin"
	"protu.ai/quiz-service/cmd/api/handlers"
	"protu.ai/quiz-service/config"
	"protu.ai/quiz-service/internal/middleware"
	apiResponse "protu.ai/quiz-service/pkg/response"
)

func SetupRoutes(
	router *gin.Engine,
	cfg *config.Config,
	quizHandler *handlers.QuizHandler,
	attemptHandler *handlers.AttemptHandler,
	dashboardHandler *handlers.DashboardHandler,
) {
	// Health endpoint
	router.GET("/health", func(c *gin.Context) {
		apiResponse.OK(c, "Service is healthy", gin.H{
			"status": "ok",
		})
	})

	v1 := router.Group("/api/v1")
	v1.Use(middleware.JWTMiddleware(cfg))
	{
		dashboard := v1.Group("/quizzes")
		{
			dashboard.GET("/dashboard/summary", dashboardHandler.GetDashboardSummary)
			dashboard.GET("/dashboard/passed", dashboardHandler.GetPassedQuizzes)
			dashboard.GET("/dashboard/failed", dashboardHandler.GetFailedQuizzes)
			dashboard.GET("/dashboard/drafts", dashboardHandler.GetDraftQuizzes)
		}

		quizzes := v1.Group("/quizzes")
		{
			quizzes.POST("/stage1", quizHandler.CreateQuizStage1)
			quizzes.POST("/stage2", quizHandler.CompleteQuizStage2)
			quizzes.PATCH("/:quizId/title", quizHandler.UpdateQuizTitle)
			quizzes.DELETE("/:quizId", quizHandler.DeleteQuiz)
		}

		attempts := v1.Group("/attempts")
		{
			attempts.GET("/preview/:quizId", attemptHandler.PreviewQuiz)
			attempts.POST("/start/:quizId", attemptHandler.StartQuiz)
			attempts.PUT("/:id/submit", attemptHandler.SubmitAttempt)
		}
	}
}
