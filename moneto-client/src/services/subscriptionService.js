import api from './api';

export const getSubscriptionStatus = async () => {
    const response = await api.get('/subscription/status');
    return response.data;
};

export const subscribe = async (tier, cardLast4) => {
    const params = { tier };
    if (cardLast4) params.cardLast4 = cardLast4;
    const response = await api.post('/subscription/subscribe', null, { params });
    return response.data;
};

export const cancelSubscription = async () => {
    await api.post('/subscription/cancel');
};

export const getPlans = async () => {
    const response = await api.get('/subscription/plans');
    return response.data;
};