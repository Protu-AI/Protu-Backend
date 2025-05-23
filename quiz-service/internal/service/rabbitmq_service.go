package service

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"time"

	amqp "github.com/rabbitmq/amqp091-go"
	"protu.ai/quiz-service/internal/models"
	"protu.ai/quiz-service/internal/repository"
)

const (
	USER_EVENTS_EXCHANGE = "user.events.topic"
	USER_REPLICA_QUEUE   = "quiz-service.user-replica.queue"
	USER_CREATED_KEY     = "user.created"
	USER_UPDATED_KEY     = "user.updated"
	USER_DELETED_KEY     = "user.deleted"
	USER_PATTERN         = "user.*"
)

type RabbitMQService struct {
	connection     *amqp.Connection
	channel        *amqp.Channel
	userRepository repository.UserRepository
	url            string
}

type UserEventMessage struct {
	Data UserEventData `json:"data"`
}

type UserEventData struct {
	ID       string   `json:"id,omitempty"`
	PublicID string   `json:"publicId"`
	Roles    []string `json:"roles"`
}

func NewRabbitMQService(url string, userRepo repository.UserRepository) *RabbitMQService {
	return &RabbitMQService{
		url:            url,
		userRepository: userRepo,
	}
}

func (r *RabbitMQService) Connect() error {
	retries := 5
	delay := 5 * time.Second

	for i := 0; i < retries; i++ {
		log.Printf("Connecting to RabbitMQ at %s (attempt %d/%d)...", r.url, i+1, retries)

		conn, err := amqp.Dial(r.url)
		if err != nil {
			log.Printf("Failed to connect to RabbitMQ (attempt %d): %v", i+1, err)
			if i < retries-1 {
				time.Sleep(delay)
				continue
			}
			return fmt.Errorf("failed to connect to RabbitMQ after %d attempts: %w", retries, err)
		}

		channel, err := conn.Channel()
		if err != nil {
			conn.Close()
			log.Printf("Failed to open channel (attempt %d): %v", i+1, err)
			if i < retries-1 {
				time.Sleep(delay)
				continue
			}
			return fmt.Errorf("failed to open channel after %d attempts: %w", retries, err)
		}

		err = channel.ExchangeDeclare(
			USER_EVENTS_EXCHANGE,
			"topic",
			true,  // durable
			false, // auto-deleted
			false, // internal
			false, // no-wait
			nil,   // arguments
		)
		if err != nil {
			channel.Close()
			conn.Close()
			log.Printf("Failed to declare exchange (attempt %d): %v", i+1, err)
			if i < retries-1 {
				time.Sleep(delay)
				continue
			}
			return fmt.Errorf("failed to declare exchange after %d attempts: %w", retries, err)
		}

		r.connection = conn
		r.channel = channel
		log.Println("Successfully connected to RabbitMQ")
		return nil
	}

	return fmt.Errorf("failed to connect to RabbitMQ after %d attempts", retries)
}

func (r *RabbitMQService) StartListening() error {
	if r.channel == nil {
		return fmt.Errorf("RabbitMQ channel is not initialized")
	}

	queue, err := r.channel.QueueDeclare(
		USER_REPLICA_QUEUE,
		true,  // durable
		false, // delete when unused
		false, // exclusive
		false, // no-wait
		nil,   // arguments
	)
	if err != nil {
		return fmt.Errorf("failed to declare queue: %w", err)
	}

	err = r.channel.QueueBind(
		queue.Name,
		USER_PATTERN,
		USER_EVENTS_EXCHANGE,
		false, // no-wait
		nil,   // arguments
	)
	if err != nil {
		return fmt.Errorf("failed to bind queue: %w", err)
	}

	messages, err := r.channel.Consume(
		queue.Name,
		"",    // consumer
		false, // auto-ack
		false, // exclusive
		false, // no-local
		false, // no-wait
		nil,   // args
	)
	if err != nil {
		return fmt.Errorf("failed to register consumer: %w", err)
	}

	log.Printf("Waiting for messages in %s", USER_REPLICA_QUEUE)
	log.Printf("Listening for user events: %s, %s, %s", USER_CREATED_KEY, USER_UPDATED_KEY, USER_DELETED_KEY)

	go func() {
		for msg := range messages {
			err := r.processMessage(msg)
			if err != nil {
				log.Printf("Error processing message: %v", err)
				// Reject and requeue the message
				msg.Nack(false, true)
			} else {
				// Acknowledge the message
				msg.Ack(false)
			}
		}
	}()

	return nil
}

func (r *RabbitMQService) processMessage(msg amqp.Delivery) error {
	log.Printf("Received message with routing key %s: %s", msg.RoutingKey, string(msg.Body))

	var eventMessage UserEventMessage
	err := json.Unmarshal(msg.Body, &eventMessage)
	if err != nil {
		return fmt.Errorf("failed to unmarshal message: %w", err)
	}

	userData := eventMessage.Data
	log.Printf("Processing %s event for user %s", msg.RoutingKey, userData.PublicID)

	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	switch msg.RoutingKey {
	case USER_CREATED_KEY, USER_UPDATED_KEY:
		user := &models.User{
			PublicID: userData.PublicID,
			Roles:    userData.Roles,
		}
		err = r.userRepository.CreateOrUpdate(ctx, user)
		if err != nil {
			return fmt.Errorf("failed to create/update user %s: %w", userData.PublicID, err)
		}
		log.Printf("User %s processed successfully", userData.PublicID)

	case USER_DELETED_KEY:
		err = r.userRepository.Delete(ctx, userData.PublicID)
		if err != nil && err != repository.ErrNotFound {
			return fmt.Errorf("failed to delete user %s: %w", userData.PublicID, err)
		}
		log.Printf("User %s deleted successfully", userData.PublicID)

	default:
		log.Printf("Unknown routing key: %s", msg.RoutingKey)
	}

	return nil
}

func (r *RabbitMQService) Close() {
	if r.channel != nil {
		r.channel.Close()
	}
	if r.connection != nil {
		r.connection.Close()
	}
	log.Println("RabbitMQ connection closed")
}
