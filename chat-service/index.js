const express = require("express");
const http = require("http");
const { Server } = require("socket.io");
const axios = require("axios");
const cors = require("cors");

const app = express();
const server = http.createServer(app);

const io = new Server(server, {
  cors: {
    origin: "*",
    methods: ["GET", "POST"],
  },
});

app.use(express.json());
app.use(cors());

const chatRoutes = require("./routes/chatRoutes");
const messageRoutes = require("./routes/messageRoutes");
const attachmentRoutes = require("./routes/attachmentRoutes");

app.use("/api/v1", chatRoutes);
app.use("/api/v1", messageRoutes);
app.use("/api/v1", attachmentRoutes);

const getAIResponse = async (userInput) => {
  return "Ai Response";
  try {
    const response = await axios.post("http://localhost:8090/api/ai_model", {
      user_input: userInput,
    });

    return response.data.reply;
  } catch (error) {
    console.error("Error in AI service request:", error);
    return "Sorry, I couldn't process your request.";
  }
};

const messageService = require("./services/messageService");

io.on("connection", (socket) => {
  console.log("a user connected");

  socket.on("send_message", async (data) => {
    console.log("User message:", data.message);

    const aiResponse = await getAIResponse(data.message);

    // const message = await messageService.createMessage(
    //   data.chatId,
    //   "user",
    //   data.message
    // );

    // const aiMessage = await messageService.createMessage(
    //   data.chatId,
    //   "model",
    //   aiResponse
    // );

    socket.emit("receive_message", { message: aiResponse });
  });

  socket.on("disconnect", () => {
    console.log("user disconnected");
  });
});

server.listen(8082, () => {
  console.log("listening on *:8082");
});
