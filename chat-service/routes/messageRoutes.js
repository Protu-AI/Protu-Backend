const express = require('express');
const messageController = require('../controllers/messageController');
const uploadMiddleware = require('../utils/uploadMiddleware');
const jwtMiddleware = require('../middleware/jwtMiddleware');

const router = express.Router();

router.use(jwtMiddleware);

router.post(
  '/messages/:chatId',
  uploadMiddleware,
  messageController.createMessage
);

module.exports = router;
