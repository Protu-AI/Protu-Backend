package config

import (
	"github.com/spf13/viper"
)

type Config struct {
	// MongoDB configuration
	MongoURI     string `mapstructure:"MONGO_URI"`
	MongoDB      string `mapstructure:"MONGO_DB"`
	MongoTimeout int    `mapstructure:"MONGO_TIMEOUT"`

	// Server configuration
	HTTPServerAddress string `mapstructure:"HTTP_SERVER_ADDRESS"`

	// AI Service connection
	AIBaseURL string `mapstructure:"AI_BASE_URL"`

	// Authentication configuration
	JWTSecret string `mapstructure:"JWT_SECRET"`

	// RabbitMQ configuration
	RabbitMQURL string `mapstructure:"RABBITMQ_URL"`
}

func LoadConfig(path string) (config Config, err error) {
	viper.AddConfigPath(path)
	viper.SetConfigName(".env")
	viper.SetConfigType("env")

	viper.AutomaticEnv()

	err = viper.ReadInConfig()
	if err != nil {
		return
	}

	err = viper.Unmarshal(&config)
	return
}
