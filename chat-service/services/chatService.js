const { PrismaClient } = require('@prisma/client');
const prisma = new PrismaClient();

const createChat = async (userId, name) => {
  const chat = await prisma.chats.create({
    data: { userId, name }
  });
  return {
    data: chat
  };
};

const getUserChats = async (userId, page, limit) => {
  const skip = (page - 1) * limit;
  const chats = await prisma.chats.findMany({
    where: { userId },
    skip,
    take: limit,
    orderBy: { createdAt: 'desc' }
  });

  const totalChats = await prisma.chats.count({ where: { userId } });

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
    where: { id: chatId },
    include: {
      messages: {
        skip,
        take: limit,
        orderBy: { createdAt: 'desc' }
      }
    }
  });

  if (!chat) return null;

  const totalMessages = await prisma.messages.count({ where: { chatId } });

  return {
    chat,
    messages: chat.messages,
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
