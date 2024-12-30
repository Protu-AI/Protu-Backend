-- CreateEnum
CREATE TYPE "enum_messages_senderRole" AS ENUM ('user', 'model');

-- CreateTable
CREATE TABLE "users" (
    "id" SERIAL PRIMARY KEY,
    "first_name" VARCHAR(50) NOT NULL,
    "last_name" VARCHAR(50) NOT NULL,
    "username" VARCHAR(50) NOT NULL UNIQUE,
    "email" VARCHAR(100) NOT NULL UNIQUE,
    "password" VARCHAR(100) NOT NULL,
    "phone_number" VARCHAR(20) NOT NULL,
    "authorities" TEXT NOT NULL,
    "is_active" BOOLEAN NOT NULL DEFAULT TRUE,
    "is_email_verified" BOOLEAN NOT NULL,
    "verification_code" VARCHAR NOT NULL,
    "code_expiry_date" TIMESTAMPTZ NOT NULL,
    "createdAt" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- CreateTable
CREATE TABLE "chats" (
    "id" SERIAL PRIMARY KEY,
    "userId" INTEGER NOT NULL,
    "name" VARCHAR(50) NOT NULL,
    "createdAt" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "chats_userId_fkey" FOREIGN KEY ("userId") REFERENCES "users"("id") ON DELETE CASCADE ON UPDATE NO ACTION
);

-- CreateTable
CREATE TABLE "messages" (
    "id" SERIAL PRIMARY KEY,
    "chatId" INTEGER NOT NULL,
    "senderRole" "enum_messages_senderRole" NOT NULL,
    "content" TEXT NOT NULL,
    "attachmentName" VARCHAR(255) NOT NULL,
    "createdAt" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "messages_chatId_fkey" FOREIGN KEY ("chatId") REFERENCES "chats"("id") ON DELETE CASCADE ON UPDATE NO ACTION
);

-- CreateTable
CREATE TABLE "attachments" (
    "id" SERIAL PRIMARY KEY,
    "messageId" INTEGER NOT NULL,
    "filePath" VARCHAR(255) NOT NULL,
    "fileType" VARCHAR(255) NOT NULL,
    "uploadedAt" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "attachments_messageId_fkey" FOREIGN KEY ("messageId") REFERENCES "messages"("id") ON DELETE CASCADE ON UPDATE NO ACTION
);

-- Create Clustered Indexes
CREATE INDEX idx_chats_userId ON "chats"("userId");
CLUSTER chats USING idx_chats_userId;

CREATE INDEX idx_messages_chatId ON "messages"("chatId");
CLUSTER messages USING idx_messages_chatId;

-- Create Non-Clustered Indexes
CREATE INDEX "idx_users_username" ON "users"("username");
CREATE INDEX "idx_users_email" ON "users"("email");
CREATE INDEX idx_attachments_messageId ON "attachments"("messageId");

-- Composite Indexes
CREATE INDEX idx_messages_chatId_createdAt ON "messages"("chatId", "createdAt" DESC);
CREATE INDEX idx_chats_userId_createdAt ON "chats"("userId", "createdAt" DESC);
