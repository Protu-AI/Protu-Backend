const { PrismaClient } = require('@prisma/client');
const { v4: uuidv7 } = require('uuid');
const prisma = new PrismaClient();
const { DatabaseError, NotFoundError } = require('../utils/errorTypes');

const createChat = async (userId, name) => {
  try {
    const chat = await prisma.chats.create({
      data: {
        id: uuidv7(),
        userId,
        name
      }
    });
    return { data: chat };
  } catch (error) {
    if (error.code === 'P2003') {
      throw new ValidationError('Invalid user ID');
    }
    throw new DatabaseError('Failed to create chat');
  }
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
  try {
    const chat = await prisma.chats.findUnique({
      where: { id: chatId }
    });

    if (!chat) {
      throw new NotFoundError('Chat');
    }

    const messages = await prisma.messages.findMany({
      where: { chatId },
      skip: (page - 1) * limit,
      take: limit,
      orderBy: { createdAt: 'desc' }
    });

    const totalMessages = await prisma.messages.count({
      where: { chatId }
    });

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
  } catch (error) {
    if (error instanceof AppError) throw error;
    throw new DatabaseError('Failed to fetch chat');
  }
};

module.exports = {
  createChat,
  getUserChats,
  deleteChat,
  getSingleChat
};
