import React, { useState, useEffect } from 'react';
import { getTrainingHistory, getSessionResults } from '../services/api';

const TrainingHistory = ({ teamId }) => {
    const [sessions, setSessions] = useState([]);
    const [selectedSession, setSelectedSession] = useState(null);
    const [sessionResults, setSessionResults] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (teamId) {
            loadTrainingHistory();
        }
    }, [teamId]);

    const loadTrainingHistory = async () => {
        try {
            const response = await getTrainingHistory(teamId);
            const content = response.data?.content || (Array.isArray(response.data) ? response.data : []);
            setSessions(content);
        } catch (error) {
            console.error('Error loading training history:', error);
            setSessions([]);
        } finally {
            setLoading(false);
        }
    };

    const loadSessionResults = async (sessionId) => {
        if (selectedSession === sessionId) {
             setSelectedSession(null);
             setSessionResults([]);
             return;
        }
        try {
            const response = await getSessionResults(sessionId);
            setSessionResults(response.data);
            setSelectedSession(sessionId);
        } catch (error) {
            console.error('Error loading session results:', error);
        }
    };

    const getPerformanceColor = (performance) => {
        const colors = {
            POOR: '#ff4444',
            AVERAGE: '#ffaa00',
            GOOD: '#44aa44',
            EXCELLENT: '#0088ff'
        };
        return colors[performance] || '#666';
    };

    if (loading) return <div>Loading training history...</div>;

    return (
        <div className="training-history">
            <h2>Training History</h2>

            <div className="sessions-list">
                {sessions.map(session => (
                    <div
                        key={session.id}
                        className={`session-card ${selectedSession === session.id ? 'selected' : ''}`}
                        onClick={() => loadSessionResults(session.id)}
                        style={{
                            border: '1px solid #ddd',
                            padding: '10px',
                            marginBottom: '10px',
                            cursor: 'pointer',
                            backgroundColor: selectedSession === session.id ? '#f0f8ff' : 'white'
                        }}
                    >
                        <div className="session-header" style={{ display: 'flex', justifyContent: 'space-between' }}>
                            <span className="session-date" style={{ fontWeight: 'bold' }}>
                                {session.startDate ? new Date(session.startDate).toLocaleDateString() : 'N/A'}
                            </span>
                            <span className="session-focus">{session.focus}</span>
                            <span className="session-intensity">{session.intensity}</span>
                        </div>
                        <div className="session-stats" style={{ fontSize: '0.9em', color: '#666', marginTop: '5px' }}>
                            <span style={{ marginRight: '15px' }}>Players trained: {session.playerCount !== undefined ? session.playerCount : 'N/A'}</span>
                            <span>Effectiveness: {session.effectivenessMultiplier ? Math.round(session.effectivenessMultiplier * 100) : 0}%</span>
                        </div>
                    </div>
                ))}
                {sessions.length === 0 && <div>No training sessions recorded yet.</div>}
            </div>

            {selectedSession && sessionResults.length > 0 && (
                <div className="session-results" style={{ marginTop: '20px' }}>
                    <h3>Session Results</h3>
                    <table className="results-table" style={{ width: '100%', borderCollapse: 'collapse' }}>
                        <thead>
                            <tr style={{ backgroundColor: '#f8f9fa' }}>
                                <th style={{ padding: '8px', textAlign: 'left', borderBottom: '1px solid #ddd' }}>Player</th>
                                <th style={{ padding: '8px', textAlign: 'left', borderBottom: '1px solid #ddd' }}>Attendance</th>
                                <th style={{ padding: '8px', textAlign: 'left', borderBottom: '1px solid #ddd' }}>Performance</th>
                                <th style={{ padding: '8px', textAlign: 'left', borderBottom: '1px solid #ddd' }}>Improvement</th>
                                <th style={{ padding: '8px', textAlign: 'left', borderBottom: '1px solid #ddd' }}>Fatigue</th>
                            </tr>
                        </thead>
                        <tbody>
                            {sessionResults.map(result => (
                                <tr key={result.id}>
                                    <td style={{ padding: '8px', borderBottom: '1px solid #ddd' }}>
                                        {result.player ? `${result.player.name} ${result.player.surname}` : 'Unknown Player'}
                                    </td>
                                    <td style={{ padding: '8px', borderBottom: '1px solid #ddd' }}>
                                        {result.attendanceRate ? Math.round(result.attendanceRate * 100) : 0}%
                                    </td>
                                    <td style={{ padding: '8px', borderBottom: '1px solid #ddd' }}>
                                        <span style={{ color: getPerformanceColor(result.performance), fontWeight: 'bold' }}>
                                            {result.performance}
                                        </span>
                                    </td>
                                    <td style={{ padding: '8px', borderBottom: '1px solid #ddd' }}>
                                        +{result.improvementGained ? result.improvementGained.toFixed(4) : '0.0000'}
                                    </td>
                                    <td style={{ padding: '8px', borderBottom: '1px solid #ddd' }}>
                                        -{result.fatigueGained ? result.fatigueGained.toFixed(2) : '0.00'}
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
};

export default TrainingHistory;
