const express = require("express");
const messageController = require("../controllers/messageController");

const router = express.Router();

router.get("/messages/:chatId", messageController.getMessagesForChat);

module.exports = router;
