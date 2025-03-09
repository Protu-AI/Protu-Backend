const express = require("express");
const attachmentController = require("../controllers/attachmentController");
const jwtMiddleware = require("../middleware/jwtMiddleware");

const router = express.Router();
router.use(jwtMiddleware);

router.get(
  "/attachments/:messageId",
  attachmentController.getAttachmentsForMessage
);

module.exports = router;
