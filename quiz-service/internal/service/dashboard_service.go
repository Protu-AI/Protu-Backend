package service

import (
	"context"
	"math"
	"sort"
	"strings"
	"sync"
	"time"

	"protu.ai/quiz-service/internal/models"
	"protu.ai/quiz-service/internal/repository"
)

type DashboardService struct {
	quizRepo    *repository.QuizRepository
	attemptRepo *repository.AttemptRepository
}

type QuizCard struct {
	ID        string    `json:"id"`
	Title     string    `json:"title"`
	Topic     string    `json:"topic"`
	Score     float64   `json:"score"`
	DateTaken time.Time `json:"dateTaken"`
	TimeTaken int       `json:"timeTaken"`
	Category  string    `json:"-"`
}

type PaginationMetadata struct {
	CurrentPage int `json:"currentPage"`
	PageSize    int `json:"pageSize"`
	TotalItems  int `json:"totalItems"`
	TotalPages  int `json:"totalPages"`
}

type FilterOptions struct {
	Page      int    `json:"page"`
	PageSize  int    `json:"pageSize"`
	SortBy    string `json:"sortBy"`
	SortOrder string `json:"sortOrder"`
	Topic     string `json:"topic"`
}

type DashboardSummary struct {
	TotalQuizzes int     `json:"totalQuizzes"`
	AverageScore float64 `json:"averageScore"`
	SuccessRate  float64 `json:"successRate"`
}

type QuizList struct {
	Quizzes    []QuizCard         `json:"quizzes"`
	Pagination PaginationMetadata `json:"pagination"`
}

func NewDashboardService(
	quizRepo *repository.QuizRepository,
	attemptRepo *repository.AttemptRepository,
) *DashboardService {
	return &DashboardService{
		quizRepo:    quizRepo,
		attemptRepo: attemptRepo,
	}
}

func (s *DashboardService) GetDashboardSummary(ctx context.Context, userID string) (*DashboardSummary, error) {
	userData, err := s.fetchUserData(ctx, userID)
	if err != nil {
		return nil, err
	}

	stats := s.calculateStats(userData)

	return &DashboardSummary{
		TotalQuizzes: stats.totalQuizzes,
		AverageScore: stats.averageScore,
		SuccessRate:  stats.successRate,
	}, nil
}

func (s *DashboardService) GetPassedQuizzes(ctx context.Context, userID string, options FilterOptions) (*QuizList, error) {
	normalizePaginationOptions(&options)

	userData, err := s.fetchUserData(ctx, userID)
	if err != nil {
		return nil, err
	}

	quizCards := s.processQuizDataForPassedQuizzes(ctx, userData, options)

	sortedQuizzes := sortQuizCards(quizCards, options.SortBy, options.SortOrder)

	totalItems := len(sortedQuizzes)
	totalPages := int(math.Ceil(float64(totalItems) / float64(options.PageSize)))
	if totalPages == 0 {
		totalPages = 1
	}

	start := (options.Page - 1) * options.PageSize
	end := start + options.PageSize

	if start >= len(sortedQuizzes) {
		sortedQuizzes = []QuizCard{}
	} else if end > len(sortedQuizzes) {
		sortedQuizzes = sortedQuizzes[start:len(sortedQuizzes)]
	} else {
		sortedQuizzes = sortedQuizzes[start:end]
	}

	return &QuizList{
		Quizzes: sortedQuizzes,
		Pagination: PaginationMetadata{
			CurrentPage: options.Page,
			PageSize:    options.PageSize,
			TotalItems:  totalItems,
			TotalPages:  totalPages,
		},
	}, nil
}

func (s *DashboardService) GetFailedQuizzes(ctx context.Context, userID string, options FilterOptions) (*QuizList, error) {
	normalizePaginationOptions(&options)

	userData, err := s.fetchUserData(ctx, userID)
	if err != nil {
		return nil, err
	}

	quizCards := s.processQuizDataForFailedQuizzes(ctx, userData, options)

	sortedQuizzes := sortQuizCards(quizCards, options.SortBy, options.SortOrder)

	totalItems := len(sortedQuizzes)
	totalPages := int(math.Ceil(float64(totalItems) / float64(options.PageSize)))
	if totalPages == 0 {
		totalPages = 1
	}

	start := (options.Page - 1) * options.PageSize
	end := start + options.PageSize

	if start >= len(sortedQuizzes) {
		sortedQuizzes = []QuizCard{}
	} else if end > len(sortedQuizzes) {
		sortedQuizzes = sortedQuizzes[start:len(sortedQuizzes)]
	} else {
		sortedQuizzes = sortedQuizzes[start:end]
	}

	return &QuizList{
		Quizzes: sortedQuizzes,
		Pagination: PaginationMetadata{
			CurrentPage: options.Page,
			PageSize:    options.PageSize,
			TotalItems:  totalItems,
			TotalPages:  totalPages,
		},
	}, nil
}

func (s *DashboardService) GetDraftQuizzes(ctx context.Context, userID string, options FilterOptions) (*QuizList, error) {
	normalizePaginationOptions(&options)

	userData, err := s.fetchUserData(ctx, userID)
	if err != nil {
		return nil, err
	}

	quizCards := s.processQuizDataForDraftQuizzes(ctx, userData, options)

	sortedQuizzes := sortQuizCards(quizCards, options.SortBy, options.SortOrder)

	totalItems := len(sortedQuizzes)
	totalPages := int(math.Ceil(float64(totalItems) / float64(options.PageSize)))
	if totalPages == 0 {
		totalPages = 1
	}

	start := (options.Page - 1) * options.PageSize
	end := start + options.PageSize

	if start >= len(sortedQuizzes) {
		sortedQuizzes = []QuizCard{}
	} else if end > len(sortedQuizzes) {
		sortedQuizzes = sortedQuizzes[start:len(sortedQuizzes)]
	} else {
		sortedQuizzes = sortedQuizzes[start:end]
	}

	return &QuizList{
		Quizzes: sortedQuizzes,
		Pagination: PaginationMetadata{
			CurrentPage: options.Page,
			PageSize:    options.PageSize,
			TotalItems:  totalItems,
			TotalPages:  totalPages,
		},
	}, nil
}

func normalizePaginationOptions(options *FilterOptions) {
	if options.Page < 1 {
		options.Page = 1
	}
	if options.PageSize < 1 {
		options.PageSize = 10
	} else if options.PageSize > 100 {
		options.PageSize = 100
	}
}

type userData struct {
	passedAttempts []*models.QuizAttempt
	failedAttempts []*models.QuizAttempt
	draftQuizzes   []*models.Quiz
}

func (s *DashboardService) fetchUserData(ctx context.Context, userID string) (*userData, error) {
	type dataResult struct {
		passedAttempts []*models.QuizAttempt
		failedAttempts []*models.QuizAttempt
		draftQuizzes   []*models.Quiz
		err            error
	}

	resultChan := make(chan dataResult, 3)
	var wg sync.WaitGroup
	wg.Add(3)

	go func() {
		defer wg.Done()
		passedAttempts, err := s.attemptRepo.GetPassedAttemptsByUserID(ctx, userID)
		resultChan <- dataResult{passedAttempts: passedAttempts, err: err}
	}()

	go func() {
		defer wg.Done()
		failedAttempts, err := s.attemptRepo.GetFailedAttemptsByUserID(ctx, userID)
		resultChan <- dataResult{failedAttempts: failedAttempts, err: err}
	}()

	go func() {
		defer wg.Done()
		draftQuizzes, err := s.quizRepo.GetQuizzesByUserIDAndStatus(ctx, userID, "draft")
		resultChan <- dataResult{draftQuizzes: draftQuizzes, err: err}
	}()

	go func() {
		wg.Wait()
		close(resultChan)
	}()

	data := &userData{}
	for result := range resultChan {
		if result.err != nil {
			return nil, result.err
		}

		if result.passedAttempts != nil {
			data.passedAttempts = result.passedAttempts
		} else if result.failedAttempts != nil {
			data.failedAttempts = result.failedAttempts
		} else if result.draftQuizzes != nil {
			data.draftQuizzes = result.draftQuizzes
		}
	}

	return data, nil
}

type statsData struct {
	totalQuizzes int
	averageScore float64
	successRate  float64
}

// getBestAttemptPerQuiz groups attempts by quiz ID and returns only the highest scoring attempt for each quiz
// If there are multiple attempts with the same highest score, it returns the most recent one
func getBestAttemptPerQuiz(attempts []*models.QuizAttempt) []*models.QuizAttempt {
	quizAttemptMap := make(map[string]*models.QuizAttempt)

	for _, attempt := range attempts {
		quizID := attempt.QuizID.Hex()

		existingAttempt, exists := quizAttemptMap[quizID]

		if !exists {
			// First attempt for this quiz
			quizAttemptMap[quizID] = attempt
		} else if attempt.Score > existingAttempt.Score {
			// Higher score found
			quizAttemptMap[quizID] = attempt
		} else if attempt.Score == existingAttempt.Score && attempt.CompletedAt.After(existingAttempt.CompletedAt) {
			// Same score but more recent attempt
			quizAttemptMap[quizID] = attempt
		}
	}

	// Convert map back to slice
	bestAttempts := make([]*models.QuizAttempt, 0, len(quizAttemptMap))
	for _, attempt := range quizAttemptMap {
		bestAttempts = append(bestAttempts, attempt)
	}

	return bestAttempts
}

func (s *DashboardService) calculateStats(data *userData) statsData {
	stats := statsData{}

	// Get best attempts only - one per unique quiz
	allAttempts := append(data.passedAttempts, data.failedAttempts...)
	bestAttempts := getBestAttemptPerQuiz(allAttempts)

	// Separate best attempts into passed and failed
	bestPassedAttempts := make([]*models.QuizAttempt, 0)
	bestFailedAttempts := make([]*models.QuizAttempt, 0)

	for _, attempt := range bestAttempts {
		if attempt.Passed {
			bestPassedAttempts = append(bestPassedAttempts, attempt)
		} else {
			bestFailedAttempts = append(bestFailedAttempts, attempt)
		}
	}

	totalBestAttempts := len(bestAttempts)
	stats.totalQuizzes = totalBestAttempts + len(data.draftQuizzes)

	if totalBestAttempts > 0 {
		totalScore := 0.0
		for _, attempt := range bestAttempts {
			totalScore += attempt.Score
		}
		stats.averageScore = totalScore / float64(totalBestAttempts)
		stats.successRate = float64(len(bestPassedAttempts)) / float64(totalBestAttempts) * 100
	}

	return stats
}

func (s *DashboardService) processQuizDataForPassedQuizzes(ctx context.Context, data *userData, options FilterOptions) []QuizCard {
	quizCache := make(map[string]*models.Quiz)

	// Get all attempts and filter to best attempts only
	allAttempts := append(data.passedAttempts, data.failedAttempts...)
	bestAttempts := getBestAttemptPerQuiz(allAttempts)

	// Filter to only passed best attempts
	bestPassedAttempts := make([]*models.QuizAttempt, 0)
	for _, attempt := range bestAttempts {
		if attempt.Passed {
			bestPassedAttempts = append(bestPassedAttempts, attempt)
		}
	}

	return filterQuizAttempts(bestPassedAttempts, s.quizRepo, ctx, options, quizCache, "passed")
}

func (s *DashboardService) processQuizDataForFailedQuizzes(ctx context.Context, data *userData, options FilterOptions) []QuizCard {
	quizCache := make(map[string]*models.Quiz)

	// Get all attempts and filter to best attempts only
	allAttempts := append(data.passedAttempts, data.failedAttempts...)
	bestAttempts := getBestAttemptPerQuiz(allAttempts)

	// Filter to only failed best attempts
	bestFailedAttempts := make([]*models.QuizAttempt, 0)
	for _, attempt := range bestAttempts {
		if !attempt.Passed {
			bestFailedAttempts = append(bestFailedAttempts, attempt)
		}
	}

	return filterQuizAttempts(bestFailedAttempts, s.quizRepo, ctx, options, quizCache, "failed")
}

func (s *DashboardService) processQuizDataForDraftQuizzes(ctx context.Context, data *userData, options FilterOptions) []QuizCard {
	return filterDraftQuizzes(data.draftQuizzes, options)
}

func sortQuizCards(cards []QuizCard, sortBy, sortOrder string) []QuizCard {
	switch sortBy {
	case "score":
		sort.Slice(cards, func(i, j int) bool {
			if sortOrder == "asc" {
				return cards[i].Score < cards[j].Score
			}
			return cards[i].Score > cards[j].Score
		})
	case "title":
		sort.Slice(cards, func(i, j int) bool {
			if sortOrder == "asc" {
				return strings.ToLower(cards[i].Title) < strings.ToLower(cards[j].Title)
			}
			return strings.ToLower(cards[i].Title) > strings.ToLower(cards[j].Title)
		})
	case "topic":
		sort.Slice(cards, func(i, j int) bool {
			if sortOrder == "asc" {
				return strings.ToLower(cards[i].Topic) < strings.ToLower(cards[j].Topic)
			}
			return strings.ToLower(cards[i].Topic) > strings.ToLower(cards[j].Topic)
		})
	default: // dateTaken
		sort.Slice(cards, func(i, j int) bool {
			if sortOrder == "asc" {
				return cards[i].DateTaken.Before(cards[j].DateTaken)
			}
			return cards[i].DateTaken.After(cards[j].DateTaken)
		})
	}

	return cards
}

func filterQuizAttempts(
	attempts []*models.QuizAttempt,
	quizRepo *repository.QuizRepository,
	ctx context.Context,
	options FilterOptions,
	quizCache map[string]*models.Quiz,
	category string,
) []QuizCard {
	var cards []QuizCard

	for _, attempt := range attempts {
		quizID := attempt.QuizID.Hex()
		quiz, err := getQuizWithCache(quizID, quizRepo, ctx, quizCache)
		if err != nil {
			continue
		}

		if options.Topic != "" && !strings.EqualFold(quiz.Topic, options.Topic) {
			continue
		}

		cards = append(cards, QuizCard{
			ID:        quizID,
			Title:     quiz.Title,
			Topic:     quiz.Topic,
			Score:     attempt.Score,
			DateTaken: attempt.CompletedAt,
			TimeTaken: attempt.TimeTaken,
			Category:  category,
		})
	}

	return cards
}

func getQuizWithCache(quizID string, quizRepo *repository.QuizRepository, ctx context.Context, quizCache map[string]*models.Quiz) (*models.Quiz, error) {
	if quiz, exists := quizCache[quizID]; exists {
		return quiz, nil
	}

	quiz, err := quizRepo.GetQuizByID(ctx, quizID)
	if err != nil {
		return nil, err
	}

	quizCache[quizID] = quiz
	return quiz, nil
}

func filterDraftQuizzes(quizzes []*models.Quiz, options FilterOptions) []QuizCard {
	var cards []QuizCard

	for _, quiz := range quizzes {
		if options.Topic != "" && !strings.EqualFold(quiz.Topic, options.Topic) {
			continue
		}

		cards = append(cards, QuizCard{
			ID:        quiz.ID.Hex(),
			Title:     quiz.Title,
			Topic:     quiz.Topic,
			Score:     0,
			DateTaken: quiz.CreatedAt,
			TimeTaken: 0,
			Category:  "draft",
		})
	}

	return cards
}
