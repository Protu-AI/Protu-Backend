const { PrismaClient } = require('@prisma/client');
const prisma = new PrismaClient();
const { ulid } = require('ulid');
const { NotFoundError } = require('../utils/errorTypes');

const createMessage = async (
  chatId,
  senderRole,
  content,
  attachmentPath,
  attachmentName
) => {
  const chat = await prisma.chat.findUnique({
    where: { id: chatId }
  });

  if (!chat) {
    throw new NotFoundError('Chat');
  }

  try {
    const message = await prisma.message.create({
      data: {
        id: ulid(),
        chatId,
        senderRole,
        content,
        attachmentName: attachmentName || ''
      }
    });

    if (attachmentPath) {
      await prisma.attachment.create({
        data: {
          id: ulid(),
          messageId: message.id,
          filePath: attachmentPath,
          fileType: attachmentPath.split('.').pop()
        }
      });
    }

    const completeMessage = await prisma.message.findUnique({
      where: { id: message.id },
      include: {
        attachments: true
      }
    });

    return completeMessage;
  } catch (error) {
    throw error;
  }
};

module.exports = {
  createMessage
};
