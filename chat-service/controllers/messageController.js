const messageService = require("../services/messageService");
const asyncWrapper = require("../utils/asyncWrapper");

const getMessagesForChat = asyncWrapper(async (req, res) => {
  const { chatId } = req.params;
  const messages = await messageService.getMessagesForChat(chatId);
  res.status(200).json(messages);
});

module.exports = {
  getMessagesForChat,
};
