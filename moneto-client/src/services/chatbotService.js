import api from './api';

export const askChatbot = async (question) => {
    const response = await api.post('/chatbot', { question });
    return response.data.answer;
};