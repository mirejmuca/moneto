import api from './api';

export const getProfile = async () => {
    const response = await api.get('/user/profile');
    return response.data;
};

export const updateProfile = async (data) => {
    const response = await api.put('/user/profile', null, { params: data });
    return response.data;
};

export const changePassword = async (data) => {
    const response = await api.put('/user/password', null, { params: data });
    return response.data;
};

export const updateSettings = async (data) => {
    const response = await api.put('/user/settings', null, { params: data });
    return response.data;
};

export const deleteAccount = async () => {
    await api.delete('/user/account');
};