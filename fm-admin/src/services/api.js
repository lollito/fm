import axios from 'axios';

const API_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_URL,
});

api.interceptors.request.use(
  (config) => {
    const user = JSON.parse(localStorage.getItem('user'));
    if (user && user.accessToken) {
      config.headers['Authorization'] = 'Bearer ' + user.accessToken;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

export const getAdminDashboard = () => api.get('/admin/dashboard');
export const getClubs = () => api.get('/admin/clubs');
export const createClub = (data) => api.post('/admin/clubs', data);
export const updateClub = (id, data) => api.put('/admin/clubs/' + id, data);
export const deleteClub = (id) => api.delete('/admin/clubs/' + id);
export const getAdminActions = (page, size) => api.get('/admin/actions', { params: { page, size } });

export const bulkUpdatePlayers = (data) => api.post('/admin/players/bulk-update', data);
export const generatePlayersForLeague = (leagueId, data) => api.post(`/admin/leagues/${leagueId}/generate-players`, data);
export const importData = (data) => api.post('/admin/import', data);
export const exportData = (data) => api.post('/admin/export', data);
export const getSystemConfiguration = (category) => api.get('/admin/config', { params: { category } });
export const updateSystemConfiguration = (id, newValue) => api.put(`/admin/config/${id}`, { newValue });

export default api;
