const path = require('path');
const { getAIResponse } = require('../services/aiService');
const messageService = require('../services/messageService');
const { asyncWrapper } = require('../middleware/errorMiddleware');
const { buildResponse } = require('../utils/responseHelper');

const createMessage = asyncWrapper(async (req, res) => {
  const { chatId } = req.params;
  const { content } = req.body;
  const file = req.file;

  let attachmentPath = null;
  let attachmentName = null;

  if (file) {
    attachmentPath = path.join(
      __dirname,
      '..',
      'uploads',
      chatId,
      file.filename
    );
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

  res.status(201).json(
    buildResponse(
      req,
      'CREATED',
      {
        userMessage,
        aiMessage
      },
      'Messages created successfully'
    )
  );
});

module.exports = {
  createMessage
};
