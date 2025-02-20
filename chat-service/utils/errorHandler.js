const {
  ValidationError,
  DatabaseError,
  NotFoundError,
  FileUploadError
} = require('./errorTypes');
const multer = require('multer');
const { buildResponse } = require('./responseHelper');

const prismaErrorHandler = error => {
  if (error.code === 'P2002')
    return new ValidationError('Unique constraint violation');
  if (error.code === 'P2025') return new NotFoundError('Record');
  return new DatabaseError('Database operation failed');
};

const globalErrorHandler = (err, req, res, next) => {
  console.error('Error details:', {
    name: err.name,
    message: err.message,
    stack: process.env.NODE_ENV === 'development' ? err.stack : undefined
  });

  if (err.name === 'PrismaClientKnownRequestError')
    err = prismaErrorHandler(err);
  if (err instanceof multer.MulterError) err = new FileUploadError(err.message);

  const response = buildResponse(req, 'ERROR', null, err.message);
  response.meta.status = 'ERROR';
  response.error = {
    code: err.errorCode || 'INTERNAL_ERROR',
    details: process.env.NODE_ENV === 'development' ? err.stack : undefined
  };

  res.status(err.statusCode || 500).json(response);
};

module.exports = {
  globalErrorHandler
};
