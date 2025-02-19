const { NotFoundError } = require('../utils/errorTypes');

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

module.exports = {
  notFoundMiddleware,
  asyncWrapper
};
