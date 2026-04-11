import api from './api';

export const getTransactions = async () => {
    const response = await api.get('/transactions');
    return response.data;
};

export const createTransaction = async (data) => {
    const response = await api.post('/transactions', null, { params: data });
    return response.data;
};

export const updateTransaction = async (id, data) => {
    const response = await api.put(`/transactions/${id}`, null, { params: data });
    return response.data;
};

export const deleteTransaction = async (id) => {
    await api.delete(`/transactions/${id}`);
};
