package main

import (
	"log"
	"net/http"

	"github.com/Protu-AI/Protu-Backend/code-execution-service/api"
	"github.com/gin-gonic/gin"
	"github.com/joho/godotenv"
)

func main() {
	if err := godotenv.Load(); err != nil {
		log.Printf("Warning: .env file not found")
	}

	router := gin.Default()

	setupRoutes(router)

	port := "8086"

	log.Printf("Server starting on port %s", port)
	if err := router.Run(":" + port); err != nil {
		log.Fatalf("Failed to start server: %v", err)
	}
}

func setupRoutes(router *gin.Engine) {
	router.GET("/health", func(c *gin.Context) {
		c.JSON(http.StatusOK, gin.H{"status": "OK"})
	})

	execRouter := router.Group("/api/v1/execute")
	{
		execRouter.POST("/", api.HandleCodeExecution)
	}
}
