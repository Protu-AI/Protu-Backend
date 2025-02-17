const path = require('path');
const { getAIResponse } = require('../services/aiService');
const messageService = require('../services/messageService');
const { asyncWrapper } = require('../middleware/errorMiddleware');

const createMessage = asyncWrapper(async (req, res) => {
  const { chatId } = req.params;
  const { content } = req.body;
  const file = req.file;

  let attachmentPath = null;
  let attachmentName = null;

  if (file) {
    attachmentPath = `/uploads/${chatId}/${file.filename}`;
    attachmentName = file.originalname;
  }

  const userMessage = await messageService.createMessage(
    chatId,
    'user',
    content,
    attachmentPath,
    attachmentName
  );

  const aiResponse = await getAIResponse(content);

  const aiMessage = await messageService.createMessage(
    chatId,
    'model',
    aiResponse
  );

  res.status(201).json({
    status: 'success',
    data: {
      userMessage,
      aiMessage
    }
  });
});

module.exports = {
  createMessage
};
