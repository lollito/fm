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

// Infrastructure API
export const getInfrastructureOverview = (clubId) => api.get('/infrastructure/club/' + clubId + '/overview');

export const getAvailableUpgrades = (clubId, facilityType) => api.get('/infrastructure/club/' + clubId + '/upgrades/' + facilityType);

export const startUpgrade = (clubId, data) => api.post('/infrastructure/club/' + clubId + '/upgrade/start', data);

export const completeUpgrade = (upgradeId) => api.post('/infrastructure/upgrade/' + upgradeId + '/complete');

export const getMaintenanceSchedule = (clubId) => api.get('/infrastructure/club/' + clubId + '/maintenance/schedule');

export const scheduleMaintenance = (clubId, data) => api.post('/infrastructure/club/' + clubId + '/maintenance/schedule', data);
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

// Training API
export const getTrainingPlan = (teamId) => api.get('/training/plan/' + teamId);
export const updateTrainingPlan = (teamId, data) => api.put('/training/plan/' + teamId, data);
export const getTrainingHistory = (teamId, page = 0, size = 20) => api.get('/training/history/' + teamId + '?page=' + page + '&size=' + size);
export const getSessionResults = (sessionId) => api.get('/training/session/' + sessionId + '/results');
export const createManualTrainingSession = (teamId, data) => api.post('/training/session/manual/' + teamId, data);
export const getClub = (id) => api.get('/club/' + id);
// Scouting API
export const getClubScouts = (clubId) => api.get('/scouting/club/' + clubId + '/scouts');
export const assignPlayerScouting = (data) => api.post('/scouting/assignment/player', data);
export const getClubAssignments = (clubId, status = null) => {
    const params = new URLSearchParams();
    if (status) params.append('status', status);
    return api.get('/scouting/club/' + clubId + '/assignments?' + params.toString());
};
export const getPlayerScoutingStatus = (playerId, clubId) => api.get('/scouting/player/' + playerId + '/status/' + clubId);
export const getRevealedPlayerInfo = (playerId, clubId) => api.get('/scouting/player/' + playerId + '/revealed/' + clubId);
export const getScoutingReports = (clubId, page = 0, size = 20, recommendation = null) => {
    const params = new URLSearchParams();
    params.append('page', page);
    params.append('size', size);
    if (recommendation) params.append('recommendation', recommendation);
    return api.get('/scouting/club/' + clubId + '/reports?' + params.toString());
};
export const getScoutingRecommendations = (clubId) => api.get('/scouting/club/' + clubId + '/recommendations');
export const addToWatchlist = (reportId, notes) => api.post('/scouting/report/' + reportId + '/watchlist', { notes });
export const cancelAssignment = (assignmentId) => api.post('/scouting/assignment/' + assignmentId + '/cancel');
