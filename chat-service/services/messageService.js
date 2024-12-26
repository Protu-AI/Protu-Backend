const { PrismaClient } = require("@prisma/client");
const prisma = new PrismaClient();

const createMessage = async (chatId, senderRole, content) => {
  const message = await prisma.messages.create({
    data: { chatId, senderRole, content },
  });
  return message;
};

const getMessagesForChat = async (chatId) => {
  const messages = await prisma.messages.findMany({
    where: { chatId },
  });
  return messages;
};

module.exports = {
  createMessage,
  getMessagesForChat,
};
