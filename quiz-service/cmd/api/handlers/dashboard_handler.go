package handlers

import (
	"strconv"

	"github.com/gin-gonic/gin"
	"protu.ai/quiz-service/internal/dto/response"
	"protu.ai/quiz-service/internal/service"
	"protu.ai/quiz-service/pkg/errors"
	apiResponse "protu.ai/quiz-service/pkg/response"
)

type DashboardHandler struct {
	dashboardService *service.DashboardService
	defaultPageSize  int
	maxPageSize      int
}

func NewDashboardHandler(dashboardService *service.DashboardService) *DashboardHandler {
	return &DashboardHandler{
		dashboardService: dashboardService,
		defaultPageSize:  10,
		maxPageSize:      100,
	}
}

func (h *DashboardHandler) GetDashboardSummary(c *gin.Context) {
	userID, exists := c.Get("userID")
	if !exists {
		apiResponse.Error(c, errors.AuthenticationError("User ID not found in token"))
		return
	}

	userIDStr, ok := userID.(string)
	if !ok {
		apiResponse.Error(c, errors.AuthenticationError("Invalid user ID format"))
		return
	}

	dashboardSummary, err := h.dashboardService.GetDashboardSummary(c, userIDStr)
	if err != nil {
		apiResponse.Error(c, errors.InternalError("Failed to retrieve dashboard summary: "+err.Error()))
		return
	}

	summaryResponse := response.DashboardSummaryResponse{
		TotalQuizzes: dashboardSummary.TotalQuizzes,
		AverageScore: dashboardSummary.AverageScore,
		SuccessRate:  dashboardSummary.SuccessRate,
	}

	apiResponse.OK(c, "Dashboard summary retrieved successfully", summaryResponse)
}

func (h *DashboardHandler) GetPassedQuizzes(c *gin.Context) {
	filterOptions := h.getFilterOptions(c)

	userID, exists := c.Get("userID")
	if !exists {
		apiResponse.Error(c, errors.AuthenticationError("User ID not found in token"))
		return
	}

	userIDStr, ok := userID.(string)
	if !ok {
		apiResponse.Error(c, errors.AuthenticationError("Invalid user ID format"))
		return
	}

	quizzesList, err := h.dashboardService.GetPassedQuizzes(c, userIDStr, filterOptions)
	if err != nil {
		apiResponse.Error(c, errors.InternalError("Failed to retrieve passed quizzes: "+err.Error()))
		return
	}

	quizResponses := make([]response.QuizSummaryResponse, 0, len(quizzesList.Quizzes))
	for _, quiz := range quizzesList.Quizzes {
		quizResponses = append(quizResponses, mapQuizToSummaryResponse(quiz))
	}

	pagination := response.PaginationMetadata{
		CurrentPage: quizzesList.Pagination.CurrentPage,
		PageSize:    quizzesList.Pagination.PageSize,
		TotalItems:  quizzesList.Pagination.TotalItems,
		TotalPages:  quizzesList.Pagination.TotalPages,
	}

	quizzesResponse := response.QuizListResponse{
		Quizzes:    quizResponses,
		Pagination: pagination,
	}

	apiResponse.OK(c, "Passed quizzes retrieved successfully", quizzesResponse)
}

func (h *DashboardHandler) GetFailedQuizzes(c *gin.Context) {
	filterOptions := h.getFilterOptions(c)

	userID, exists := c.Get("userID")
	if !exists {
		apiResponse.Error(c, errors.AuthenticationError("User ID not found in token"))
		return
	}

	userIDStr, ok := userID.(string)
	if !ok {
		apiResponse.Error(c, errors.AuthenticationError("Invalid user ID format"))
		return
	}

	quizzesList, err := h.dashboardService.GetFailedQuizzes(c, userIDStr, filterOptions)
	if err != nil {
		apiResponse.Error(c, errors.InternalError("Failed to retrieve failed quizzes: "+err.Error()))
		return
	}

	quizResponses := make([]response.QuizSummaryResponse, 0, len(quizzesList.Quizzes))
	for _, quiz := range quizzesList.Quizzes {
		quizResponses = append(quizResponses, mapQuizToSummaryResponse(quiz))
	}

	pagination := response.PaginationMetadata{
		CurrentPage: quizzesList.Pagination.CurrentPage,
		PageSize:    quizzesList.Pagination.PageSize,
		TotalItems:  quizzesList.Pagination.TotalItems,
		TotalPages:  quizzesList.Pagination.TotalPages,
	}

	quizzesResponse := response.QuizListResponse{
		Quizzes:    quizResponses,
		Pagination: pagination,
	}

	apiResponse.OK(c, "Failed quizzes retrieved successfully", quizzesResponse)
}

func (h *DashboardHandler) GetDraftQuizzes(c *gin.Context) {
	filterOptions := h.getFilterOptions(c)

	userID, exists := c.Get("userID")
	if !exists {
		apiResponse.Error(c, errors.AuthenticationError("User ID not found in token"))
		return
	}

	userIDStr, ok := userID.(string)
	if !ok {
		apiResponse.Error(c, errors.AuthenticationError("Invalid user ID format"))
		return
	}

	quizzesList, err := h.dashboardService.GetDraftQuizzes(c, userIDStr, filterOptions)
	if err != nil {
		apiResponse.Error(c, errors.InternalError("Failed to retrieve draft quizzes: "+err.Error()))
		return
	}

	quizResponses := make([]response.QuizSummaryResponse, 0, len(quizzesList.Quizzes))
	for _, quiz := range quizzesList.Quizzes {
		quizResponses = append(quizResponses, mapQuizToSummaryResponse(quiz))
	}

	pagination := response.PaginationMetadata{
		CurrentPage: quizzesList.Pagination.CurrentPage,
		PageSize:    quizzesList.Pagination.PageSize,
		TotalItems:  quizzesList.Pagination.TotalItems,
		TotalPages:  quizzesList.Pagination.TotalPages,
	}

	quizzesResponse := response.QuizListResponse{
		Quizzes:    quizResponses,
		Pagination: pagination,
	}

	apiResponse.OK(c, "Draft quizzes retrieved successfully", quizzesResponse)
}

func (h *DashboardHandler) getFilterOptions(c *gin.Context) service.FilterOptions {
	page, err := strconv.Atoi(c.DefaultQuery("page", "1"))
	if err != nil || page < 1 {
		page = 1
	}

	pageSize, err := strconv.Atoi(c.DefaultQuery("pageSize", strconv.Itoa(h.defaultPageSize)))
	if err != nil || pageSize < 1 {
		pageSize = h.defaultPageSize
	} else if pageSize > h.maxPageSize {
		pageSize = h.maxPageSize
	}

	sortBy := c.DefaultQuery("sortBy", "dateTaken")
	sortOrder := c.DefaultQuery("sortOrder", "desc")

	if sortOrder != "asc" && sortOrder != "desc" {
		sortOrder = "desc"
	}

	validSortFields := map[string]bool{
		"dateTaken": true,
		"score":     true,
		"title":     true,
		"topic":     true,
	}

	if !validSortFields[sortBy] {
		sortBy = "dateTaken"
	}

	topic := c.Query("topic")

	return service.FilterOptions{
		Page:      page,
		PageSize:  pageSize,
		SortBy:    sortBy,
		SortOrder: sortOrder,
		Topic:     topic,
	}
}

func mapQuizToSummaryResponse(quiz service.QuizCard) response.QuizSummaryResponse {
	return response.QuizSummaryResponse{
		ID:        quiz.ID,
		Title:     quiz.Title,
		Score:     quiz.Score,
		DateTaken: quiz.DateTaken,
		Topic:     quiz.Topic,
		TimeTaken: quiz.TimeTaken,
	}
}
