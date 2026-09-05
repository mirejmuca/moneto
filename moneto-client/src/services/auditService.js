import api from './api';

export const getAuditLogs = async (page = 0, size = 50) => {
    const response = await api.get('/audit', { params: { page, size } });
    return response.data;
};

export const getMyActivity = async (page = 0, size = 50) => {
    const response = await api.get('/audit/my-activity', { params: { page, size } });
    return response.data;
};

export const getUserAudit = async (email, page = 0, size = 100) => {
    const response = await api.get('/audit/user', { params: { email, page, size } });
    return response.data;
};