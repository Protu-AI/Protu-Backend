const { NotFoundError } = require('../utils/errorTypes');

const notFoundMiddleware = (req, res, next) => {
  next(new NotFoundError('Route'));
};

const asyncWrapper = asyncFn => {
  return (req, res, next) => {
    asyncFn(req, res, next).catch(next);
  };
};

module.exports = {
  notFoundMiddleware,
  asyncWrapper
};
