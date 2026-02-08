import api from './api';

export const getManagerProfile = () => api.get('/manager/profile');

export const unlockPerk = (perkId) => api.post('/manager/unlock-perk', { perkId });
