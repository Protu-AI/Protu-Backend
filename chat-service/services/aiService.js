const axios = require('axios');

const getAIResponse = async (chatId, hasAttachment) => {
  return 'hello, world';

  // try {
  //   const response = await axios.post('<BASE_URL>/protu/ai/data/process', {
  //     chat_id: chatId,
  //     has_attachment: hasAttachment
  //   });
  //   return response.data;
  // } catch (error) {
  //   console.error('Error fetching AI response:', error);
  //   throw new Error('Failed to get AI response');
  // }
};

module.exports = {
  getAIResponse
};
