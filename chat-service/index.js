const express = require('express');
const cors = require('cors');
const path = require('path');
const multer = require('multer');
const morgan = require('morgan');
const { globalErrorHandler } = require('./utils/errorHandler');

const app = express();
const corsOptions = {
  origin: '*',
  methods: ['GET', 'POST', 'DELETE'],
  allowedHeaders: ['Content-Type', 'Authorization'],
  credentials: true
};

app.use(cors(corsOptions));
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(morgan('dev'));

app.use('/uploads', express.static(path.join(__dirname, 'uploads')));

const fs = require('fs');
const uploadsDir = path.join(__dirname, 'uploads');
if (!fs.existsSync(uploadsDir)) {
  fs.mkdirSync(uploadsDir, { recursive: true });
}

const chatRoutes = require('./routes/chatRoutes');
const attachmentRoutes = require('./routes/attachmentRoutes');
const messageRoutes = require('./routes/messageRoutes');

app.use('/api/v1', chatRoutes);
app.use('/api/v1', attachmentRoutes);
app.use('/api/v1', messageRoutes);

app.use((err, req, res, next) => {
  console.error('Error details:', {
    name: err.name,
    message: err.message,
    stack: err.stack
  });

  if (err instanceof multer.MulterError) {
    if (err.code === 'LIMIT_FILE_SIZE') {
      return res.status(400).json({
        status: 'error',
        message: 'File size is too large. Maximum size is 10MB'
      });
    }
    return res.status(400).json({
      status: 'error',
      message: err.message
    });
  }

  res.status(err.status || 500).json({
    status: 'error',
    message: err.message || 'Internal server error'
  });
});

app.use((req, res) => {
  res.status(404).json({
    status: 'error',
    message: 'Route not found'
  });
});

app.use(globalErrorHandler);

const PORT = 8082;
app.listen(PORT, () => {
  console.log(`Server is running on port ${PORT}`);
});

process.on('unhandledRejection', err => {
  console.error('UNHANDLED REJECTION!');
  console.error(err.name, err.message);
  process.exit(1);
});

process.on('uncaughtException', err => {
  console.error('UNCAUGHT EXCEPTION!');
  console.error(err.name, err.message);
  process.exit(1);
});
