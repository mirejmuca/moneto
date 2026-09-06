import api from './api';

export const getAllUsers = async () => {
    const response = await api.get('/admin/users');
    return response.data;
};

export const getStats = async () => {
    const response = await api.get('/admin/stats');
    return response.data;
};

export const deleteUser = async (userId) => {
    await api.delete(`/admin/users/${userId}`);
};

export const changeRole = async (userId, role) => {
    await api.put(`/admin/users/${userId}/role`, null, { params: { role } });
};

export const changeSubscription = async (userId, tier) => {
    await api.put(`/admin/users/${userId}/subscription`, null, { params: { tier } });
};