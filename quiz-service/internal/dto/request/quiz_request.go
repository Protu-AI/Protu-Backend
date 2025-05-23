package request

type QuizStage1Request struct {
	Prompt            string   `json:"prompt" binding:"required"`
	DifficultyLevel   string   `json:"difficultyLevel" binding:"required"`
	NumberOfQuestions int      `json:"numberOfQuestions" binding:"required"`
	QuestionTypes     []string `json:"questionTypes" binding:"required"`
	TimeLimit         int      `json:"timeLimit"`
}

type QuizStage2Request struct {
	QuizID              string   `json:"quizId" binding:"required"`
	Subtopics           []string `json:"subtopics" binding:"required"`
	AdditionalSubtopics []string `json:"additional_subtopics"`
	AdditionalPrefs     string   `json:"additionalPrefs"`
}
