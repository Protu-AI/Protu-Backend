const { PrismaClient } = require('@prisma/client');
const { ulid } = require('ulid');
const prisma = new PrismaClient();

const {
  DatabaseError,
  ValidationError,
  NotFoundError
} = require('../utils/errorTypes');

const createChat = async (userId, name) => {
  try {
    if (!name) {
      throw new ValidationError('Chat name is required');
    }

    const user = await prisma.user.findUnique({
      where: {
        publicId: userId
      },
      select: {
        publicId: true,
        id: true,
        username: true
      }
    });

    if (!user) {
      throw new NotFoundError('User');
    }

    const chat = await prisma.chat
      .create({
        data: {
          id: ulid(),
          userId: userId,
          name: name
        }
      })
      .catch(error => {
        throw error;
      });

    return chat;
  } catch (error) {
    if (error instanceof ValidationError || error instanceof NotFoundError) {
      throw error;
    }

    if (error.code === 'P2003') {
      throw new ValidationError('Invalid user ID');
    }
    throw new DatabaseError('Failed to create chat');
  }
};

const getUserChats = async (userId, page, limit) => {
  try {
    const skip = (page - 1) * limit;
    const chats = await prisma.chat.findMany({
      where: { userId: userId },
      skip,
      take: limit,
      orderBy: { createdAt: 'desc' }
    });

    const totalChats = await prisma.chat.count({
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
  } catch (error) {
    throw new DatabaseError('Failed to fetch user chats');
  }
};

const deleteChat = async chatId => {
  const result = await prisma.chat.delete({
    where: { id: chatId }
  });
  return result;
};

const getSingleChat = async (chatId, page, limit) => {
  try {
    const chat = await prisma.chat.findUnique({
      where: { id: chatId }
    });

    if (!chat) {
      throw new NotFoundError('Chat');
    }

    const messages = await prisma.message.findMany({
      where: { chatId },
      skip: (page - 1) * limit,
      take: limit,
      orderBy: { createdAt: 'desc' }
    });

    const totalMessages = await prisma.message.count({
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
    if (error instanceof NotFoundError) throw error;
    throw new DatabaseError('Failed to fetch chat');
  }
};

module.exports = {
  createChat,
  getUserChats,
  deleteChat,
  getSingleChat
};
