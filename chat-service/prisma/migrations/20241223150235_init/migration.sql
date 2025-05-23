-- CreateEnum
CREATE TYPE "enum_messages_sender_role" AS ENUM ('user', 'model');

-- CreateTable
CREATE TABLE "users" (
    "id" SERIAL PRIMARY KEY,
    "public_id" CHAR(26) NOT NULL UNIQUE,
    "roles" TEXT NOT NULL,
    "created_at" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- CreateTable
CREATE TABLE "chats" (
    "id" CHAR(26) PRIMARY KEY,
    "user_id" CHAR(26) NOT NULL,
    "name" VARCHAR(100) NOT NULL,
    "created_at" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "chats_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "users"("public_id") ON DELETE CASCADE ON UPDATE NO ACTION
);

-- CreateTable
CREATE TABLE "messages" (
    "id" CHAR(26) PRIMARY KEY,
    "chat_id" CHAR(26) NOT NULL,
    "sender_role" "enum_messages_sender_role" NOT NULL,
    "content" TEXT NOT NULL,
    "attachment_name" VARCHAR(255) NOT NULL,
    "created_at" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "messages_chat_id_fkey" FOREIGN KEY ("chat_id") REFERENCES "chats"("id") ON DELETE CASCADE ON UPDATE NO ACTION
);

-- CreateTable
CREATE TABLE "attachments" (
    "id" CHAR(26) PRIMARY KEY,
    "message_id" CHAR(26) NOT NULL,
    "file_path" VARCHAR(255) NOT NULL,
    "file_type" VARCHAR(255) NOT NULL,
    "uploaded_at" TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "attachments_message_id_fkey" FOREIGN KEY ("message_id") REFERENCES "messages"("id") ON DELETE CASCADE ON UPDATE NO ACTION
);

-- Create Clustered Indexes
CREATE INDEX idx_chats_user_id ON "chats"("user_id");
CLUSTER chats USING idx_chats_user_id;

CREATE INDEX idx_messages_chat_id ON "messages"("chat_id");
CLUSTER messages USING idx_messages_chat_id;

CREATE INDEX idx_users_public_id ON "users"("public_id");
CREATE INDEX idx_attachments_message_id ON "attachments"("message_id");

CREATE INDEX idx_messages_chat_id_created_at ON "messages"("chat_id", "created_at" DESC);
CREATE INDEX idx_chats_user_id_created_at ON "chats"("user_id", "created_at" DESC);