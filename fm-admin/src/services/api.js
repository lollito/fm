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

export const getDebugDashboard = () => api.get('/admin/debug/dashboard');
export const advanceSeason = (data) => api.post('/admin/debug/season/advance', data);
export const simulateMatches = (data) => api.post('/admin/debug/matches/simulate', data);
export const modifyPlayerStats = (data) => api.post('/admin/debug/players/modify-stats', data);
export const adjustFinances = (data) => api.post('/admin/debug/finances/adjust', data);
export const getSystemSnapshots = () => api.get('/admin/debug/snapshots');
export const createSystemSnapshot = (data) => api.post('/admin/debug/snapshots', data);
export const getTestScenarios = () => api.get('/admin/debug/test-scenarios');
export const executeTestScenario = (id) => api.post('/admin/debug/test-scenarios/' + id + '/execute');
export const getPerformanceMetrics = (params) => api.get('/admin/debug/metrics', { params });
export const getDebugActions = (params) => api.get('/admin/debug/actions', { params });

export default api;
