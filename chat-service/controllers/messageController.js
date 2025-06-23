const path = require('path');
const { getAIResponse } = require('../services/aiService');
const messageService = require('../services/messageService');
const { asyncWrapper } = require('../middleware/errorMiddleware');
const { buildResponse } = require('../utils/responseHelper');
const chatService = require('../services/chatService');
const { ValidationError } = require('../utils/errorTypes');

const createMessage = asyncWrapper(async (req, res) => {
  const userId = req.user.id;
  const { chatId } = req.params;
  const { content } = req.body;
  const file = req.file;

  await chatService.verifyOwnership(chatId, userId);

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

  try {
    const hasAttachment = !!file;
    const aiResponse = await getAIResponse(chatId, hasAttachment);

    const aiMessage = await messageService.createMessage(
      chatId,
      'model',
      aiResponse.answer
    );
    // const aiMessage = await messageService.createMessage(
    //   chatId,
    //   'model',
    //   'No response from AI'
    // );

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
  } catch (error) {
    res.status(500).json(
      buildResponse(
        req,
        'ERROR',
        {
          userMessage
        },
        'Failed to get AI response'
      )
    );
  }
});

const createMessageWithAutoChat = asyncWrapper(async (req, res) => {
  const userId = req.user.id;
  const { content, chatId } = req.body;
  const file = req.file;

  if (!content || content.trim() === '') {
    throw new ValidationError('Message content is required');
  }

  const chat = await chatService.getOrCreateChatForMessage(
    userId,
    content,
    chatId
  );
  const targetChatId = chat.id;

  let attachmentPath = null;
  let attachmentName = null;

  if (file) {
    attachmentPath = path.join(
      __dirname,
      '..',
      'uploads',
      targetChatId,
      file.filename
    );
    attachmentName = file.originalname;
  }

  const userMessage = await messageService.createMessage(
    targetChatId,
    'user',
    content,
    attachmentPath,
    attachmentName
  );

  try {
    const hasAttachment = !!file;
    const aiResponse = await getAIResponse(targetChatId, hasAttachment);

    const aiMessage = await messageService.createMessage(
      targetChatId,
      'model',
      aiResponse.answer
    );

    res.status(201).json(
      buildResponse(
        req,
        'CREATED',
        {
          chatId: targetChatId,
          userMessage,
          aiMessage
        },
        'Messages created successfully'
      )
    );
  } catch (error) {
    res.status(500).json(
      buildResponse(
        req,
        'ERROR',
        {
          chatId: targetChatId,
          userMessage
        },
        'Failed to get AI response'
      )
    );
  }
});

module.exports = {
  createMessage,
  createMessageWithAutoChat
};
