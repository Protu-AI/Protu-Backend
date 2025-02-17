const chatService = require('../services/chatService');
const { asyncWrapper } = require('../middleware/errorMiddleware');
const { AppError } = require('../utils/errorHandler');

const createChat = asyncWrapper(async (req, res) => {
  const { userId } = req.params;
  const { name } = req.body;
  const chat = await chatService.createChat(userId, name);
  res.status(201).json({
    status: 'success',
    data: chat
  });
});

const getUserChats = asyncWrapper(async (req, res) => {
  const { userId } = req.params;
  const { page = 1, limit = 10 } = req.query;

  const chats = await chatService.getUserChats(
    userId,
    parseInt(page),
    parseInt(limit)
  );
  res.status(200).json({
    status: 'success',
    data: chats
  });
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

  res.status(200).json({
    status: 'success',
    data: chat
  });
});

const deleteChat = asyncWrapper(async (req, res) => {
  const { chatId } = req.params;

  const result = await chatService.deleteChat(chatId);
  res.status(200).json({
    status: 'success',
    message: 'Chat deleted successfully',
    data: result
  });
});

module.exports = {
  createChat,
  getUserChats,
  deleteChat,
  getSingleChat
};
