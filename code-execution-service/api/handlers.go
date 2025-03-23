package api

import (
	"encoding/base64"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"os"
	"strings"

	"github.com/gin-gonic/gin"
)

type ExecutionRequest struct {
	SourceCode string `json:"source_code" binding:"required"`
	Language   string `json:"language" binding:"required"`
	Input      string `json:"input,omitempty"`
}

type ExecutionResponse struct {
	Stdout     string `json:"stdout"`
	Stderr     string `json:"stderr"`
	StatusCode int    `json:"status_code"`
	Time       string `json:"time"`
	Memory     int    `json:"memory"`
}

func HandleCodeExecution(c *gin.Context) {
	var req ExecutionRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	response, err := executeCode(req)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, response)
}

func executeCode(req ExecutionRequest) (*ExecutionResponse, error) {
	apiKey := os.Getenv("RAPIDAPI_KEY")
	if apiKey == "" {
		return nil, fmt.Errorf("RapidAPI key not found")
	}

	sourceCode := base64.StdEncoding.EncodeToString([]byte(req.SourceCode))
	stdin := base64.StdEncoding.EncodeToString([]byte(req.Input))

	payload := strings.NewReader(fmt.Sprintf(`{
		"language_id": %d,
		"source_code": "%s",
		"stdin": "%s"
	}`, getLanguageID(req.Language), sourceCode, stdin))

	url := "https://judge0-ce.p.rapidapi.com/submissions?base64_encoded=true&wait=true&fields=*"
	httpReq, err := http.NewRequest("POST", url, payload)
	if err != nil {
		return nil, err
	}

	httpReq.Header.Add("Content-Type", "application/json")
	httpReq.Header.Add("X-RapidAPI-Key", apiKey)
	httpReq.Header.Add("X-RapidAPI-Host", "judge0-ce.p.rapidapi.com")

	res, err := http.DefaultClient.Do(httpReq)
	if err != nil {
		return nil, err
	}
	defer res.Body.Close()

	body, err := io.ReadAll(res.Body)
	if err != nil {
		return nil, err
	}

	var result ExecutionResponse
	if err := json.Unmarshal(body, &result); err != nil {
		return nil, err
	}

	if result.Stdout != "" {
		if decoded, err := base64.StdEncoding.DecodeString(result.Stdout); err == nil {
			result.Stdout = string(decoded)
		}
	}
	if result.Stderr != "" {
		if decoded, err := base64.StdEncoding.DecodeString(result.Stderr); err == nil {
			result.Stderr = string(decoded)
		}
	}

	return &result, nil
}

func getLanguageID(language string) int {
	languageMap := map[string]int{
		"python":     71,
		"java":       62,
		"cpp":        54,
		"javascript": 63,
		"go":         60,
	}

	id, ok := languageMap[language]
	if !ok {
		return 71
	}
	return id
}
