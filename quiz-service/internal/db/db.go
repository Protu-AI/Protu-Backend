package db

import (
	"context"
	"fmt"
	"log"
	"time"

	"go.mongodb.org/mongo-driver/bson"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
	"protu.ai/quiz-service/config"
)

func ConnectMongoDB(cfg config.Config) (*mongo.Client, error) {
	ctx, cancel := context.WithTimeout(context.Background(), time.Duration(cfg.MongoTimeout)*time.Second)
	defer cancel()

	log.Printf("Connecting to MongoDB at %s...", cfg.MongoURI)
	clientOptions := options.Client().ApplyURI(cfg.MongoURI)
	client, err := mongo.Connect(ctx, clientOptions)
	if err != nil {
		return nil, fmt.Errorf("failed to connect to MongoDB: %w", err)
	}

	err = client.Ping(ctx, nil)
	if err != nil {
		return nil, fmt.Errorf("failed to ping MongoDB: %w", err)
	}

	log.Println("Successfully connected to MongoDB")

	err = runMigrations(client, cfg.MongoDB)
	if err != nil {
		return nil, fmt.Errorf("failed to run migrations: %w", err)
	}

	return client, nil
}

func runMigrations(client *mongo.Client, dbName string) error {
	db := client.Database(dbName)
	log.Println("Running database migrations...")

	collections := []string{"users", "quizzes", "quiz_attempts", "user_stats"}
	for _, collName := range collections {
		err := createCollectionIfNotExists(db, collName)
		if err != nil {
			return err
		}
	}

	err := createIndexes(db)
	if err != nil {
		return err
	}

	log.Println("Database migrations completed successfully")
	return nil
}

func createCollectionIfNotExists(db *mongo.Database, collName string) error {
	collections, err := db.ListCollectionNames(context.Background(), bson.D{})
	if err != nil {
		return fmt.Errorf("failed to list collections: %w", err)
	}

	for _, name := range collections {
		if name == collName {
			return nil
		}
	}

	err = db.CreateCollection(context.Background(), collName)
	if err != nil {
		return fmt.Errorf("failed to create collection %s: %w", collName, err)
	}
	log.Printf("Created collection: %s", collName)
	return nil
}

func createIndexes(db *mongo.Database) error {
	userIndexes := []mongo.IndexModel{
		{
			Keys:    bson.D{{Key: "publicId", Value: 1}},
			Options: options.Index().SetUnique(true),
		},
	}
	_, err := db.Collection("users").Indexes().CreateMany(context.Background(), userIndexes)
	if err != nil {
		return fmt.Errorf("failed to create indexes for users collection: %w", err)
	}

	quizIndexes := []mongo.IndexModel{
		{
			Keys: bson.D{{Key: "userId", Value: 1}},
		},
		{
			Keys: bson.D{{Key: "status", Value: 1}},
		},
		{
			Keys: bson.D{{Key: "topic", Value: 1}},
		},
		{
			Keys: bson.D{{Key: "userId", Value: 1}, {Key: "status", Value: 1}},
		},
	}
	_, err = db.Collection("quizzes").Indexes().CreateMany(context.Background(), quizIndexes)
	if err != nil {
		return fmt.Errorf("failed to create indexes for quizzes collection: %w", err)
	}

	attemptIndexes := []mongo.IndexModel{
		{
			Keys: bson.D{{Key: "userId", Value: 1}},
		},
		{
			Keys: bson.D{{Key: "quizId", Value: 1}},
		},
		{
			Keys: bson.D{{Key: "status", Value: 1}},
		},
	}
	_, err = db.Collection("quiz_attempts").Indexes().CreateMany(context.Background(), attemptIndexes)
	if err != nil {
		return fmt.Errorf("failed to create indexes for quiz_attempts collection: %w", err)
	}

	statsIndexes := []mongo.IndexModel{
		{
			Keys:    bson.D{{Key: "userId", Value: 1}},
			Options: options.Index().SetUnique(true),
		},
	}
	_, err = db.Collection("user_stats").Indexes().CreateMany(context.Background(), statsIndexes)
	if err != nil {
		return fmt.Errorf("failed to create indexes for user_stats collection: %w", err)
	}

	log.Println("All indexes created successfully")
	return nil
}

func RunMigrations(client *mongo.Client, dbName string) error {
	return runMigrations(client, dbName)
}
