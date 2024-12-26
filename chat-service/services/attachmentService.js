const { PrismaClient } = require("@prisma/client");
const prisma = new PrismaClient();

const createAttachment = async (messageId, filePath, fileType) => {
  const attachment = await prisma.attachments.create({
    data: { messageId, filePath, fileType },
  });
  return attachment;
};

const getAttachmentsForMessage = async (messageId) => {
  const attachments = await prisma.attachments.findMany({
    where: { messageId },
  });
  return attachments;
};

module.exports = {
  createAttachment,
  getAttachmentsForMessage,
};
