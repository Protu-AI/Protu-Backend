const { PrismaClient } = require('@prisma/client');
const prisma = new PrismaClient();

const createMessage = async (
  chatId,
  senderRole,
  content,
  attachmentPath,
  attachmentName
) => {
  const message = await prisma.messages.create({
    data: {
      chatId,
      senderRole,
      content,
      attachmentName: attachmentName || '',
      attachments: attachmentPath
        ? {
            create: [
              {
                filePath: attachmentPath,
                fileType: attachmentPath.split('.').pop()
              }
            ]
          }
        : undefined
    },
    include: {
      attachments: true
    }
  });
  return message;
};

const getMessagesForChat = async chatId => {
  const messages = await prisma.messages.findMany({
    where: { chatId },
    include: {
      attachments: true
    },
    orderBy: {
      createdAt: 'asc'
    }
  });
  return messages;
};

module.exports = {
  createMessage,
  getMessagesForChat
};
