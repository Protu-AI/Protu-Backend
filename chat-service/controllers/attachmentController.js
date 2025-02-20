const attachmentService = require('../services/attachmentService');
const { asyncWrapper } = require('../middleware/errorMiddleware');
const { buildResponse } = require('../utils/responseHelper');

const getAttachmentsForMessage = asyncWrapper(async (req, res) => {
  const { messageId } = req.params;
  const attachments = await attachmentService.getAttachmentsForMessage(
    messageId
  );
  res
    .status(200)
    .json(
      buildResponse(
        req,
        'OK',
        attachments,
        'Attachments retrieved successfully'
      )
    );
});

module.exports = {
  getAttachmentsForMessage
};
