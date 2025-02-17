const { PrismaClient } = require('@prisma/client');
const prisma = new PrismaClient();
const { v7: uuidv7 } = require('uuid');

const createMessage = async (
  chatId,
  senderRole,
  content,
  attachmentPath,
  attachmentName
) => {
  const message = await prisma.messages.create({
    data: {
      id: uuidv7(),
      chatId,
      senderRole,
      content,
      attachmentName: attachmentName || '',
      attachments: attachmentPath
        ? {
            create: [
              {
                id: uuidv7(),
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

module.exports = {
  createMessage
};
