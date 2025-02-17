const multer = require('multer');
const { storage, fileFilter } = require('./multerConfig');

const upload = multer({
  storage: storage,
  fileFilter: fileFilter,
  limits: { fileSize: 10 * 1024 * 1024 },
}).single('file');

const uploadMiddleware = (req, res, next) => {
  upload(req, res, function (err) {
    if (err instanceof multer.MulterError) {
      return res.status(400).json({ status: 'error', message: err.message });
    } else if (err) {
      return res.status(500).json({ status: 'error', message: 'File upload failed' });
    }
    next();
  });
};

module.exports = uploadMiddleware;