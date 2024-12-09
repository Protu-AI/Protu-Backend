# 👤 User Service

## 🌟 Overview

- The **User Service** is a RESTful API that handles user management, including registration, authentication, profile
  updates, and deactivation.
- The service uses JWT-based authentication to secure endpoints.

## ✨ Features

- **User Registration**: Create a new account.
- **Login**: Authenticate and retrieve tokens.
- **Token Refresh**: Renew expired access tokens.
- **User Profile Management**: View and update user details.
- **Account Deactivation**: Disable user accounts.

## 💻 Technology Stack

- Java 22 ☕
- Spring Boot 🍃
- Docker & Docker Compose 🐳
- PostgreSQL 🐘.
- Maven 📦

## 🔗 Endpoints Overview

### Authentication and Token Management

- **Register**: `POST /api/v1/users/register`
- **Login**: `POST /api/v1/users/login`
- **Refresh Token**: `POST /api/v1/users/refresh`

### User Management

- **Get User**: `GET /api/v1/users/{id}`
- **Update User**: `PUT /api/v1/users/{id}`
- **Deactivate User**: `PATCH /api/v1/users/{id}/deactivate`

## ⚙️ Running the Service

### Prerequisites

- Docker and Docker Compose installed.

### Steps

1. Create an `.env` file in user-service directory and add your credentials:
   ```env
    SPRING_DATASOURCE_URL=your_jdbc_url
    SPRING_DATASOURCE_USERNAME=your_postgres_username
    SPRING_DATASOURCE_PASSWORD=your_postgres_password
    POSTGRES_USER=your_postgres_username
    POSTGRES_PASSWORD=your_postgres_password
    POSTGRES_DB=protu-db
    JWT_SECRET=your_jwt_secret
    JWT_ACCESS_TOKEN_EXPIRATION_TIME=access_token_expriy_time
    JWT_REFRESH_TOKEN_EXPIRATION_TIME=refresh_token_expriy_time
   ```
2. Build and run the service:
   ```bash
    docker-compose up --build
   ```

## 📖 API Documentation

### Postman Collection

- Download and import the Postman collection to interact with the API:
  [User Service Postman Collection](src/main/resources/postman/user-service-v1.0.0.postman_collection.json)


