const chatService = require("../services/chatService");
const asyncWrapper = require("../utils/asyncWrapper");

const createChat = asyncWrapper(async (req, res) => {
  const { userId } = req.body;
  const chat = await chatService.createChat(userId);
  res.status(201).json(chat);
});

const getUserChats = asyncWrapper(async (req, res) => {
  const { userId } = req.params;
  const chats = await chatService.getUserChats(userId);
  res.status(200).json(chats);
});

const deleteChat = asyncWrapper(async (req, res) => {
  const { chatId } = req.params;
  const result = await chatService.deleteChat(chatId);
  res.status(200).json({ message: "Chat deleted successfully", result });
});

const getSingleChat = asyncWrapper(async (req, res) => {
  const { chatId } = req.params;
  const chat = await chatService.getSingleChat(chatId);
  res.status(200).json(chat);
});

module.exports = {
  createChat,
  getUserChats,
  deleteChat,
  getSingleChat,
};
