const attachmentService = require("../services/attachmentService");
const asyncWrapper = require("../utils/asyncWrapper");

const getAttachmentsForMessage = asyncWrapper(async (req, res) => {
  const { messageId } = req.params;
  const attachments = await attachmentService.getAttachmentsForMessage(
    messageId
  );
  res.status(200).json(attachments);
});

module.exports = {
  getAttachmentsForMessage,
};
