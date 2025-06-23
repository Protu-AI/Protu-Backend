package handlers

import (
	"github.com/gin-gonic/gin"
	"go.mongodb.org/mongo-driver/bson/primitive"
	"protu.ai/quiz-service/internal/dto/request"
	"protu.ai/quiz-service/internal/dto/response"
	"protu.ai/quiz-service/internal/middleware"
	"protu.ai/quiz-service/internal/models"
	"protu.ai/quiz-service/internal/service"
	"protu.ai/quiz-service/pkg/errors"
	apiResponse "protu.ai/quiz-service/pkg/response"
	"protu.ai/quiz-service/pkg/validator"
)

type AttemptHandler struct {
	attemptService *service.AttemptService
	quizService    *service.QuizService
	aiService      *service.AIService
}

func NewAttemptHandler(attemptService *service.AttemptService, quizService *service.QuizService, aiService *service.AIService) *AttemptHandler {
	return &AttemptHandler{
		attemptService: attemptService,
		quizService:    quizService,
		aiService:      aiService,
	}
}

func (h *AttemptHandler) PreviewQuiz(c *gin.Context) {
	quizID := c.Param("quizId")
	if quizID == "" {
		apiResponse.Error(c, errors.BadRequestError("Quiz ID is required", nil))
		return
	}

	quiz, err := h.quizService.GetQuizByID(c, quizID)
	if err != nil {
		apiResponse.Error(c, errors.NotFoundError("Quiz not found"))
		return
	}

	previewResponse := response.QuizPreviewResponse{
		ID:                quiz.ID.Hex(),
		Title:             quiz.Title,
		Topic:             quiz.Topic,
		DifficultyLevel:   quiz.DifficultyLevel,
		NumberOfQuestions: quiz.NumberOfQuestions,
		TimeLimit:         quiz.TimeLimit,
		CreatedAt:         quiz.CreatedAt,
	}

	apiResponse.QuizFound(c, previewResponse)
}

func (h *AttemptHandler) StartQuiz(c *gin.Context) {

	quizID := c.Param("quizId")
	if quizID == "" {
		apiResponse.Error(c, errors.BadRequestError("Quiz ID is required", nil))
		return
	}

	userID, err := middleware.GetUserIDFromContext(c)
	if err != nil {
		apiResponse.Error(c, errors.AuthenticationError("Failed to get user ID from token"))
		return
	}

	quiz, err := h.quizService.GetQuizByID(c, quizID)
	if err != nil {
		apiResponse.Error(c, errors.NotFoundError("Quiz not found"))
		return
	}

	quizObjectID, err := primitive.ObjectIDFromHex(quizID)
	if err != nil {
		apiResponse.Error(c, errors.BadRequestError("Invalid quiz ID format", nil))
		return
	}

	attempt := &models.QuizAttempt{
		QuizID: quizObjectID,
		UserID: userID,
		Status: models.AttemptStatusInProgress,
	}

	createdAttempt, err := h.attemptService.CreateAttempt(c, attempt)
	if err != nil {
		apiResponse.Error(c, errors.QuizExecutionError("Failed to create attempt", err.Error()))
		return
	}

	if quiz.Status == models.QuizStatusDraft || quiz.Status == models.QuizStatusDraftStage1 {
		_, err := h.quizService.PublishQuiz(c, quizID)
		if err != nil {
		}
	}

	if err := h.quizService.IncrementAttemptCount(c, quizID); err != nil {
	}

	questions := make([]response.QuestionDetail, 0, len(quiz.Questions))

	for _, q := range quiz.Questions {
		options := make([]response.Option, 0, len(q.Options))
		for _, opt := range q.Options {
			options = append(options, response.Option{
				Text: opt.Text,
			})
		}

		questions = append(questions, response.QuestionDetail{
			ID:           q.ID.Hex(),
			QuestionText: q.QuestionText,
			QuestionType: q.QuestionType,
			Options:      options,
			Order:        q.Order,
		})
	}

	startResponse := response.QuizStartResponse{
		AttemptID: createdAttempt.ID.Hex(),
		QuizID:    quiz.ID.Hex(),
		Title:     quiz.Title,
		TimeLimit: quiz.TimeLimit,
		Questions: questions,
		StartedAt: createdAttempt.StartedAt,
	}

	apiResponse.AttemptCreated(c, startResponse)
}

func (h *AttemptHandler) SubmitAttempt(c *gin.Context) {
	attemptID := c.Param("id")
	if attemptID == "" {
		apiResponse.Error(c, errors.BadRequestError("Attempt ID is required", nil))
		return
	}

	var submitReq request.SubmitAttemptRequest
	if err := c.ShouldBindJSON(&submitReq); err != nil {
		apiResponse.Error(c, errors.ValidationError("Invalid request body", err.Error()))
		return
	}

	if err := validator.Validate(submitReq); err != nil {
		apiResponse.Error(c, errors.ValidationError("Invalid input data", err.Error()))
		return
	}

	attempt, err := h.attemptService.GetAttemptByID(c, attemptID)
	if err != nil {
		apiResponse.Error(c, errors.NotFoundError("Attempt not found"))
		return
	}

	if attempt.Status == models.AttemptStatusCompleted {
		apiResponse.Error(c, errors.BadRequestError("Attempt has already been submitted", nil))
		return
	}

	quiz, err := h.quizService.GetQuizByID(c, attempt.QuizID.Hex())
	if err != nil {
		apiResponse.Error(c, errors.InternalError("Failed to retrieve quiz: "+err.Error()))
		return
	}

	if submitReq.QuizID != "" && submitReq.QuizID != quiz.ID.Hex() {
		apiResponse.Error(c, errors.ValidationError("Quiz ID mismatch", nil))
		return
	}

	questionMap := make(map[string]*models.Question, len(quiz.Questions))
	for i := range quiz.Questions {
		questionMap[quiz.Questions[i].ID.Hex()] = &quiz.Questions[i]
	}

	answers := make([]models.Answer, 0, len(submitReq.Answers))
	for _, ans := range submitReq.Answers {
		questionID, err := primitive.ObjectIDFromHex(ans.QuestionID)
		if err != nil {
			apiResponse.Error(c, errors.ValidationError("Invalid question ID format", ans.QuestionID))
			return
		}

		question, exists := questionMap[ans.QuestionID]
		if !exists {
			apiResponse.Error(c, errors.NotFoundError("Question not found in quiz: "+ans.QuestionID))
			return
		}

		if ans.Selected < 0 || ans.Selected >= len(question.Options) {
			apiResponse.Error(c, errors.ValidationError("Invalid option index for question", ans.QuestionID))
			return
		}

		answers = append(answers, models.Answer{
			QuestionID: questionID,
			Selected:   ans.Selected,
		})
	}

	submittedAttempt, err := h.attemptService.SubmitAttempt(c, attemptID, answers)
	if err != nil {
		apiResponse.Error(c, errors.QuizSubmissionError("Failed to submit attempt", err.Error()))
		return
	}

	var aiFeedback string
	feedback, err := h.aiService.GetQuizFeedback(c, quiz, answers)
	if err != nil {
		aiFeedback = "AI feedback is currently unavailable."
	} else {
		aiFeedback = feedback
	}

	reviewResponse := response.QuizReviewResponse{
		AttemptID:   submittedAttempt.ID.Hex(),
		QuizID:      submittedAttempt.QuizID.Hex(),
		QuizTitle:   quiz.Title,
		QuizTopic:   quiz.Topic,
		Score:       submittedAttempt.Score,
		Passed:      submittedAttempt.Passed,
		TimeTaken:   submittedAttempt.TimeTaken,
		CompletedAt: submittedAttempt.CompletedAt,
		AIFeedback:  aiFeedback,
	}

	apiResponse.AttemptSubmitted(c, reviewResponse)
}
