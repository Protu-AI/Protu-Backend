const axios = require('axios');

const AI_BASE_URL =
  process.env.AI_SERVICE_URL || 'http://model-service-container:8000';

const getAIResponse = async (chatId, hasAttachment) => {
  try {
    const response = await axios.post(
      `${AI_BASE_URL}/protu/ai/data/process`,
      {
        chat_id: chatId,
        is_attached: hasAttachment
      },
      {
        headers: {
          'Content-Type': 'application/json'
        },
        timeout: 30000
      }
    );

    if (!response.data) {
      throw new Error('Empty response from AI service');
    }

    return response.data;
  } catch (error) {
    console.error('Error fetching AI response:', {
      message: error.message,
      status: error.response?.status,
      data: error.response?.data
    });

    throw new Error(
      error.response?.data?.message || 'Failed to get AI response'
    );
  }
};

module.exports = {
  getAIResponse
};
