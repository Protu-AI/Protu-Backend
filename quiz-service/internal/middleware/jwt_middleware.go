package middleware

import (
	"fmt"
	"strings"

	"github.com/gin-gonic/gin"
	"github.com/golang-jwt/jwt/v5"
	"protu.ai/quiz-service/config"
	"protu.ai/quiz-service/pkg/errors"
	apiResponse "protu.ai/quiz-service/pkg/response"
)

type UserClaims struct {
	jwt.RegisteredClaims
}

func JWTMiddleware(cfg *config.Config) gin.HandlerFunc {
	return func(c *gin.Context) {
		authHeader := c.GetHeader("Authorization")

		token, err := getTokenFromHeader(authHeader)
		if err != nil {
			apiResponse.Error(c, errors.AuthenticationError("Invalid authorization header"))
			c.Abort()
			return
		}

		userID, err := validateToken(token, cfg.JWTSecret)
		if err != nil {
			apiResponse.Error(c, errors.AuthenticationError("Invalid or expired token"))
			c.Abort()
			return
		}

		c.Set("userID", userID)
		fmt.Printf("User ID from token: %s\n", userID)

		c.Next()
	}
}

func getTokenFromHeader(authHeader string) (string, error) {
	if authHeader == "" {
		return "", fmt.Errorf("authorization header is required")
	}

	if !strings.HasPrefix(authHeader, "Bearer ") {
		return "", fmt.Errorf("authorization header must start with 'Bearer '")
	}

	token := strings.TrimPrefix(authHeader, "Bearer ")
	if token == "" {
		return "", fmt.Errorf("token cannot be empty")
	}

	return token, nil
}

func validateToken(tokenString string, jwtSecret string) (string, error) {
	if jwtSecret == "" {
		return "", fmt.Errorf("JWT secret is not configured")
	}

	token, err := jwt.ParseWithClaims(tokenString, &UserClaims{}, func(token *jwt.Token) (interface{}, error) {
		if _, ok := token.Method.(*jwt.SigningMethodHMAC); !ok {
			return nil, fmt.Errorf("unexpected signing method: %v", token.Header["alg"])
		}
		return []byte(jwtSecret), nil
	})

	if err != nil {
		return "", fmt.Errorf("failed to parse token: %w", err)
	}

	if !token.Valid {
		return "", fmt.Errorf("token is invalid")
	}

	claims, ok := token.Claims.(*UserClaims)
	if !ok {
		return "", fmt.Errorf("failed to extract claims")
	}

	userID := claims.Subject
	if userID == "" {
		return "", fmt.Errorf("user ID not found in token")
	}

	return userID, nil
}

func GetUserIDFromContext(c *gin.Context) (string, error) {
	userID, exists := c.Get("userID")
	if !exists {
		return "", fmt.Errorf("user ID not found in context")
	}

	userIDStr, ok := userID.(string)
	if !ok {
		return "", fmt.Errorf("user ID is not a string")
	}

	return userIDStr, nil
}
