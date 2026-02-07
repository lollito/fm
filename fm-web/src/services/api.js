import axios from 'axios';

export const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';
const API_URL = API_BASE_URL + '/api';

const api = axios.create({
  baseURL: API_URL,
  withCredentials: true,
});

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

// Staff API
export const getClubStaff = (clubId) => api.get('/staff/club/' + clubId);
export const getAvailableStaff = (role = null) => {
    const params = new URLSearchParams();
    if (role) params.append('role', role);
    return api.get('/staff/available?' + params.toString());
};
export const hireStaff = (data) => api.post('/staff/hire', data);
export const fireStaff = (staffId, data) => api.post('/staff/' + staffId + '/fire', data);
export const renewContract = (staffId, data) => api.post('/staff/' + staffId + '/renew', data);
export const getStaffBonuses = (clubId) => api.get('/staff/club/' + clubId + '/bonuses');
export const generateStaff = (role, count) => api.post('/staff/generate/' + role + '?count=' + count);

// Contract API
export const getPlayerContract = (playerId) => api.get('/contracts/player/' + playerId);
export const startContractNegotiation = (data) => api.post('/contracts/negotiate', data);
export const makeContractCounterOffer = (negotiationId, data) => api.post('/contracts/negotiate/' + negotiationId + '/offer', data);
export const acceptContractOffer = (negotiationId) => api.post('/contracts/negotiate/' + negotiationId + '/accept');
export const rejectContractOffer = (negotiationId, data) => api.post('/contracts/negotiate/' + negotiationId + '/reject', data);
export const getClubNegotiations = (clubId, status = null) => {
    const params = new URLSearchParams();
    if (status) params.append('status', status);
    return api.get('/contracts/club/' + clubId + '/negotiations?' + params.toString());
};
export const addPerformanceBonus = (contractId, data) => api.post('/contracts/' + contractId + '/bonus', data);
export const triggerReleaseClause = (playerId, data) => api.post('/contracts/release-clause/' + playerId, data);
export const getExpiringContracts = (monthsAhead = 6) => api.get('/contracts/expiring?monthsAhead=' + monthsAhead);
// Watchlist API
export const getClubWatchlist = (clubId) => api.get('/watchlist/club/' + clubId);
export const addPlayerToWatchlist = (clubId, playerId, data) => api.post('/watchlist/club/' + clubId + '/player/' + playerId, data);
export const removeFromWatchlist = (entryId) => api.delete('/watchlist/entry/' + entryId);
export const updateWatchlistEntry = (entryId, data) => api.put('/watchlist/entry/' + entryId, data);
export const getWatchlistNotifications = (clubId, unreadOnly = false) => api.get('/watchlist/club/' + clubId + '/notifications?unreadOnly=' + unreadOnly);
export const markNotificationAsRead = (notificationId) => api.post('/watchlist/notification/' + notificationId + '/read');
export const getWatchlistStats = (clubId) => api.get('/watchlist/club/' + clubId + '/stats');
export const getEntryUpdates = (entryId) => api.get('/watchlist/entry/' + entryId + '/updates');

// Loan API
export const createLoanProposal = (data) => api.post('/loans/proposal', data);
export const acceptLoanProposal = (proposalId) => api.post('/loans/proposal/' + proposalId + '/accept');
export const rejectLoanProposal = (proposalId, data) => api.post('/loans/proposal/' + proposalId + '/reject', data);
export const getActiveLoans = (clubId) => api.get('/loans/club/' + clubId + '/active');
export const recallPlayerFromLoan = (loanId, data) => api.post('/loans/agreement/' + loanId + '/recall', data);
export const activatePurchaseOption = (loanId) => api.post('/loans/agreement/' + loanId + '/purchase');
export const getLoanReviews = (loanId) => api.get('/loans/agreement/' + loanId + '/reviews');

export const getServers = () => api.get('/server/findAll');
