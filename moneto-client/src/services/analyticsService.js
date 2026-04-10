import api from './api';

export const getMonthlySummary = async () => {
    const response = await api.get('/analytics/summary');
    return response.data;
};

export const getSpendingByCategory = async () => {
    const response = await api.get('/analytics/by-category');
    return response.data;
};

export const getMonthlyTrend = async (months = 6) => {
    const response = await api.get(`/analytics/trend?months=${months}`);
    return response.data;
};

export const getTopCategory = async () => {
    const response = await api.get('/analytics/top-category');
    return response.data;
};