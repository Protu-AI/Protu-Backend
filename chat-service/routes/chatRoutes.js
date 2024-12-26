const express = require("express");
const chatController = require("../controllers/chatController");

const router = express.Router();

router.post("/chats", chatController.createChat);
router.get("/chats/:userId", chatController.getUserChats);
router.delete("/chats/:chatId", chatController.deleteChat);
router.get("/chats/single/:chatId", chatController.getSingleChat);

module.exports = router;
