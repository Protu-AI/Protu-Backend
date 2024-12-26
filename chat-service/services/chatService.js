const { PrismaClient } = require("@prisma/client");
const prisma = new PrismaClient();

const createChat = async (userId) => {
  const chat = await prisma.chats.create({
    data: {
      userId,
      id: crypto.randomUUID(),
    },
  });
  return chat;
};

const getUserChats = async (userId) => {
  const chats = await prisma.chats.findMany({
    where: { userId },
  });
  return chats;
};

const deleteChat = async (chatId) => {
  const result = await prisma.chats.delete({
    where: { id: chatId },
  });
  return result;
};

const getSingleChat = async (chatId) => {
  const chat = await prisma.chats.findUnique({
    where: { id: chatId },
    include: {
      messages: true,
    },
  });
  return chat;
};

module.exports = {
  createChat,
  getUserChats,
  deleteChat,
  getSingleChat,
};
