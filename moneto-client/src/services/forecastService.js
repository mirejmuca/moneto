import api from './api';

export const getForecast = async () => {
    const response = await api.get('/forecast');
    return response.data;
};