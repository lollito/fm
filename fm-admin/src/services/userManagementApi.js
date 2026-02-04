import api from './api';

export const getUserDashboard = () => {
    return api.get('/admin/users/dashboard');
};

export const getUsers = (page, size, filters) => {
    const params = {
        page,
        size,
        ...filters
    };
    return api.get('/admin/users', { params });
};

export const createUser = (userData) => {
    return api.post('/admin/users', userData);
};

export const updateUser = (userId, userData) => {
    return api.put('/admin/users/' + userId, userData);
};

export const banUser = (userId, data) => {
    return api.post('/admin/users/' + userId + '/ban', data);
};

export const unbanUser = (userId) => {
    return api.post('/admin/users/' + userId + '/unban');
};

export const resetUserPassword = (userId) => {
    return api.post('/admin/users/' + userId + '/reset-password');
};

export const getUserActivities = (userId, page = 0, size = 20) => {
    return api.get('/admin/users/' + userId + '/activities', {
        params: { page, size }
    });
};

export const getUserSessions = (userId) => {
    return api.get('/admin/users/' + userId + '/sessions');
};

export const terminateSession = (sessionId) => {
    return api.post('/admin/users/sessions/' + sessionId + '/terminate');
};
