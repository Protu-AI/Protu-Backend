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
