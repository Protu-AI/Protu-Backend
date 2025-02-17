const express = require('express');
const messageController = require('../controllers/messageController');
const uploadMiddleware = require('../utils/uploadMiddleware');

const router = express.Router();

router.post(
  '/messages/:chatId',
  uploadMiddleware,
  messageController.createMessage
);

module.exports = router;
