const express = require('express');
const http = require('http');
const { Server } = require('socket.io');
const cors = require('cors');

const socketHandler = require('./utils/socketHandler');

const app = express();
const server = http.createServer(app);

const io = new Server(server, {
  cors: {
    origin: '*',
    methods: ['GET', 'POST']
  },
  maxHttpBufferSize: 1e7 // 10 MB max file size
});

const corsOptions = {
  origin: '*',
  methods: ['GET', 'POST', 'DELETE'],
  allowedHeaders: ['Content-Type', 'Authorization'],
  credentials: true
};

app.use(cors(corsOptions));
app.use(express.json());

const chatRoutes = require('./routes/chatRoutes');
const attachmentRoutes = require('./routes/attachmentRoutes');

app.use('/api/v1', chatRoutes);
app.use('/api/v1', attachmentRoutes);

socketHandler(io);

app.use((err, req, res, next) => {
  console.error(err.stack);
  res.status(err.status || 500).json({
    status: 'error',
    message: err.message || 'Internal server error'
  });
});

server.listen(8082, () => {
  console.log('listening on *:8082');
});
