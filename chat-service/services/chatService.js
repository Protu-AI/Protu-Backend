const { PrismaClient } = require('@prisma/client');
const { v4: uuidv7 } = require('uuid');
const prisma = new PrismaClient();

const createChat = async (userId, name) => {
  const chat = await prisma.chats.create({
    data: {
      id: uuidv7(),
      userId: userId,
      name
    }
  });
  return { data: chat };
};

const getUserChats = async (userId, page, limit) => {
  const skip = (page - 1) * limit;
  const chats = await prisma.chats.findMany({
    where: { userId: userId },
    skip,
    take: limit,
    orderBy: { createdAt: 'desc' }
  });

  const totalChats = await prisma.chats.count({
    where: { userId: userId }
  });

  return {
    chats,
    pagination: {
      total: totalChats,
      page,
      limit,
      totalPages: Math.ceil(totalChats / limit)
    }
  };
};

const deleteChat = async chatId => {
  const result = await prisma.chats.delete({
    where: { id: chatId }
  });
  return result;
};

const getSingleChat = async (chatId, page, limit) => {
  const skip = (page - 1) * limit;

  const chat = await prisma.chats.findUnique({
    where: { id: chatId }
  });

  if (!chat) return null;

  const messages = await prisma.messages.findMany({
    where: { chatId },
    skip,
    take: limit,
    orderBy: { createdAt: 'desc' }
  });

  const totalMessages = await prisma.messages.count({ where: { chatId } });

  return {
    chat,
    messages,
    pagination: {
      total: totalMessages,
      page,
      limit,
      totalPages: Math.ceil(totalMessages / limit)
    }
  };
};

module.exports = {
  createChat,
  getUserChats,
  deleteChat,
  getSingleChat
};
