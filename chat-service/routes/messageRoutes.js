const express = require('express');
const messageController = require('../controllers/messageController');
const upload = require('../utils/multerConfig');

const router = express.Router();

router.post(
  '/messages/:chatId',
  upload.single('file'),
  messageController.createMessage
);

module.exports = router;
