import api from './api';

export const getSubscriptionStatus = async () => {
    const response = await api.get('/subscription/status');
    return response.data;
};

export const subscribe = async (tier) => {
    const response = await api.post('/subscription/subscribe', null, { params: { tier } });
    return response.data;
};

export const cancelSubscription = async () => {
    await api.post('/subscription/cancel');
};