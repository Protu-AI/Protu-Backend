const chatService = require('../services/chatService');
const asyncWrapper = require('../utils/asyncWrapper');

const createChat = asyncWrapper(async (req, res) => {
  const { userId } = req.params;
  const { name } = req.body;
  const chat = await chatService.createChat(parseInt(userId), name);
  res.status(201).json(chat);
});

const getUserChats = asyncWrapper(async (req, res) => {
  const { userId } = req.params;
  const chats = await chatService.getUserChats(parseInt(userId));
  res.status(200).json(chats);
});

const deleteChat = asyncWrapper(async (req, res) => {
  const { chatId } = req.params;
  const result = await chatService.deleteChat(parseInt(chatId));
  res.status(200).json({ message: 'Chat deleted successfully', result });
});

const getSingleChat = asyncWrapper(async (req, res) => {
  const { chatId } = req.params;
  const chat = await chatService.getSingleChat(parseInt(chatId));
  res.status(200).json(chat);
});

module.exports = {
  createChat,
  getUserChats,
  deleteChat,
  getSingleChat
};
