package repository

import (
	"errors"
)

var (
	ErrInvalidID       = errors.New("invalid id format")
	ErrQuizNotFound    = errors.New("quiz not found")
	ErrAttemptNotFound = errors.New("attempt not found")
	ErrNotFound        = errors.New("resource not found")
	ErrUserNotFound    = errors.New("user not found")
	ErrDuplicateKey    = errors.New("duplicate key error")
)

func wrapError(err error, message string) error {
	return errors.New(message + ": " + err.Error())
}
