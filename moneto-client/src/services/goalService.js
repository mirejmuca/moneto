import api from './api';

export const getGoals = async () => {
    const response = await api.get('/goals');
    return response.data;
};

export const getActiveGoals = async () => {
    const response = await api.get('/goals/active');
    return response.data;
};

export const createGoal = async (data) => {
    const response = await api.post('/goals', null, { params: data });
    return response.data;
};

export const updateGoal = async (id, data) => {
    const response = await api.put(`/goals/${id}`, null, { params: data });
    return response.data;
};

export const addToGoal = async (id, amount) => {
    const response = await api.put(`/goals/${id}/add`, null, { params: { amount } });
    return response.data;
};

export const cancelGoal = async (id) => {
    await api.delete(`/goals/${id}`);
};