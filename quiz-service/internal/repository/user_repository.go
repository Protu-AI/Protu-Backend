package repository

import (
	"context"
	"fmt"
	"time"

	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/bson/primitive"
	"go.mongodb.org/mongo-driver/mongo"
	"protu.ai/quiz-service/internal/models"
)

type UserRepository interface {
	FindByPublicID(ctx context.Context, publicID string) (*models.User, error)
	Create(ctx context.Context, user *models.User) error
	Update(ctx context.Context, user *models.User) error
	Delete(ctx context.Context, publicID string) error
	CreateOrUpdate(ctx context.Context, user *models.User) error
}

type userRepository struct {
	collection *mongo.Collection
}

func NewUserRepository(db *mongo.Database) UserRepository {
	return &userRepository{
		collection: db.Collection("users"),
	}
}

func (r *userRepository) FindByPublicID(ctx context.Context, publicID string) (*models.User, error) {
	var user models.User
	err := r.collection.FindOne(ctx, bson.M{"publicId": publicID}).Decode(&user)
	if err != nil {
		if err == mongo.ErrNoDocuments {
			return nil, ErrNotFound
		}
		return nil, fmt.Errorf("error finding user by publicID: %w", err)
	}
	return &user, nil
}

func (r *userRepository) Create(ctx context.Context, user *models.User) error {
	now := time.Now()
	if user.CreatedAt.IsZero() {
		user.CreatedAt = now
	}
	if user.UpdatedAt.IsZero() {
		user.UpdatedAt = now
	}

	result, err := r.collection.InsertOne(ctx, user)
	if err != nil {
		if mongo.IsDuplicateKeyError(err) {
			return ErrDuplicateKey
		}
		return fmt.Errorf("error creating user: %w", err)
	}

	if oid, ok := result.InsertedID.(primitive.ObjectID); ok {
		user.ID = oid
	}

	return nil
}

func (r *userRepository) Update(ctx context.Context, user *models.User) error {
	user.UpdatedAt = time.Now()

	update := bson.M{
		"$set": bson.M{
			"roles":     user.Roles,
			"updatedAt": user.UpdatedAt,
		},
	}

	result, err := r.collection.UpdateOne(ctx, bson.M{"publicId": user.PublicID}, update)
	if err != nil {
		return fmt.Errorf("error updating user: %w", err)
	}

	if result.MatchedCount == 0 {
		return ErrNotFound
	}

	return nil
}

func (r *userRepository) Delete(ctx context.Context, publicID string) error {
	result, err := r.collection.DeleteOne(ctx, bson.M{"publicId": publicID})
	if err != nil {
		return fmt.Errorf("error deleting user: %w", err)
	}

	if result.DeletedCount == 0 {
		return ErrNotFound
	}

	return nil
}

func (r *userRepository) CreateOrUpdate(ctx context.Context, user *models.User) error {
	existingUser, err := r.FindByPublicID(ctx, user.PublicID)
	if err != nil && err != ErrNotFound {
		return err
	}

	if existingUser != nil {
		existingUser.Roles = user.Roles
		return r.Update(ctx, existingUser)
	} else {
		return r.Create(ctx, user)
	}
}
