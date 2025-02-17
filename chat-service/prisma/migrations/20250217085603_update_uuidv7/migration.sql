/*
  Warnings:

  - You are about to drop the column `code_expiry_date` on the `users` table. All the data in the column will be lost.
  - You are about to drop the column `createdAt` on the `users` table. All the data in the column will be lost.
  - You are about to drop the column `updatedAt` on the `users` table. All the data in the column will be lost.
  - You are about to drop the column `verification_code` on the `users` table. All the data in the column will be lost.
  - Added the required column `updated_at` to the `users` table without a default value. This is not possible if the table is not empty.

*/
-- DropIndex
DROP INDEX "idx_chats_userid_createdat";

-- DropIndex
DROP INDEX "idx_messages_chatid_createdat";

-- AlterTable
ALTER TABLE "attachments" ALTER COLUMN "id" DROP DEFAULT,
ALTER COLUMN "uploadedAt" DROP NOT NULL;

-- AlterTable
ALTER TABLE "chats" ALTER COLUMN "id" DROP DEFAULT,
ALTER COLUMN "createdAt" DROP NOT NULL,
ALTER COLUMN "updatedAt" DROP NOT NULL;

-- AlterTable
ALTER TABLE "messages" ALTER COLUMN "id" DROP DEFAULT,
ALTER COLUMN "createdAt" DROP NOT NULL;

-- AlterTable
ALTER TABLE "users" DROP COLUMN "code_expiry_date",
DROP COLUMN "createdAt",
DROP COLUMN "updatedAt",
DROP COLUMN "verification_code",
ADD COLUMN     "created_at" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN     "updated_at" TIMESTAMP(3) NOT NULL,
ALTER COLUMN "id" DROP DEFAULT,
ALTER COLUMN "publicId" SET DATA TYPE TEXT;

-- CreateIndex
CREATE INDEX "idx_chats_userId_createdAt" ON "chats"("userId", "createdAt");

-- CreateIndex
CREATE INDEX "idx_messages_chatId_createdAt" ON "messages"("chatId", "createdAt");

-- CreateIndex
CREATE INDEX "idx_users_public_id" ON "users"("publicId");

-- RenameIndex
ALTER INDEX "idx_attachments_messageid" RENAME TO "idx_attachments_messageId";

-- RenameIndex
ALTER INDEX "idx_chats_userid" RENAME TO "idx_chats_userId";

-- RenameIndex
ALTER INDEX "idx_messages_chatid" RENAME TO "idx_messages_chatId";
