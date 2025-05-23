package repository

import (
	"context"
	"errors"
	"time"

	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/bson/primitive"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
	"protu.ai/quiz-service/internal/models"
)

const AttemptCollection = "attempts"

type AttemptRepository struct {
	db         *mongo.Database
	collection *mongo.Collection
}

func NewAttemptRepository(db *mongo.Database) *AttemptRepository {
	return &AttemptRepository{
		db:         db,
		collection: db.Collection(AttemptCollection),
	}
}

func (r *AttemptRepository) GetAttemptsByUserID(ctx context.Context, userID string) ([]*models.QuizAttempt, error) {
	filter := bson.M{"userId": userID}

	opts := options.Find().
		SetSort(bson.M{"startedAt": -1})

	return r.findAttempts(ctx, filter, opts, "failed to find attempts")
}

func (r *AttemptRepository) GetAttemptByID(ctx context.Context, id string) (*models.QuizAttempt, error) {
	attemptID, err := primitive.ObjectIDFromHex(id)
	if err != nil {
		return nil, ErrInvalidID
	}

	var attempt models.QuizAttempt
	err = r.collection.FindOne(ctx, bson.M{"_id": attemptID}).Decode(&attempt)

	if err != nil {
		if errors.Is(err, mongo.ErrNoDocuments) {
			return nil, ErrAttemptNotFound
		}
		return nil, wrapError(err, "failed to retrieve attempt")
	}

	return &attempt, nil
}

func (r *AttemptRepository) CreateAttempt(ctx context.Context, attempt *models.QuizAttempt) error {
	if attempt.ID.IsZero() {
		attempt.ID = primitive.NewObjectID()
	}
	if attempt.StartedAt.IsZero() {
		attempt.StartedAt = time.Now()
	}

	_, err := r.collection.InsertOne(ctx, attempt)
	if err != nil {
		return wrapError(err, "failed to create attempt")
	}

	return nil
}

func (r *AttemptRepository) UpdateAttempt(ctx context.Context, attempt *models.QuizAttempt) error {
	update := bson.M{
		"$set": bson.M{
			"answers":     attempt.Answers,
			"score":       attempt.Score,
			"passed":      attempt.Passed,
			"timeTaken":   attempt.TimeTaken,
			"completedAt": attempt.CompletedAt,
			"status":      attempt.Status,
		},
	}

	result, err := r.collection.UpdateOne(ctx, bson.M{"_id": attempt.ID}, update)
	if err != nil {
		return wrapError(err, "failed to update attempt")
	}

	if result.MatchedCount == 0 {
		return ErrAttemptNotFound
	}

	return nil
}

func (r *AttemptRepository) GetPassedAttemptsByUserID(ctx context.Context, userID string) ([]*models.QuizAttempt, error) {
	filter := bson.M{
		"userId": userID,
		"passed": true,
		"status": "completed",
	}

	opts := options.Find().
		SetSort(bson.M{"completedAt": -1})

	return r.findAttempts(ctx, filter, opts, "failed to find passed attempts")
}

func (r *AttemptRepository) GetFailedAttemptsByUserID(ctx context.Context, userID string) ([]*models.QuizAttempt, error) {
	filter := bson.M{
		"userId": userID,
		"passed": false,
		"status": "completed",
	}

	opts := options.Find().
		SetSort(bson.M{"completedAt": -1})

	return r.findAttempts(ctx, filter, opts, "failed to find failed attempts")
}

func (r *AttemptRepository) findAttempts(ctx context.Context, filter bson.M, opts *options.FindOptions, errMsg string) ([]*models.QuizAttempt, error) {
	cursor, err := r.collection.Find(ctx, filter, opts)
	if err != nil {
		return nil, wrapError(err, errMsg)
	}
	defer cursor.Close(ctx)

	attempts := make([]*models.QuizAttempt, 0, 10)
	if err = cursor.All(ctx, &attempts); err != nil {
		return nil, wrapError(err, errMsg+" (decoding)")
	}

	return attempts, nil
}
