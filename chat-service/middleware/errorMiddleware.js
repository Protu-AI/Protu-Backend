const { NotFoundError } = require('../utils/errorTypes');
const { UnauthorizedError } = require('../utils/errorTypes');

const notFoundMiddleware = (req, res, next) => {
  next(new NotFoundError('Route'));
};

const asyncWrapper = asyncFn => {
  return (req, res, next) => {
    asyncFn(req, res, next).catch(error => {
      console.error('Request details:', {
        method: req.method,
        path: req.path,
        params: req.params,
        body: req.body
      });
      next(error);
    });
  };
};

const handleErrors = (err, req, res, next) => {
  if (err instanceof UnauthorizedError) {
    return res.status(401).json({
      status: 'ERROR',
      error: {
        code: 'UNAUTHORIZED',
        message: err.message
      }
    });
  }

  next(new NotFoundError('Route'));
};

module.exports = {
  handleErrors,
  asyncWrapper
};
