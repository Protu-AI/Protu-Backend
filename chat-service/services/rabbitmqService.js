const amqp = require('amqplib');
const { PrismaClient } = require('@prisma/client');
const prisma = new PrismaClient();

const RABBITMQ_URL = process.env.RABBITMQ_URL || 'amqp://localhost';
const USER_EVENTS_EXCHANGE = 'user.events.topic';
const USER_REPLICA_QUEUE = 'chat-service.user-replica.queue';

const USER_CREATED_KEY = 'user.created';
const USER_UPDATED_KEY = 'user.updated';
const USER_DELETED_KEY = 'user.deleted';
const USER_PATTERN = 'user.*';

async function connectRabbitMQ(retries = 5, delay = 5000) {
  for (let i = 0; i < retries; i++) {
    try {
      console.log(`Connecting to RabbitMQ at ${RABBITMQ_URL}...`);
      const connection = await amqp.connect(RABBITMQ_URL);
      const channel = await connection.createChannel();
      await channel.assertExchange(USER_EVENTS_EXCHANGE, 'topic', {
        durable: true
      });
      return channel;
    } catch (error) {
      console.error(`Failed to connect to RabbitMQ (attempt ${i + 1}):`, error);
      if (i < retries - 1) {
        await new Promise(res => setTimeout(res, delay));
      } else {
        throw error;
      }
    }
  }
}

async function listenForUserEvents(channel) {
  try {
    await channel.assertQueue(USER_REPLICA_QUEUE, { durable: true });
    await channel.bindQueue(
      USER_REPLICA_QUEUE,
      USER_EVENTS_EXCHANGE,
      USER_PATTERN
    );

    console.log(`Waiting for messages in ${USER_REPLICA_QUEUE}.`);
    console.log(
      `Listening for user events: ${USER_CREATED_KEY}, ${USER_UPDATED_KEY}, ${USER_DELETED_KEY}`
    );

    channel.consume(USER_REPLICA_QUEUE, async msg => {
      if (msg !== null) {
        try {
          const messageContent = msg.content.toString();
          const routingKey = msg.fields.routingKey;

          console.log(
            `Received message with routing key ${routingKey}: ${messageContent}`
          );

          const rabbitMessage = JSON.parse(messageContent);
          await processUserEvent(rabbitMessage, routingKey);

          channel.ack(msg);
        } catch (error) {
          console.error('Error processing message:', error);
          channel.nack(msg, false, true);
        }
      }
    });
  } catch (error) {
    console.error('Error setting up user events listener:', error);
    throw error;
  }
}

async function processUserEvent(rabbitMessage, routingKey) {
  const { data } = rabbitMessage;
  const userData = data;

  console.log(`Processing ${routingKey} event for user ${userData.publicId}`);

  switch (routingKey) {
    case USER_CREATED_KEY:
      await createOrUpdateUser(userData);
      break;
    case USER_UPDATED_KEY:
      await createOrUpdateUser(userData);
      break;
    case USER_DELETED_KEY:
      await deleteUser(userData.publicId);
      break;
    default:
      console.log(`Unknown routing key: ${routingKey}`);
  }
}

async function createOrUpdateUser(userData) {
  try {
    console.log('User data received:', JSON.stringify(userData));

    // Check if user already exists by publicId
    const existingUser = await prisma.user.findUnique({
      where: { publicId: userData.publicId }
    });

    if (existingUser) {
      // Only update roles, do not update id
      await prisma.user.update({
        where: { publicId: userData.publicId },
        data: {
          roles: userData.roles
        }
      });
      console.log(`User ${userData.publicId} updated successfully`);
    } else {
      // Create new user, include id if provided
      await prisma.user.create({
        data: {
          ...(userData.id ? { id: userData.id } : {}),
          publicId: userData.publicId,
          roles: userData.roles
        }
      });
      console.log(`User ${userData.publicId} created successfully`);
    }
  } catch (error) {
    console.error(`Error creating/updating user ${userData.publicId}:`, error);
    throw error;
  }
}

async function deleteUser(publicId) {
  try {
    await prisma.user.delete({
      where: { publicId }
    });
    console.log(`User ${publicId} deleted successfully`);
  } catch (error) {
    console.error(`Error deleting user ${publicId}:`, error);
    if (error.code !== 'P2025') {
      throw error;
    }
  }
}

module.exports = {
  connectRabbitMQ,
  listenForUserEvents
};
