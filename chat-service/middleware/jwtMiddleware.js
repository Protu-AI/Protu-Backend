const jwt = require('jsonwebtoken');
const { UnauthorizedError } = require('../utils/errorTypes');

const getTokenFromHeader = authHeader => {
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    throw new UnauthorizedError('Invalid authorization header');
  }
  return authHeader.split(' ')[1];
};

const validateToken = token => {
  try {
    const decoded = jwt.verify(token, process.env.JWT_SECRET);
    return {
      valid: true,
      userId: decoded.sub
    };
  } catch (error) {
    return {
      valid: false,
      userId: null
    };
  }
};

const jwtMiddleware = async (req, res, next) => {
  try {
    const authHeader = req.headers.authorization;
    const token = getTokenFromHeader(authHeader);

    const { valid, userId } = validateToken(token);
    if (!valid) {
      throw new UnauthorizedError('Invalid or expired token');
    }

    req.user = { id: userId };
    next();
  } catch (error) {
    next(error);
  }
};

module.exports = jwtMiddleware;
