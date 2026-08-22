import api from './api';

export const getRecurring = async () => {
    const response = await api.get('/recurring');
    return response.data;
};

export const createRecurring = async (data) => {
    const response = await api.post('/recurring', null, { params: data });
    return response.data;
};

export const updateRecurring = async (id, data) => {
    const response = await api.put(`/recurring/${id}`, null, { params: data });
    return response.data;
};

export const cancelRecurring = async (id) => {
    await api.delete(`/recurring/${id}`);
};