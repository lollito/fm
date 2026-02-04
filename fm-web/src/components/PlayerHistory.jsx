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
        return colors[type] || '#666';
    };

    if (loading) return <div>Loading player history...</div>;
    if (!playerHistory) return <div>Player history not found</div>;

    const { player, careerStats, seasonStats, achievements, transferHistory } = playerHistory;
    const chartData = getChartData();

    return (
        <div className="player-history" style={{ padding: '20px' }}>
            <div className="player-header">
                <h2>{player.name} {player.surname}</h2>
                <div className="player-basic-info">
                    <span style={{ marginRight: '15px' }}>Age: {player.age}</span>
                    <span style={{ marginRight: '15px' }}>Position: {player.role}</span>
                    <span style={{ marginRight: '15px' }}>Current Club: {playerHistory.seasonStats[0]?.club?.name || 'Unknown'}</span>
                </div>
            </div>

            <div className="career-overview" style={{ margin: '20px 0' }}>
                <div className="career-stats-grid" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))', gap: '15px' }}>
                    <div className="stat-card" style={statCardStyle}>
                        <h3>Career Matches</h3>
                        <span className="stat-value">{careerStats.totalMatchesPlayed}</span>
                    </div>
                    <div className="stat-card" style={statCardStyle}>
                        <h3>Career Goals</h3>
                        <span className="stat-value">{careerStats.totalGoals}</span>
                    </div>
                    <div className="stat-card" style={statCardStyle}>
                        <h3>Career Assists</h3>
                        <span className="stat-value">{careerStats.totalAssists}</span>
                    </div>
                    <div className="stat-card" style={statCardStyle}>
                        <h3>Clubs Played</h3>
                        <span className="stat-value">{careerStats.clubsPlayed}</span>
                    </div>
                    <div className="stat-card" style={statCardStyle}>
                        <h3>League Titles</h3>
                        <span className="stat-value">{careerStats.leagueTitles}</span>
                    </div>
                    <div className="stat-card" style={statCardStyle}>
                        <h3>Highest Transfer</h3>
                        <span className="stat-value">${careerStats.highestTransferValue?.toLocaleString()}</span>
                    </div>
                </div>
            </div>

            <div className="history-tabs" style={{ marginBottom: '20px' }}>
                {['overview', 'seasons', 'achievements', 'transfers'].map(tab => (
                    <button
                        key={tab}
                        className={selectedTab === tab ? 'active' : ''}
                        onClick={() => setSelectedTab(tab)}
                        style={{
                            padding: '10px 20px',
                            marginRight: '10px',
                            backgroundColor: selectedTab === tab ? '#007bff' : '#f8f9fa',
                            color: selectedTab === tab ? 'white' : 'black',
                            border: '1px solid #ddd',
                            cursor: 'pointer'
                        }}
                    >
                        {tab.charAt(0).toUpperCase() + tab.slice(1)}
                        {tab === 'achievements' && ` (${achievements.length})`}
                    </button>
                ))}
            </div>

            {selectedTab === 'overview' && (
                <div className="overview-tab">
                    <div className="charts-section" style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>
                        <div className="chart-container" style={{ height: '300px' }}>
                            <h3>Goals per Season</h3>
                            <ResponsiveContainer width="100%" height="100%">
                                <LineChart data={chartData}>
                                    <CartesianGrid strokeDasharray="3 3" />
                                    <XAxis dataKey="season" />
                                    <YAxis />
                                    <Tooltip />
                                    <Legend />
                                    <Line type="monotone" dataKey="goals" stroke="#8884d8" />
                                </LineChart>
                            </ResponsiveContainer>
                        </div>

                        <div className="chart-container" style={{ height: '300px' }}>
                            <h3>Performance Overview</h3>
                            <ResponsiveContainer width="100%" height="100%">
                                <BarChart data={chartData}>
                                    <CartesianGrid strokeDasharray="3 3" />
                                    <XAxis dataKey="season" />
                                    <YAxis />
                                    <Tooltip />
                                    <Legend />
                                    <Bar dataKey="goals" fill="#ff6384" />
                                    <Bar dataKey="assists" fill="#36a2eb" />
                                    <Bar dataKey="matches" fill="#ffce56" />
                                </BarChart>
                            </ResponsiveContainer>
                        </div>
                    </div>

                    <div className="career-records" style={{ marginTop: '30px' }}>
                        <h3>Career Records</h3>
                        <div className="records-grid" style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '10px' }}>
                            <div className="record-item">
                                <strong>Most Goals in Season: </strong>
                                <span>{careerStats.mostGoalsInSeason}</span>
                            </div>
                            <div className="record-item">
                                <strong>Most Assists in Season: </strong>
                                <span>{careerStats.mostAssistsInSeason}</span>
                            </div>
                            <div className="record-item">
                                <strong>Highest Season Rating: </strong>
                                <span>{careerStats.highestSeasonRating?.toFixed(2)}</span>
                            </div>
                            <div className="record-item">
                                <strong>Longest Goal Streak: </strong>
                                <span>{careerStats.longestGoalStreak} matches</span>
                            </div>
                        </div>
                    </div>
                </div>
            )}

            {selectedTab === 'seasons' && (
                <div className="seasons-tab">
                    <table className="seasons-table" style={{ width: '100%', borderCollapse: 'collapse' }}>
                        <thead>
                            <tr style={{ borderBottom: '2px solid #ddd' }}>
                                <th style={thStyle}>Season</th>
                                <th style={thStyle}>Club</th>
                                <th style={thStyle}>League</th>
                                <th style={thStyle}>Matches</th>
                                <th style={thStyle}>Goals</th>
                                <th style={thStyle}>Assists</th>
                                <th style={thStyle}>Rating</th>
                                <th style={thStyle}>Cards</th>
                            </tr>
                        </thead>
                        <tbody>
                            {seasonStats.map(stat => (
                                <tr key={stat.id} style={{ borderBottom: '1px solid #eee' }}>
                                    <td style={tdStyle}>{stat.season.startYear}/{stat.season.endYear}</td>
                                    <td style={tdStyle}>{stat.club?.name}</td>
                                    <td style={tdStyle}>{stat.league?.name}</td>
                                    <td style={tdStyle}>{stat.matchesPlayed}</td>
                                    <td style={tdStyle}>{stat.goals}</td>
                                    <td style={tdStyle}>{stat.assists}</td>
                                    <td style={tdStyle}>{stat.averageRating?.toFixed(2)}</td>
                                    <td style={tdStyle}>
                                        <span style={{ color: '#fbc02d', marginRight: '5px' }}>🟨{stat.yellowCards}</span>
                                        <span style={{ color: '#d32f2f' }}>🟥{stat.redCards}</span>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}

            {selectedTab === 'achievements' && (
                <div className="achievements-tab">
                    <div className="achievements-grid" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))', gap: '20px' }}>
                        {achievements.map(achievement => (
                            <div key={achievement.id} className="achievement-card" style={cardStyle}>
                                <div className="achievement-icon" style={{ fontSize: '2em', marginBottom: '10px' }}>
                                    {getAchievementIcon(achievement.type)}
                                </div>
                                <div className="achievement-info">
                                    <h4 style={{ margin: '0 0 5px 0' }}>{achievement.title}</h4>
                                    <p style={{ margin: '0 0 10px 0', fontSize: '0.9em', color: '#666' }}>{achievement.description}</p>
                                    <div className="achievement-details" style={{ fontSize: '0.8em', color: '#888' }}>
                                        <div>{new Date(achievement.dateAchieved).toLocaleDateString()}</div>
                                        <div>{achievement.club?.name}</div>
                                        {achievement.season && <div>{achievement.season.startYear}/{achievement.season.endYear}</div>}
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            )}

            {selectedTab === 'transfers' && (
                <div className="transfers-tab">
                    <div className="transfers-timeline">
                        {transferHistory.map(transfer => (
                            <div key={transfer.id} className="transfer-item" style={{ ...cardStyle, marginBottom: '10px', display: 'flex', alignItems: 'center' }}>
                                <div className="transfer-date" style={{ width: '100px', fontWeight: 'bold' }}>
                                    {new Date(transfer.transferDate).toLocaleDateString()}
                                </div>
                                <div className="transfer-details" style={{ flex: 1 }}>
                                    <div className="transfer-clubs" style={{ display: 'flex', alignItems: 'center', marginBottom: '5px' }}>
                                        <span className="from-club">{transfer.fromClub?.name || 'Youth Academy'}</span>
                                        <span className="transfer-arrow" style={{ margin: '0 10px' }}>→</span>
                                        <span className="to-club">{transfer.toClub?.name}</span>
                                    </div>
                                    <div className="transfer-info" style={{ display: 'flex', gap: '15px', fontSize: '0.9em' }}>
                                        <span
                                            className="transfer-type"
                                            style={{ color: getTransferTypeColor(transfer.transferType), fontWeight: 'bold' }}
                                        >
                                            {transfer.transferType?.replace('_', ' ')}
                                        </span>
                                        {transfer.transferFee > 0 && (
                                            <span className="transfer-fee">
                                                ${transfer.transferFee.toLocaleString()}
                                            </span>
                                        )}
                                    </div>
                                    <div className="transfer-season" style={{ fontSize: '0.8em', color: '#888', marginTop: '5px' }}>
                                        Season: {transfer.season?.startYear}/{transfer.season?.endYear}
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
};

const statCardStyle = {
    padding: '15px',
    backgroundColor: 'white',
    boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
    borderRadius: '8px',
    textAlign: 'center'
};

const cardStyle = {
    padding: '15px',
    backgroundColor: 'white',
    boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
    borderRadius: '8px'
};

const thStyle = {
    textAlign: 'left',
    padding: '10px'
};

const tdStyle = {
    padding: '10px'
};

export default PlayerHistory;
