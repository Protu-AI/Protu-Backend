package errors

import (
	"fmt"
	"net/http"
)

type ErrorType string

const (
	ErrorTypeValidation     ErrorType = "VALIDATION_ERROR"
	ErrorTypeAuthorization  ErrorType = "AUTHORIZATION_ERROR"
	ErrorTypeAuthentication ErrorType = "AUTHENTICATION_ERROR"
	ErrorTypeNotFound       ErrorType = "NOT_FOUND"
	ErrorTypeInternal       ErrorType = "INTERNAL_ERROR"
	ErrorTypeBadRequest     ErrorType = "BAD_REQUEST"
	ErrorTypeConflict       ErrorType = "CONFLICT"
	ErrorTypeQuizCreation   ErrorType = "QUIZ_CREATION_ERROR"
	ErrorTypeQuizExecution  ErrorType = "QUIZ_EXECUTION_ERROR"
	ErrorTypeQuizSubmission ErrorType = "QUIZ_SUBMISSION_ERROR"
)

type AppError struct {
	Type       ErrorType `json:"-"`
	StatusCode int       `json:"-"`
	Code       string    `json:"code"`
	Message    string    `json:"message"`
	Details    any       `json:"details,omitempty"`
}

func (e *AppError) Error() string {
	return fmt.Sprintf("%s: %s", e.Code, e.Message)
}

func New(errType ErrorType, message string, details any) *AppError {
	statusCode := getStatusCodeForErrorType(errType)
	return &AppError{
		Type:       errType,
		StatusCode: statusCode,
		Code:       string(errType),
		Message:    message,
		Details:    details,
	}
}

func ValidationError(message string, details any) *AppError {
	return New(ErrorTypeValidation, message, details)
}

func AuthorizationError(message string) *AppError {
	return New(ErrorTypeAuthorization, message, nil)
}

func AuthenticationError(message string) *AppError {
	return New(ErrorTypeAuthentication, message, nil)
}

func NotFoundError(message string) *AppError {
	return New(ErrorTypeNotFound, message, nil)
}

func InternalError(message string) *AppError {
	return New(ErrorTypeInternal, message, nil)
}

func BadRequestError(message string, details any) *AppError {
	return New(ErrorTypeBadRequest, message, details)
}

func ConflictError(message string) *AppError {
	return New(ErrorTypeConflict, message, nil)
}

func QuizCreationError(message string, details any) *AppError {
	return New(ErrorTypeQuizCreation, message, details)
}

func QuizExecutionError(message string, details any) *AppError {
	return New(ErrorTypeQuizExecution, message, details)
}

func QuizSubmissionError(message string, details any) *AppError {
	return New(ErrorTypeQuizSubmission, message, details)
}

func getStatusCodeForErrorType(errType ErrorType) int {
	switch errType {
	case ErrorTypeValidation, ErrorTypeBadRequest:
		return http.StatusBadRequest
	case ErrorTypeAuthorization:
		return http.StatusForbidden
	case ErrorTypeAuthentication:
		return http.StatusUnauthorized
	case ErrorTypeNotFound:
		return http.StatusNotFound
	case ErrorTypeConflict:
		return http.StatusConflict
	case ErrorTypeQuizCreation, ErrorTypeQuizExecution, ErrorTypeQuizSubmission:
		return http.StatusBadRequest
	default:
		return http.StatusInternalServerError
	}
}
