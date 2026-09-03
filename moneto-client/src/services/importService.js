import api from './api';

export const importTransactions = async (file) => {
    const formData = new FormData();
    formData.append('file', file);
    const response = await api.post('/import', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
    });
    return response.data;
};