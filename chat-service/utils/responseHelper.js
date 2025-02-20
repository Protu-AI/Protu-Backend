const buildResponse = (request, status, data, message) => {
  return {
    message,
    data,
    error: null,
    meta: {
      status: 'SUCCESS',
      version: process.env.API_VERSION || 'v1.0.0',
      timestamp: new Date().toISOString(),
      request: {
        method: request.method,
        path: request.path
      }
    }
  };
};

module.exports = {
  buildResponse
};
