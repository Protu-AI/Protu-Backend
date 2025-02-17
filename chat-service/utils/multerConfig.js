const multer = require('multer');
const path = require('path');
const fs = require('fs');

const baseUploadDir = path.join(__dirname, '..', 'uploads');
if (!fs.existsSync(baseUploadDir)) {
  fs.mkdirSync(baseUploadDir, { recursive: true });
}

const sanitizeFilename = filename => filename.replace(/[^a-zA-Z0-9._-]/g, '_');

const storage = multer.diskStorage({
  destination: function (req, file, cb) {
    const chatId = req.params.chatId;
    if (!chatId) return cb(new Error('Chat ID is required'), false);

    const uploadDir = path.join(__dirname, '..', 'uploads', chatId.toString());
    if (!fs.existsSync(uploadDir)) {
      fs.mkdirSync(uploadDir, { recursive: true });
    }
    cb(null, uploadDir);
  },
  filename: function (req, file, cb) {
    cb(null, sanitizeFilename(file.originalname));
  }
});

const fileFilter = (req, file, cb) => {
  const allowedTypes = [
    'image/jpeg',
    'image/png',
    'image/gif',
    'application/pdf'
  ];
  if (allowedTypes.includes(file.mimetype)) return cb(null, true);
  cb(
    new Error(`Invalid file type. Allowed types: ${allowedTypes.join(', ')}`),
    false
  );
};

const upload = multer({
  storage,
  fileFilter,
  limits: { fileSize: 10 * 1024 * 1024 }
});

module.exports = upload;
