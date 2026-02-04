import axios from 'axios';

export const API_BASE_URL = 'http://localhost:8080';
const API_URL = API_BASE_URL + '/api';

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

// Sponsorship API
export const getSponsorshipDashboard = (clubId) => api.get('/sponsorship/club/' + clubId + '/dashboard');
export const generateOffers = (clubId) => api.post('/sponsorship/club/' + clubId + '/generate-offers');
export const acceptOffer = (offerId) => api.post('/sponsorship/offer/' + offerId + '/accept');
export const rejectOffer = (offerId) => api.post('/sponsorship/offer/' + offerId + '/reject');
export const negotiateOffer = (offerId, data) => api.post('/sponsorship/offer/' + offerId + '/negotiate', data);
export const getActiveDeals = (clubId) => api.get('/sponsorship/club/' + clubId + '/deals');
export const getPendingOffers = (clubId) => api.get('/sponsorship/club/' + clubId + '/offers');
// Injury API
export const getTeamInjuries = (teamId) => api.get('/injuries/team/' + teamId);
export const getClubInjuries = (clubId) => api.get('/injuries/club/' + clubId);
export const getPlayerInjuryHistory = (playerId) => api.get('/injuries/player/' + playerId + '/history');

export default api;

// Live Match API
export const startLiveMatch = (matchId) => api.post('/live-match/' + matchId + '/start');
export const getLiveMatch = (matchId) => api.get('/live-match/' + matchId);
export const getMatchEvents = (matchId, fromMinute = null) => {
    const params = new URLSearchParams();
    if (fromMinute !== null) params.append('fromMinute', fromMinute);
    return api.get('/live-match/' + matchId + '/events?' + params.toString());
};
export const joinLiveMatch = (matchId) => api.post('/live-match/' + matchId + '/join');
export const leaveLiveMatch = (matchId) => api.post('/live-match/' + matchId + '/leave');
export const getActiveLiveMatches = () => api.get('/live-match/active');
