const chatService = require('../services/chatService');
const { asyncWrapper } = require('../middleware/errorMiddleware');
const { AppError } = require('../utils/errorHandler');
const { ValidationError } = require('../utils/errorTypes');
const { buildResponse } = require('../utils/responseHelper');

const createChat = asyncWrapper(async (req, res) => {
  const userId = req.user.id;
  const { name } = req.body;

  if (!name) {
    throw new ValidationError('Chat name is required');
  }

  const chat = await chatService.createChat(userId, name);
  res
    .status(201)
    .json(buildResponse(req, 'CREATED', chat, 'Chat created successfully'));
});

const getUserChats = asyncWrapper(async (req, res) => {
  const userId = req.user.id;
  const { page = 1, limit = 10 } = req.query;

  const chats = await chatService.getUserChats(
    userId,
    parseInt(page),
    parseInt(limit)
  );
  res
    .status(200)
    .json(buildResponse(req, 'OK', chats, 'Chats retrieved successfully'));
});

const getSingleChat = asyncWrapper(async (req, res, next) => {
  const userId = req.user.id;
  const { chatId } = req.params;
  const { page = 1, limit = 10 } = req.query;

  const chat = await chatService.getSingleChat(
    chatId,
    parseInt(page),
    parseInt(limit),
    userId
  );

  res
    .status(200)
    .json(buildResponse(req, 'OK', chat, 'Chat retrieved successfully'));
});

const deleteChat = asyncWrapper(async (req, res) => {
  const userId = req.user.id;
  const { chatId } = req.params;

  const result = await chatService.deleteChat(chatId, userId);
  res
    .status(200)
    .json(buildResponse(req, 'OK', result, 'Chat deleted successfully'));
});

const updateChatName = asyncWrapper(async (req, res) => {
  const userId = req.user.id;
  const { chatId } = req.params;
  const { name } = req.body;

  if (!name) {
    throw new ValidationError('Chat name is required');
  }

  const updatedChat = await chatService.updateChatName(chatId, userId, name);
  res
    .status(200)
    .json(
      buildResponse(req, 'OK', updatedChat, 'Chat name updated successfully')
    );
});

module.exports = {
  createChat,
  getUserChats,
  deleteChat,
  getSingleChat,
  updateChatName
};
