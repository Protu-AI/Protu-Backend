const path = require('path');
const fs = require('fs');
const { getAIResponse } = require('../services/aiService');
const messageService = require('../services/messageService');

const socketHandler = io => {
  io.on('connection', socket => {
    console.log('A user connected:', socket.id);

    socket.on('join_chat', chatId => {
      socket.join(`chat_${chatId}`);
    });

    socket.on('send_message', async data => {
      try {
        const { chatId, content, file } = data;
        let attachmentPath = null;

        if (file) {
          try {
            const fileName = `${Date.now()}-${file.name}`;
            const uploadDir = path.join(
              __dirname,
              '../uploads',
              chatId.toString()
            );

            if (!fs.existsSync(uploadDir)) {
              fs.mkdirSync(uploadDir, { recursive: true });
            }

            const filePath = path.join(uploadDir, fileName);
            const buffer = Buffer.from(file.data);
            fs.writeFileSync(filePath, buffer);

            attachmentPath = `/uploads/${chatId}/${fileName}`;
          } catch (fileError) {
            console.error('File processing error:', fileError);
            socket.emit('error', { message: 'Failed to process file' });
            return;
          }
        }

        const userMessage = await messageService.createMessage(
          parseInt(chatId),
          'user',
          content,
          attachmentPath,
          file?.name || null
        );

        const aiResponse = await getAIResponse(content);

        const aiMessage = await messageService.createMessage(
          parseInt(chatId),
          'model',
          aiResponse
        );

        socket.emit('receive_message', {
          status: 'success',
          data: aiMessage
        });
        socket.emit('receive_message', aiMessage);
      } catch (error) {
        console.error('Error in send_message handler:', error);
        socket.emit('error', {
          message: 'Failed to process message',
          error: error.message
        });
      }
    });

    socket.on('disconnect', () => {
      console.log('User disconnected:', socket.id);
    });
  });
};

module.exports = socketHandler;
