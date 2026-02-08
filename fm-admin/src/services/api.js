import axios from 'axios';

const API_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_URL,
  withCredentials: true,
});

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
export const getPlayer = (id) => api.get('/admin/debug/players/' + id);
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

export const getServers = () => api.get('/server/findAll');
export const createServer = (serverName) => api.post('/server/?serverName=' + serverName);
export const forceNextDay = () => api.post('/server/next');
export const deleteServer = (serverId) => api.delete('/server/?serverId=' + serverId);

export const getAllLiveMatches = () => api.get('/live-match/all');
export const forceFinishMatch = (id) => api.post('/live-match/' + id + '/finish');
export const resetMatch = (id) => api.post('/live-match/' + id + '/reset');

export const getQuests = (status) => api.get('/quests', { params: { status } });
export const claimQuest = (id) => api.post('/quests/' + id + '/claim');

export default api;
