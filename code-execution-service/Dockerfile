# build stage
FROM golang:1.24-alpine3.21 AS builder
WORKDIR /app

COPY . .

RUN go mod tidy && \
    CGO_ENABLED=0 GOOS=linux go build -o code-execution-service main.go

# run stage
FROM alpine:3.21
WORKDIR /app

COPY --from=builder /app/code-execution-service .
COPY .env .

EXPOSE 8086

CMD [ "/app/code-execution-service" ]
