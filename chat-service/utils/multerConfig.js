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
    const timestamp = Date.now();
    const originalName = sanitizeFilename(file.originalname);
    const fileExt = path.extname(originalName);
    const fileName = path.basename(originalName, fileExt);
    const newFileName = `${fileName}_${timestamp}${fileExt}`;
    cb(null, newFileName);
  }
});

const fileFilter = (req, file, cb) => {
  const allowedTypes = ['text/plain', 'application/pdf'];
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
