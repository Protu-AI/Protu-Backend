const attachmentService = require('../services/attachmentService');
const { asyncWrapper } = require('../middleware/errorMiddleware');

const getAttachmentsForMessage = asyncWrapper(async (req, res) => {
  const { messageId } = req.params;
  const attachments = await attachmentService.getAttachmentsForMessage(
    messageId
  );
  res.status(200).json({
    status: 'success',
    data: attachments
  });
});

module.exports = {
  getAttachmentsForMessage
};
