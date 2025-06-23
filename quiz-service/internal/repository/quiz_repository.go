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

const (
	QuizCollection = "quizzes"
)

type QuizRepository struct {
	db         *mongo.Database
	collection *mongo.Collection
}

func NewQuizRepository(db *mongo.Database) *QuizRepository {
	return &QuizRepository{
		db:         db,
		collection: db.Collection(QuizCollection),
	}
}

func (r *QuizRepository) GetQuizzesByUserIDAndStatus(ctx context.Context, userID, status string) ([]*models.Quiz, error) {
	filter := bson.M{
		"userId": userID,
		"status": status,
	}

	opts := options.Find().
		SetSort(bson.M{"createdAt": -1})

	cursor, err := r.collection.Find(ctx, filter, opts)
	if err != nil {
		return nil, wrapError(err, "failed to find quizzes")
	}
	defer cursor.Close(ctx)

	quizzes := make([]*models.Quiz, 0, 10)
	if err = cursor.All(ctx, &quizzes); err != nil {
		return nil, wrapError(err, "failed to decode quizzes")
	}

	return quizzes, nil
}

func (r *QuizRepository) GetQuizByID(ctx context.Context, id string) (*models.Quiz, error) {
	objectID, err := primitive.ObjectIDFromHex(id)
	if err != nil {
		return nil, ErrInvalidID
	}

	filter := bson.M{"_id": objectID}

	var quiz models.Quiz
	err = r.collection.FindOne(ctx, filter).Decode(&quiz)

	if err != nil {
		if errors.Is(err, mongo.ErrNoDocuments) {
			return nil, ErrQuizNotFound
		}
		return nil, wrapError(err, "failed to retrieve quiz")
	}

	return &quiz, nil
}

func (r *QuizRepository) CreateQuiz(ctx context.Context, quiz *models.Quiz) error {
	if quiz.ID.IsZero() {
		quiz.ID = primitive.NewObjectID()
	}

	now := time.Now()
	quiz.CreatedAt = now
	quiz.UpdatedAt = now

	_, err := r.collection.InsertOne(ctx, quiz)
	if err != nil {
		return wrapError(err, "failed to create quiz")
	}

	return nil
}

func (r *QuizRepository) UpdateQuiz(ctx context.Context, quiz *models.Quiz) error {
	quiz.UpdatedAt = time.Now()

	update := bson.M{
		"$set": bson.M{
			"title":               quiz.Title,
			"topic":               quiz.Topic,
			"prompt":              quiz.Prompt,
			"subtopics":           quiz.Subtopics,
			"additionalSubtopics": quiz.AdditionalSubtopics,
			"difficultyLevel":     quiz.DifficultyLevel,
			"numberOfQuestions":   quiz.NumberOfQuestions,
			"questionTypes":       quiz.QuestionTypes,
			"timeLimit":           quiz.TimeLimit,
			"additionalPrefs":     quiz.AdditionalPrefs,
			"status":              quiz.Status,
			"questions":           quiz.Questions,
			"attemptCount":        quiz.AttemptCount,
			"publishedAt":         quiz.PublishedAt,
			"updatedAt":           quiz.UpdatedAt,
		},
	}

	result, err := r.collection.UpdateOne(ctx, bson.M{"_id": quiz.ID}, update)
	if err != nil {
		return wrapError(err, "failed to update quiz")
	}

	if result.MatchedCount == 0 {
		return ErrQuizNotFound
	}

	return nil
}

func (r *QuizRepository) IncrementAttemptCount(ctx context.Context, quizID string) error {
	objectID, err := primitive.ObjectIDFromHex(quizID)
	if err != nil {
		return ErrInvalidID
	}

	update := bson.M{
		"$inc": bson.M{
			"attemptCount": 1,
		},
		"$set": bson.M{
			"updatedAt": time.Now(),
		},
	}

	result, err := r.collection.UpdateOne(ctx, bson.M{"_id": objectID}, update)
	if err != nil {
		return wrapError(err, "failed to increment attempt count")
	}

	if result.MatchedCount == 0 {
		return ErrQuizNotFound
	}

	return nil
}

func (r *QuizRepository) GetQuizzesByUserIDAndStatusPaginated(ctx context.Context, userID, status string, page, pageSize int) ([]*models.Quiz, int64, error) {
	filter := bson.M{
		"userId": userID,
		"status": status,
	}

	totalCount, err := r.collection.CountDocuments(ctx, filter)
	if err != nil {
		return nil, 0, wrapError(err, "failed to count quizzes")
	}

	skip := (page - 1) * pageSize

	projection := bson.M{
		"title":             1,
		"topic":             1,
		"difficultyLevel":   1,
		"numberOfQuestions": 1,
		"timeLimit":         1,
		"status":            1,
		"attemptCount":      1,
		"createdAt":         1,
		"updatedAt":         1,
		"publishedAt":       1,
	}

	opts := options.Find().
		SetSort(bson.M{"createdAt": -1}).
		SetSkip(int64(skip)).
		SetLimit(int64(pageSize)).
		SetProjection(projection)

	cursor, err := r.collection.Find(ctx, filter, opts)
	if err != nil {
		return nil, 0, wrapError(err, "failed to find quizzes")
	}
	defer cursor.Close(ctx)

	quizzes := make([]*models.Quiz, 0, pageSize)
	if err = cursor.All(ctx, &quizzes); err != nil {
		return nil, 0, wrapError(err, "failed to decode quizzes")
	}

	return quizzes, totalCount, nil
}

func (r *QuizRepository) GetQuizzesByUserIDAndStatusesPaginated(ctx context.Context, userID string, statuses []string, page, pageSize int) ([]*models.Quiz, int64, error) {
	filter := bson.M{
		"userId": userID,
		"status": bson.M{"$in": statuses},
	}

	totalCount, err := r.collection.CountDocuments(ctx, filter)
	if err != nil {
		return nil, 0, wrapError(err, "failed to count quizzes")
	}

	skip := (page - 1) * pageSize

	projection := bson.M{
		"title":             1,
		"topic":             1,
		"difficultyLevel":   1,
		"numberOfQuestions": 1,
		"timeLimit":         1,
		"status":            1,
		"attemptCount":      1,
		"createdAt":         1,
		"updatedAt":         1,
		"publishedAt":       1,
	}

	opts := options.Find().
		SetSort(bson.M{"createdAt": -1}).
		SetSkip(int64(skip)).
		SetLimit(int64(pageSize)).
		SetProjection(projection)

	cursor, err := r.collection.Find(ctx, filter, opts)
	if err != nil {
		return nil, 0, wrapError(err, "failed to find quizzes")
	}
	defer cursor.Close(ctx)

	quizzes := make([]*models.Quiz, 0, pageSize)
	if err = cursor.All(ctx, &quizzes); err != nil {
		return nil, 0, wrapError(err, "failed to decode quizzes")
	}

	return quizzes, totalCount, nil
}

func (r *QuizRepository) GetQuizzesByIDs(ctx context.Context, quizIDs []string) ([]*models.Quiz, error) {
	objectIDs := make([]primitive.ObjectID, 0, len(quizIDs))
	for _, id := range quizIDs {
		objectID, err := primitive.ObjectIDFromHex(id)
		if err != nil {
			continue
		}
		objectIDs = append(objectIDs, objectID)
	}

	if len(objectIDs) == 0 {
		return []*models.Quiz{}, nil
	}

	filter := bson.M{
		"_id": bson.M{"$in": objectIDs},
	}

	projection := bson.M{
		"title":             1,
		"topic":             1,
		"difficultyLevel":   1,
		"numberOfQuestions": 1,
		"timeLimit":         1,
		"status":            1,
		"attemptCount":      1,
		"createdAt":         1,
		"updatedAt":         1,
		"publishedAt":       1,
	}

	opts := options.Find().SetProjection(projection)

	cursor, err := r.collection.Find(ctx, filter, opts)
	if err != nil {
		return nil, wrapError(err, "failed to find quizzes by IDs")
	}
	defer cursor.Close(ctx)

	quizzes := make([]*models.Quiz, 0, len(objectIDs))
	if err = cursor.All(ctx, &quizzes); err != nil {
		return nil, wrapError(err, "failed to decode quizzes")
	}

	return quizzes, nil
}

func (r *QuizRepository) GetQuizSummaryByID(ctx context.Context, id string) (*models.Quiz, error) {
	objectID, err := primitive.ObjectIDFromHex(id)
	if err != nil {
		return nil, ErrInvalidID
	}

	filter := bson.M{"_id": objectID}

	projection := bson.M{
		"title":             1,
		"topic":             1,
		"prompt":            1,
		"difficultyLevel":   1,
		"numberOfQuestions": 1,
		"timeLimit":         1,
		"status":            1,
		"attemptCount":      1,
		"createdAt":         1,
		"updatedAt":         1,
		"publishedAt":       1,
		"questions":         0,
	}

	opts := options.FindOne().SetProjection(projection)

	var quiz models.Quiz
	err = r.collection.FindOne(ctx, filter, opts).Decode(&quiz)

	if err != nil {
		if errors.Is(err, mongo.ErrNoDocuments) {
			return nil, ErrQuizNotFound
		}
		return nil, wrapError(err, "failed to retrieve quiz summary")
	}

	return &quiz, nil
}

func (r *QuizRepository) DeleteQuiz(ctx context.Context, id string) error {
	objectID, err := primitive.ObjectIDFromHex(id)
	if err != nil {
		return ErrInvalidID
	}

	filter := bson.M{"_id": objectID}

	result, err := r.collection.DeleteOne(ctx, filter)
	if err != nil {
		return wrapError(err, "failed to delete quiz")
	}

	if result.DeletedCount == 0 {
		return ErrQuizNotFound
	}

	return nil
}
