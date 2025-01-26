# User Service

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

## 🚀 Getting Started

### 📋 Prerequisites
- Docker and Docker Compose are installed in your machine.

### ⚙️ Environment Setup

#### 💻 Local Run

1. Create `application-local.yaml` file in the following path:`src/main/resources`
2. Copy the properties in template file in the same path `application-local-template.yaml` into the new file
3. Replace all placeholders with your actual credentials

#### 🐋 Docker Run

1. Copy the `.env.sample` file in the root directory of the user-service to create your `.env` file:
   ```bash
      cp .env.sample .env
   ```
2. Open the .env file and replace the placeholder values with your actual configuration values.
3. Navigate to the `Docker` directory and run the following command:
   ```bash 
      docker compose up --build -d
   ```
4. Service will be available at `http://localhost:8085`


## 📚 Documentation

- **API Documentation**: Available at `http://localhost:8085/swagger-ui/index.html` when the service is running
- **Postman Collection**: Available in the `src/main/resources/postman` directory
