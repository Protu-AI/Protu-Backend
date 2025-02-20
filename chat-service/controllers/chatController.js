const chatService = require('../services/chatService');
const { asyncWrapper } = require('../middleware/errorMiddleware');
const { AppError } = require('../utils/errorHandler');
const { ValidationError } = require('../utils/errorTypes');
const { buildResponse } = require('../utils/responseHelper');

const createChat = asyncWrapper(async (req, res) => {
  const { userId } = req.params;
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
  const { userId } = req.params;
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
  const { chatId } = req.params;
  const { page = 1, limit = 10 } = req.query;

  const chat = await chatService.getSingleChat(
    chatId,
    parseInt(page),
    parseInt(limit)
  );
  if (!chat) {
    return next(new AppError('Chat not found', 404));
  }

  res
    .status(200)
    .json(buildResponse(req, 'OK', chat, 'Chat retrieved successfully'));
});

const deleteChat = asyncWrapper(async (req, res) => {
  const { chatId } = req.params;

  const result = await chatService.deleteChat(chatId);
  res
    .status(200)
    .json(buildResponse(req, 'OK', result, 'Chat deleted successfully'));
});

module.exports = {
  createChat,
  getUserChats,
  deleteChat,
  getSingleChat
};
