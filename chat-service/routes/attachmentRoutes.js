const express = require("express");
const attachmentController = require("../controllers/attachmentController");

const router = express.Router();

router.get(
  "/attachments/:messageId",
  attachmentController.getAttachmentsForMessage
);

module.exports = router;
