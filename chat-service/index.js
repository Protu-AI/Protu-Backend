const express = require('express');
const path = require('path');
const morgan = require('morgan');
const { globalErrorHandler } = require('./utils/errorHandler');
const { NotFoundError } = require('./utils/errorTypes');

const app = express();

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

app.use((req, res, next) => {
  next(new NotFoundError('Route'));
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
