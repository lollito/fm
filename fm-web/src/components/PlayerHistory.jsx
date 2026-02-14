import React, { useState, useEffect } from 'react';
import { getPlayerHistory } from '../services/api';
import { LineChart, Line, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

const PlayerHistory = ({ playerId }) => {
    const [playerHistory, setPlayerHistory] = useState(null);
    const [selectedTab, setSelectedTab] = useState('overview');
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (playerId) {
            loadPlayerHistory();
        }
    }, [playerId]);

    const loadPlayerHistory = async () => {
        try {
            const response = await getPlayerHistory(playerId);
            setPlayerHistory(response.data);
        } catch (error) {
            console.error('Error loading player history:', error);
        } finally {
            setLoading(false);
        }
    };

    const getChartData = () => {
        if (!playerHistory?.seasonStats) return [];
        return [...playerHistory.seasonStats].reverse().map(stat => ({
            season: `${stat.season.startYear}/${stat.season.endYear}`,
            goals: stat.goals,
            assists: stat.assists,
            matches: stat.matchesPlayed
        }));
    };

    const getAchievementIcon = (type) => {
        const icons = {
            MILESTONE: '🏆',
            PERFORMANCE: '⭐',
            TEAM_SUCCESS: '🥇',
            INDIVIDUAL_AWARD: '🏅',
            RECORD: '📈'
        };
        return icons[type] || '🎯';
    };

    const getTransferTypeColor = (type) => {
        const colors = {
            PURCHASE: '#4caf50',
            LOAN: '#ff9800',
            FREE_TRANSFER: '#2196f3',
            YOUTH_PROMOTION: '#9c27b0',
            RETIREMENT: '#f44336'
        };
        return colors[type] || '#a0a0b0';
    };

    if (loading) return <div className="p-4">Loading player history...</div>;

    // Destructure safely with fallback
    const { player, careerStats, seasonStats, achievements, transferHistory } = playerHistory || {};

    // Guard clause for missing player data to prevent runtime errors
    if (!player) return <div className="p-4">Player history not found</div>;

    const chartData = getChartData();

    return (
        <div className="player-history container-fluid">
            <div className="player-header mb-4">
                <h2>{player.name} {player.surname}</h2>
                <div className="player-basic-info text-muted">
                    <span className="me-3">Age: {player.age}</span>
                    <span className="me-3">Position: {player.role}</span>
                    <span className="me-3">Current Club: {playerHistory.seasonStats?.[0]?.club?.name || 'Unknown'}</span>
                </div>
            </div>

            <div className="career-overview mb-4">
                <div className="row">
                   <div className="col-4 col-md-2 mb-3">
                        <div className="card text-center h-100">
                            <div className="card-body">
                                <h6 className="text-muted mb-2">Career Matches</h6>
                                <span className="h4 text-primary">{careerStats.totalMatchesPlayed}</span>
                            </div>
                        </div>
                   </div>
                   <div className="col-4 col-md-2 mb-3">
                        <div className="card text-center h-100">
                            <div className="card-body">
                                <h6 className="text-muted mb-2">Career Goals</h6>
                                <span className="h4 text-primary">{careerStats.totalGoals}</span>
                            </div>
                        </div>
                   </div>
                   <div className="col-4 col-md-2 mb-3">
                        <div className="card text-center h-100">
                             <div className="card-body">
                                <h6 className="text-muted mb-2">Career Assists</h6>
                                <span className="h4 text-primary">{careerStats.totalAssists}</span>
                            </div>
                        </div>
                   </div>
                   <div className="col-4 col-md-2 mb-3">
                        <div className="card text-center h-100">
                             <div className="card-body">
                                <h6 className="text-muted mb-2">Clubs Played</h6>
                                <span className="h4 text-primary">{careerStats.clubsPlayed}</span>
                            </div>
                        </div>
                   </div>
                   <div className="col-4 col-md-2 mb-3">
                        <div className="card text-center h-100">
                             <div className="card-body">
                                <h6 className="text-muted mb-2">League Titles</h6>
                                <span className="h4 text-primary">{careerStats.leagueTitles}</span>
                            </div>
                        </div>
                   </div>
                   <div className="col-4 col-md-2 mb-3">
                        <div className="card text-center h-100">
                             <div className="card-body">
                                <h6 className="text-muted mb-2">Highest Transfer</h6>
                                <span className="h4 text-primary">${careerStats.highestTransferValue?.toLocaleString()}</span>
                            </div>
                        </div>
                   </div>
                </div>
            </div>

            <ul className="nav nav-tabs" role="tablist">
                {['overview', 'seasons', 'achievements', 'transfers'].map(tab => (
                    <li className="nav-item" role="presentation" key={tab}>
                        <button
                            className={`nav-link ${selectedTab === tab ? 'active' : ''}`}
                            onClick={() => setSelectedTab(tab)}
                            role="tab"
                            aria-selected={selectedTab === tab}
                            aria-controls={`${tab}-panel`}
                            id={`${tab}-tab`}
                        >
                            {tab.charAt(0).toUpperCase() + tab.slice(1)}
                            {tab === 'achievements' && ` (${achievements.length})`}
                        </button>
                    </li>
                ))}
            </ul>

            <div className="tab-content mt-3">
                {selectedTab === 'overview' && (
                    <div
                        className="overview-tab"
                        role="tabpanel"
                        id="overview-panel"
                        aria-labelledby="overview-tab"
                    >
                        <div className="row mb-4">
                            <div className="col-12 col-md-6 mb-4">
                                <div className="card h-100">
                                    <div className="card-header">Goals per Season</div>
                                    <div className="card-body" style={{ height: '300px' }}>
                                        <ResponsiveContainer width="100%" height="100%">
                                            <LineChart data={chartData}>
                                                <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.1)" />
                                                <XAxis dataKey="season" stroke="#a0a0b0" />
                                                <YAxis stroke="#a0a0b0" />
                                                <Tooltip
                                                    contentStyle={{ backgroundColor: '#1e1136', border: '1px solid rgba(161, 85, 255, 0.2)', color: '#f0f0f5' }}
                                                />
                                                <Legend />
                                                <Line type="monotone" dataKey="goals" stroke="#a155ff" strokeWidth={2} activeDot={{ r: 8 }} />
                                            </LineChart>
                                        </ResponsiveContainer>
                                    </div>
                                </div>
                            </div>

                            <div className="col-12 col-md-6 mb-4">
                                <div className="card h-100">
                                    <div className="card-header">Performance Overview</div>
                                    <div className="card-body" style={{ height: '300px' }}>
                                        <ResponsiveContainer width="100%" height="100%">
                                            <BarChart data={chartData}>
                                                <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.1)" />
                                                <XAxis dataKey="season" stroke="#a0a0b0" />
                                                <YAxis stroke="#a0a0b0" />
                                                <Tooltip
                                                    contentStyle={{ backgroundColor: '#1e1136', border: '1px solid rgba(161, 85, 255, 0.2)', color: '#f0f0f5' }}
                                                />
                                                <Legend />
                                                <Bar dataKey="goals" fill="#ff4081" />
                                                <Bar dataKey="assists" fill="#a155ff" />
                                                <Bar dataKey="matches" fill="#ffd600" />
                                            </BarChart>
                                        </ResponsiveContainer>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div className="card">
                            <div className="card-header">Career Records</div>
                            <div className="card-body">
                                <div className="row">
                                    <div className="col-6 col-md-3 mb-3">
                                        <div className="text-muted small">Most Goals in Season</div>
                                        <div className="h5">{careerStats.mostGoalsInSeason}</div>
                                    </div>
                                    <div className="col-6 col-md-3 mb-3">
                                        <div className="text-muted small">Most Assists in Season</div>
                                        <div className="h5">{careerStats.mostAssistsInSeason}</div>
                                    </div>
                                    <div className="col-6 col-md-3 mb-3">
                                        <div className="text-muted small">Highest Season Rating</div>
                                        <div className="h5">{careerStats.highestSeasonRating?.toFixed(2)}</div>
                                    </div>
                                    <div className="col-6 col-md-3 mb-3">
                                        <div className="text-muted small">Longest Goal Streak</div>
                                        <div className="h5">{careerStats.longestGoalStreak} matches</div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                )}

                {selectedTab === 'seasons' && (
                    <div
                        className="seasons-tab card"
                        role="tabpanel"
                        id="seasons-panel"
                        aria-labelledby="seasons-tab"
                    >
                        <div className="card-body p-0">
                            <div className="table-responsive">
                                <table className="table mb-0">
                                    <thead>
                                        <tr>
                                            <th>Season</th>
                                            <th>Club</th>
                                            <th>League</th>
                                            <th>Matches</th>
                                            <th>Goals</th>
                                            <th>Assists</th>
                                            <th>Rating</th>
                                            <th>Cards</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {seasonStats.map(stat => (
                                            <tr key={stat.id}>
                                                <td>{stat.season.startYear}/{stat.season.endYear}</td>
                                                <td>{stat.club?.name}</td>
                                                <td>{stat.league?.name}</td>
                                                <td>{stat.matchesPlayed}</td>
                                                <td>{stat.goals}</td>
                                                <td>{stat.assists}</td>
                                                <td>{stat.averageRating?.toFixed(2)}</td>
                                                <td>
                                                    <span className="me-2" title="Yellow Cards">🟨 {stat.yellowCards}</span>
                                                    <span className="text-danger" title="Red Cards">🟥 {stat.redCards}</span>
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                )}

                {selectedTab === 'achievements' && (
                    <div
                        className="achievements-tab"
                        role="tabpanel"
                        id="achievements-panel"
                        aria-labelledby="achievements-tab"
                    >
                        <div className="row">
                            {achievements.map(achievement => (
                                <div key={achievement.id} className="col-12 col-md-6 col-lg-4 mb-4">
                                    <div className="card h-100">
                                        <div className="card-body d-flex align-items-start">
                                            <div className="achievement-icon me-3" style={{ fontSize: '2rem' }}>
                                                {getAchievementIcon(achievement.type)}
                                            </div>
                                            <div>
                                                <h5 className="card-title mb-1">{achievement.title}</h5>
                                                <p className="card-text text-muted small mb-2">{achievement.description}</p>
                                                <div className="text-dim small">
                                                    <div>{new Date(achievement.dateAchieved).toLocaleDateString()}</div>
                                                    <div>{achievement.club?.name}</div>
                                                    {achievement.season && <div>{achievement.season.startYear}/{achievement.season.endYear}</div>}
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                )}

                {selectedTab === 'transfers' && (
                    <div
                        className="transfers-tab"
                        role="tabpanel"
                        id="transfers-panel"
                        aria-labelledby="transfers-tab"
                    >
                        <div className="card">
                            <div className="card-body">
                                {transferHistory.map((transfer, index) => (
                                    <div key={transfer.id} className={`d-flex align-items-center py-3 ${index !== transferHistory.length - 1 ? 'border-bottom' : ''}`} style={{ borderColor: 'rgba(161, 85, 255, 0.1)' }}>
                                        <div className="me-4" style={{ minWidth: '100px', fontWeight: 'bold' }}>
                                            {new Date(transfer.transferDate).toLocaleDateString()}
                                        </div>
                                        <div className="flex-grow-1">
                                            <div className="d-flex align-items-center mb-1">
                                                <span className="text-primary fw-bold">{transfer.fromClub?.name || 'Youth Academy'}</span>
                                                <span className="mx-3 text-muted">→</span>
                                                <span className="text-primary fw-bold">{transfer.toClub?.name}</span>
                                            </div>
                                            <div className="d-flex gap-3 small text-muted">
                                                <span style={{ color: getTransferTypeColor(transfer.transferType), fontWeight: 'bold' }}>
                                                    {transfer.transferType?.replace('_', ' ')}
                                                </span>
                                                {transfer.transferFee > 0 && (
                                                    <span>
                                                        ${transfer.transferFee.toLocaleString()}
                                                    </span>
                                                )}
                                                <span>
                                                    Season: {transfer.season?.startYear}/{transfer.season?.endYear}
                                                </span>
                                            </div>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

export default PlayerHistory;
