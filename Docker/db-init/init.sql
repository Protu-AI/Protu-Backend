CREATE TYPE "enum_messages_sender_role" AS ENUM ('user', 'model');

CREATE TABLE IF NOT EXISTS "users"
(
    "id"                SERIAL PRIMARY KEY,
    "public_id"         CHAR(26)     NOT NULL UNIQUE,
    "first_name"        VARCHAR(50)  NOT NULL,
    "last_name"         VARCHAR(50)  NOT NULL,
    "username"          VARCHAR(50)  NOT NULL UNIQUE,
    "email"             VARCHAR(100) NOT NULL UNIQUE,
    "password"          VARCHAR(100) NOT NULL,
    "phone_number"      VARCHAR(20)  NOT NULL,
    "roles"             TEXT         NOT NULL,
    "is_active"         BOOLEAN      NOT NULL DEFAULT TRUE,
    "is_email_verified" BOOLEAN      NOT NULL DEFAULT FALSE,
    "image_url"         VARCHAR(255),
    "created_at"        TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at"        TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS "chats"
(
    "id"         CHAR(26) PRIMARY KEY,
    "user_id"    CHAR(26)     NOT NULL,
    "name"       VARCHAR(100) NOT NULL,
    "created_at" TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "chats_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "users" ("public_id") ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS "messages"
(
    "id"              CHAR(26) PRIMARY KEY,
    "chat_id"         CHAR(26)                    NOT NULL,
    "sender_role"     "enum_messages_sender_role" NOT NULL,
    "content"         TEXT                        NOT NULL,
    "attachment_name" VARCHAR(255)                NOT NULL,
    "created_at"      TIMESTAMPTZ                 NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "messages_chat_id_fkey" FOREIGN KEY ("chat_id") REFERENCES "chats" ("id") ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS "attachments"
(
    "id"          CHAR(26) PRIMARY KEY,
    "message_id"  CHAR(26)     NOT NULL,
    "file_path"   VARCHAR(255) NOT NULL,
    "file_type"   VARCHAR(255) NOT NULL,
    "uploaded_at" TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "attachments_message_id_fkey" FOREIGN KEY ("message_id") REFERENCES "messages" ("id") ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE INDEX idx_chats_user_id ON "chats" ("user_id");
CLUSTER chats USING idx_chats_user_id;

CREATE INDEX idx_messages_chat_id ON "messages" ("chat_id");
CLUSTER messages USING idx_messages_chat_id;

CREATE INDEX idx_users_username ON "users" ("username");
CREATE INDEX idx_users_email ON "users" ("email");
CREATE INDEX idx_users_public_id ON "users" ("public_id");
CREATE INDEX idx_attachments_message_id ON "attachments" ("message_id");

CREATE INDEX idx_messages_chat_id_created_at ON "messages" ("chat_id", "created_at" DESC);
CREATE INDEX idx_chats_user_id_created_at ON "chats" ("user_id", "created_at" DESC);