import api from './api';

export const getAlerts = async () => {
    const response = await api.get('/alerts');
    return response.data;
};

export const createAlert = async (type, categoryId, threshold) => {
    const params = { type, threshold };
    if (categoryId) params.categoryId = categoryId;
    const response = await api.post('/alerts', null, { params });
    return response.data;
};

export const deleteAlert = async (alertId) => {
    await api.delete(`/alerts/${alertId}`);
};