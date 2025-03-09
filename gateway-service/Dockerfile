# build stage
FROM golang:1.24-alpine3.21 AS builder
WORKDIR /app
COPY . .
RUN go build -o gateway-service

# run stage
FROM alpine:3.21
WORKDIR /app
COPY --from=builder /app/gateway-service .

EXPOSE 80
ENTRYPOINT ["./gateway-service"]