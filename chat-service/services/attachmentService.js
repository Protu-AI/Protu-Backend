const { PrismaClient } = require('@prisma/client');
const { ulid } = require('ulid');
const prisma = new PrismaClient();
const {
  DatabaseError,
  NotFoundError,
  UnauthorizedError
} = require('../utils/errorTypes');

const verifyMessageOwnership = async (messageId, userId) => {
  const message = await prisma.message.findUnique({
    where: { id: messageId },
    include: {
      chat: true
    }
  });

  if (!message) {
    throw new NotFoundError('Message');
  }

  if (message.chat.userId !== userId) {
    throw new UnauthorizedError(
      'You do not have permission to access this attachment'
    );
  }

  return message;
};

const createAttachment = async (messageId, filePath, fileType) => {
  try {
    const message = await prisma.message.findUnique({
      where: { id: messageId }
    });

    if (!message) {
      throw new NotFoundError('Message');
    }

    const attachment = await prisma.attachment.create({
      data: {
        id: ulid(),
        messageId,
        filePath,
        fileType
      }
    });
    return attachment;
  } catch (error) {
    if (error instanceof NotFoundError) {
      throw error;
    }
    throw new DatabaseError('Failed to create attachment');
  }
};

const getAttachmentsForMessage = async (messageId, userId) => {
  try {
    await verifyMessageOwnership(messageId, userId);

    const attachments = await prisma.attachment.findMany({
      where: { messageId },
      select: {
        id: true,
        filePath: true,
        fileType: true,
        uploadedAt: true
      }
    });

    return attachments;
  } catch (error) {
    if (error instanceof NotFoundError || error instanceof UnauthorizedError) {
      throw error;
    }
    throw new DatabaseError('Failed to fetch attachments');
  }
};

module.exports = {
  createAttachment,
  getAttachmentsForMessage
};
