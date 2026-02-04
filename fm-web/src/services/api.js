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

export const getPlayerHistory = (id) => api.get('/player-history/player/' + id);

// Financial API
export const getFinancialDashboard = (clubId) => api.get('/finance/club/' + clubId + '/dashboard');

export const createTransaction = (clubId, data) => api.post('/finance/club/' + clubId + '/transaction', data);

export const getTransactions = (clubId, page = 0, size = 20, type = null, category = null) => {
  const params = new URLSearchParams();
  params.append('page', page);
  params.append('size', size);
  if (type) params.append('type', type);
  if (category) params.append('category', category);
  return api.get('/finance/club/' + clubId + '/transactions?' + params.toString());
};

export const getFinancialReports = (clubId, reportType = null) => {
  const params = new URLSearchParams();
  if (reportType) params.append('reportType', reportType);
  return api.get('/finance/club/' + clubId + '/reports?' + params.toString());
};

export const generateReport = (clubId, data) => api.post('/finance/club/' + clubId + '/report/generate', data);

export const getSponsorshipDeals = (clubId) => api.get('/finance/club/' + clubId + '/sponsorships');

export default api;
