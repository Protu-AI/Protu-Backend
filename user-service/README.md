# User Service

## 📑 Table of Contents
- [Key Features](#key-features)
- [API Reference](#api-reference)
- [Getting Started](#getting-started)
- [Documentation](#documentation)

## ⭐ Key Features

- 🔑 **User Authentication**
  - JWT-based authentication system
  - Secure login and registration flows
  - Token refresh mechanism

- 👤 **Account Management**
  - Email verification system
  - Password recovery workflow
  - Profile management
  - Account deactivation capabilities

- 🛡️ **Security**
  - JWT token-based security
  - Email verification
  - Secure password reset flow
    
## 🛠️ Technology Stack

- **Backend Framework**: Spring Boot 3.4.0 🍃
- **Language**: Java 17 ☕
- **Database**: PostgreSQL 🐘
- **Containerization**: Docker & Docker Compose 🐳
- **Build Tool**: Maven 📦
- **API Documentation**: OpenAPI (SpringDoc) 📚

## 🔌API Reference

### 🔒 Authentication Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/sign-up` | Register new user |
| POST | `/api/v1/auth/verify-email` | Verify email address |
| POST | `/api/v1/auth/sign-in` | Authenticate user |
| POST | `/api/v1/auth/validate-identifier` | Validate email/username |
| POST | `/api/v1/auth/forgot-password` | Request password reset |
| POST | `/api/v1/auth/reset-password` | Reset password |
| POST | `/api/v1/auth/refresh` | Refresh access token |

### 👥 User Management Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/users/{id}` | Get user details |
| PUT | `/api/v1/users/{id}` | Update full profile |
| PATCH | `/api/v1/users/{id}` | Update partial profile |
| PATCH | `/api/v1/users/{id}/deactivate` | Deactivate account |
| POST | `/api/v1/auth/send-verification-code` | Resend verification |

## 🚀 Getting Started

### 📋 Prerequisites
- Docker and Docker Compose

### ⚙️ Environment Setup

1. Create a `.env` file in the project root with the following variables:
```env
  SPRING_DATASOURCE_URL=your_jdbc_url
  SPRING_DATASOURCE_USERNAME=your_postgres_username
  SPRING_DATASOURCE_PASSWORD=your_postgres_password
  POSTGRES_USER=your_postgres_username
  POSTGRES_PASSWORD=your_postgres_password
  POSTGRES_DB=protu-db
  JWT_SECRET=your_jwt_secret
  JWT_ACCESS_TOKEN_EXPIRATION_TIME=access_token_expiry_time
  JWT_REFRESH_TOKEN_EXPIRATION_TIME=refresh_token_expiry_time
```

3. Navigate to the project directory
4. Run the following command:
```bash
   docker-compose up --build
```
6. Service will be available at `http://localhost:8085`

## 📚 Documentation

- **API Documentation**: Available at `http://localhost:8085/swagger-ui/index.html` when the service is running
- **Postman Collection**: Available in the `src/main/resources/postman` directory
