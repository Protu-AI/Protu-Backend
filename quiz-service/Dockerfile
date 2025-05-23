# build stage
FROM golang:1.24-alpine3.20 AS builder
WORKDIR /app

COPY . .

RUN go mod tidy && \
    CGO_ENABLED=0 GOOS=linux go build -o quiz-service cmd/main.go

# run stage
FROM alpine:3.20
WORKDIR /app

COPY --from=builder /app/quiz-service .
COPY .env .

EXPOSE 8089

CMD [ "/app/quiz-service" ]
