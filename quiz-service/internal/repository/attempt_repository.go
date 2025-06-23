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
		"status": models.AttemptStatusCompleted,
	}

	opts := options.Find().
		SetSort(bson.M{"completedAt": -1})

	return r.findAttempts(ctx, filter, opts, "failed to find passed attempts")
}

func (r *AttemptRepository) GetFailedAttemptsByUserID(ctx context.Context, userID string) ([]*models.QuizAttempt, error) {
	filter := bson.M{
		"userId": userID,
		"passed": false,
		"status": models.AttemptStatusCompleted,
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

// GetBestAttemptsByUserIDWithPagination gets the best attempt per quiz for a user with pagination
func (r *AttemptRepository) GetBestAttemptsByUserIDWithPagination(ctx context.Context, userID string, passed *bool, page, pageSize int, sortBy, sortOrder string) ([]*models.QuizAttempt, int64, error) {
	matchStage := bson.M{
		"$match": bson.M{
			"userId": userID,
			"status": models.AttemptStatusCompleted,
		},
	}

	// Add passed filter if specified
	if passed != nil {
		matchStage["$match"].(bson.M)["passed"] = *passed
	}

	// Group by quizId to get the best attempt per quiz
	groupStage := bson.M{
		"$group": bson.M{
			"_id":      "$quizId",
			"attempts": bson.M{"$push": "$$ROOT"},
		},
	}

	// Add a stage to get the best attempt from the array
	addFieldsStage := bson.M{
		"$addFields": bson.M{
			"bestAttempt": bson.M{
				"$reduce": bson.M{
					"input":        "$attempts",
					"initialValue": bson.M{"$arrayElemAt": []interface{}{"$attempts", 0}},
					"in": bson.M{
						"$cond": bson.M{
							"if": bson.M{
								"$or": []bson.M{
									{"$gt": []interface{}{"$$this.score", "$$value.score"}},
									{
										"$and": []bson.M{
											{"$eq": []interface{}{"$$this.score", "$$value.score"}},
											{"$gt": []interface{}{"$$this.completedAt", "$$value.completedAt"}},
										},
									},
								},
							},
							"then": "$$this",
							"else": "$$value",
						},
					},
				},
			},
		},
	}

	// Replace root with the best attempt
	replaceRootStage := bson.M{
		"$replaceRoot": bson.M{
			"newRoot": "$bestAttempt",
		},
	}

	// Sort stage
	sortStage := bson.M{
		"$sort": bson.M{},
	}
	switch sortBy {
	case "score":
		if sortOrder == "asc" {
			sortStage["$sort"].(bson.M)["score"] = 1
		} else {
			sortStage["$sort"].(bson.M)["score"] = -1
		}
	case "completedAt":
		if sortOrder == "asc" {
			sortStage["$sort"].(bson.M)["completedAt"] = 1
		} else {
			sortStage["$sort"].(bson.M)["completedAt"] = -1
		}
	default:
		sortStage["$sort"].(bson.M)["completedAt"] = -1
	}

	// Count pipeline for total
	countPipeline := []bson.M{
		matchStage,
		groupStage,
		addFieldsStage,
		{"$count": "total"},
	}

	// Main pipeline with pagination
	pipeline := []bson.M{
		matchStage,
		groupStage,
		addFieldsStage,
		replaceRootStage,
		sortStage,
		{"$skip": (page - 1) * pageSize},
		{"$limit": pageSize},
	}

	// Get total count
	countCursor, err := r.collection.Aggregate(ctx, countPipeline)
	if err != nil {
		return nil, 0, wrapError(err, "failed to count best attempts")
	}
	defer countCursor.Close(ctx)

	var countResult []bson.M
	if err = countCursor.All(ctx, &countResult); err != nil {
		return nil, 0, wrapError(err, "failed to decode count")
	}

	var totalCount int64 = 0
	if len(countResult) > 0 {
		if total, ok := countResult[0]["total"].(int32); ok {
			totalCount = int64(total)
		}
	}

	// Get paginated results
	cursor, err := r.collection.Aggregate(ctx, pipeline)
	if err != nil {
		return nil, 0, wrapError(err, "failed to find best attempts")
	}
	defer cursor.Close(ctx)

	attempts := make([]*models.QuizAttempt, 0, pageSize)
	if err = cursor.All(ctx, &attempts); err != nil {
		return nil, 0, wrapError(err, "failed to decode best attempts")
	}

	return attempts, totalCount, nil
}

// GetAttemptsByQuizIDs gets attempts for multiple quiz IDs in a single query
func (r *AttemptRepository) GetAttemptsByQuizIDs(ctx context.Context, quizIDs []string, userID string) ([]*models.QuizAttempt, error) {
	objectIDs := make([]primitive.ObjectID, 0, len(quizIDs))
	for _, id := range quizIDs {
		objectID, err := primitive.ObjectIDFromHex(id)
		if err != nil {
			continue // Skip invalid IDs
		}
		objectIDs = append(objectIDs, objectID)
	}

	if len(objectIDs) == 0 {
		return []*models.QuizAttempt{}, nil
	}

	filter := bson.M{
		"quizId": bson.M{"$in": objectIDs},
		"userId": userID,
		"status": models.AttemptStatusCompleted,
	}

	opts := options.Find().
		SetSort(bson.M{"completedAt": -1})

	cursor, err := r.collection.Find(ctx, filter, opts)
	if err != nil {
		return nil, wrapError(err, "failed to find attempts by quiz IDs")
	}
	defer cursor.Close(ctx)

	attempts := make([]*models.QuizAttempt, 0, len(objectIDs))
	if err = cursor.All(ctx, &attempts); err != nil {
		return nil, wrapError(err, "failed to decode attempts")
	}

	return attempts, nil
}

// GetAttemptsByQuizID gets attempts for a single quiz ID
func (r *AttemptRepository) GetAttemptsByQuizID(ctx context.Context, quizID string) ([]*models.QuizAttempt, error) {
	objectID, err := primitive.ObjectIDFromHex(quizID)
	if err != nil {
		return nil, ErrInvalidID
	}

	filter := bson.M{
		"quizId": objectID,
	}

	opts := options.Find().
		SetSort(bson.M{"completedAt": -1})

	cursor, err := r.collection.Find(ctx, filter, opts)
	if err != nil {
		return nil, wrapError(err, "failed to find attempts by quiz ID")
	}
	defer cursor.Close(ctx)

	attempts := make([]*models.QuizAttempt, 0, 10)
	if err = cursor.All(ctx, &attempts); err != nil {
		return nil, wrapError(err, "failed to decode attempts")
	}

	return attempts, nil
}

// GetAttemptStatsForUser gets aggregated stats for a user
func (r *AttemptRepository) GetAttemptStatsForUser(ctx context.Context, userID string) (map[string]interface{}, error) {
	pipeline := []bson.M{
		{
			"$match": bson.M{
				"userId": userID,
				"status": models.AttemptStatusCompleted,
			},
		},
		{
			"$group": bson.M{
				"_id":       "$quizId",
				"bestScore": bson.M{"$max": "$score"},
				"passed": bson.M{
					"$max": bson.M{
						"$cond": bson.M{
							"if":   "$passed",
							"then": 1,
							"else": 0,
						},
					},
				},
			},
		},
		{
			"$group": bson.M{
				"_id":          nil,
				"totalQuizzes": bson.M{"$sum": 1},
				"averageScore": bson.M{"$avg": "$bestScore"},
				"passedCount":  bson.M{"$sum": "$passed"},
			},
		},
		{
			"$project": bson.M{
				"_id": 0,
				"totalQuizzes": bson.M{
					"$toDouble": "$totalQuizzes",
				},
				"averageScore": bson.M{
					"$ifNull": []interface{}{
						bson.M{"$toDouble": "$averageScore"},
						0.0,
					},
				},
				"passedCount": bson.M{
					"$toDouble": "$passedCount",
				},
				"successRate": bson.M{
					"$ifNull": []interface{}{
						bson.M{
							"$multiply": []interface{}{
								bson.M{"$divide": []interface{}{"$passedCount", "$totalQuizzes"}},
								100.0,
							},
						},
						0.0,
					},
				},
			},
		},
	}

	cursor, err := r.collection.Aggregate(ctx, pipeline)
	if err != nil {
		return nil, wrapError(err, "failed to get attempt stats")
	}
	defer cursor.Close(ctx)

	var results []bson.M
	if err = cursor.All(ctx, &results); err != nil {
		return nil, wrapError(err, "failed to decode attempt stats")
	}

	if len(results) == 0 {
		return map[string]interface{}{
			"totalQuizzes": 0.0,
			"averageScore": 0.0,
			"passedCount":  0.0,
			"successRate":  0.0,
		}, nil
	}

	return results[0], nil
}
