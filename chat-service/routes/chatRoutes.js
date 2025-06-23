const express = require('express');
const jwtMiddleware = require('../middleware/jwtMiddleware');
const chatController = require('../controllers/chatController');

const router = express.Router();
router.use(jwtMiddleware);

router.post('/chats', chatController.createChat);
router.get('/chats', chatController.getUserChats);
router.delete('/chats/:chatId', chatController.deleteChat);
router.get('/chats/single/:chatId', chatController.getSingleChat);
router.put('/chats/:chatId/name', chatController.updateChatName);

module.exports = router;
