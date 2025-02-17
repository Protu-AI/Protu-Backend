class AppError extends Error {
  constructor(message, statusCode) {
    super(message);
    this.statusCode = statusCode;
    this.isOperational = true;
  }
}

const globalErrorHandler = (err, req, res, next) => {
  console.error('Error:', err);

  if (!err.statusCode) err.statusCode = 500;
  res.status(err.statusCode).json({
    status: 'error',
    message: err.isOperational ? err.message : 'Internal Server Error'
  });
};

module.exports = { AppError, globalErrorHandler };
