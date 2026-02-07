import React, { useState, useEffect } from 'react';
import {
    getDebugDashboard,
    advanceSeason,
    simulateMatches,
    modifyPlayerStats,
    adjustFinances
} from '../services/api';
import { useToast } from '../context/ToastContext';
import '../styles/DebugTools.css'; // Assuming I'll create some CSS

const DebugTools = () => {
    const [dashboard, setDashboard] = useState(null);
    const [activeTab, setActiveTab] = useState('overview');
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadDebugDashboard();
    }, []);

    const loadDebugDashboard = async () => {
        try {
            const response = await getDebugDashboard();
            setDashboard(response.data);
        } catch (error) {
            console.error('Error loading debug dashboard:', error);
        } finally {
            setLoading(false);
        }
    };

    if (loading) return <div className="loading">Loading debug tools...</div>;
    if (!dashboard) return <div className="error">Failed to load dashboard</div>;

    return (
        <div className="debug-tools">
            <div className="debug-header">
                <h1>Debug Tools</h1>
                <div className="warning-banner">
                    <i className="fas fa-exclamation-triangle"></i>
                    <span>Warning: These tools can modify game state. Use with caution!</span>
                </div>
            </div>

            <div className="debug-tabs">
                <button
                    className={activeTab === 'overview' ? 'active' : ''}
                    onClick={() => setActiveTab('overview')}
                >
                    Overview
                </button>
                <button
                    className={activeTab === 'season' ? 'active' : ''}
                    onClick={() => setActiveTab('season')}
                >
                    Season Control
                </button>
                <button
                    className={activeTab === 'matches' ? 'active' : ''}
                    onClick={() => setActiveTab('matches')}
                >
                    Match Simulation
                </button>
                <button
                    className={activeTab === 'players' ? 'active' : ''}
                    onClick={() => setActiveTab('players')}
                >
                    Player Tools
                </button>
                <button
                    className={activeTab === 'finances' ? 'active' : ''}
                    onClick={() => setActiveTab('finances')}
                >
                    Financial Tools
                </button>
                <button
                    className={activeTab === 'testing' ? 'active' : ''}
                    onClick={() => setActiveTab('testing')}
                >
                    Testing
                </button>
                <button
                    className={activeTab === 'monitoring' ? 'active' : ''}
                    onClick={() => setActiveTab('monitoring')}
                >
                    Monitoring
                </button>
            </div>

            <div className="debug-content">
                {activeTab === 'overview' && (
                    <DebugOverview dashboard={dashboard} />
                )}
                {activeTab === 'season' && (
                    <SeasonControl onRefresh={loadDebugDashboard} />
                )}
                {activeTab === 'matches' && (
                    <MatchSimulation onRefresh={loadDebugDashboard} />
                )}
                {activeTab === 'players' && (
                    <PlayerTools onRefresh={loadDebugDashboard} />
                )}
                {activeTab === 'finances' && (
                    <FinancialTools onRefresh={loadDebugDashboard} />
                )}
                {activeTab === 'testing' && (
                    <TestingTools dashboard={dashboard} />
                )}
                {activeTab === 'monitoring' && (
                    <MonitoringTools dashboard={dashboard} />
                )}
            </div>
        </div>
    );
};

const DebugOverview = ({ dashboard }) => {
    return (
        <div className="debug-overview">
            <div className="system-health">
                <h2>System Health</h2>
                <div className="health-grid">
                    <div className="health-card">
                        <h3>Memory Usage</h3>
                        <div className="progress-bar">
                            <div
                                className="progress-fill"
                                style={{ width: dashboard.systemHealth.memoryUsagePercent + '%' }}
                            ></div>
                        </div>
                        <span>{dashboard.systemHealth.memoryUsagePercent.toFixed(1)}%</span>
                        <small>{dashboard.systemHealth.usedMemoryMb}MB / {dashboard.systemHealth.totalMemoryMb}MB</small>
                    </div>

                    <div className="health-card">
                        <h3>Active Threads</h3>
                        <span className="metric-value">{dashboard.systemHealth.activeThreads}</span>
                    </div>

                    <div className="health-card">
                        <h3>DB Connections</h3>
                        <span className="metric-value">{dashboard.systemHealth.databaseConnectionsActive}</span>
                    </div>

                    <div className="health-card">
                        <h3>System Uptime</h3>
                        <span className="metric-value">{dashboard.systemHealth.systemUptime}h</span>
                    </div>
                </div>
            </div>

            <div className="recent-actions">
                <h2>Recent Debug Actions</h2>
                <div className="actions-list">
                    {dashboard.recentDebugActions.map(action => (
                        <div key={action.id} className="action-item">
                            <div className="action-icon">
                                <i className={getActionIcon(action.actionType)}></i>
                            </div>
                            <div className="action-content">
                                <div className="action-name">{action.actionName}</div>
                                <div className="action-description">{action.actionDescription}</div>
                                <div className="action-meta">
                                    <span className="action-user">{action.adminUsername}</span>
                                    <span className="action-time">
                                        {new Date(action.executedAt).toLocaleString()}
                                    </span>
                                    <span className={'action-status ' + action.status.toLowerCase()}>
                                        {action.status}
                                    </span>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            </div>

            <div className="system-snapshots">
                <h2>System Snapshots</h2>
                <div className="snapshots-list">
                    {dashboard.systemSnapshots.map(snapshot => (
                        <div key={snapshot.id} className="snapshot-item">
                            <div className="snapshot-info">
                                <h4>{snapshot.snapshotName}</h4>
                                <p>{snapshot.description}</p>
                                <div className="snapshot-meta">
                                    <span>Created: {new Date(snapshot.createdAt).toLocaleString()}</span>
                                    <span>By: {snapshot.createdBy}</span>
                                    <span className={'status ' + snapshot.status.toLowerCase()}>
                                        {snapshot.status}
                                    </span>
                                </div>
                            </div>
                            <div className="snapshot-actions">
                                {snapshot.isRestorable && snapshot.status === 'READY' && (
                                    <button className="btn-warning btn-sm">Restore</button>
                                )}
                                <button className="btn-danger btn-sm">Delete</button>
                            </div>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
};

const SeasonControl = ({ onRefresh }) => {
    const { showToast } = useToast();
    const [advanceForm, setAdvanceForm] = useState({
        skipRemainingMatches: false,
        generateNewPlayers: true,
        processTransfers: true
    });
    const [loading, setLoading] = useState(false);

    const handleAdvanceSeason = async () => {
        if (!window.confirm('Are you sure you want to advance the season? This action cannot be undone.')) {
            return;
        }

        setLoading(true);
        try {
            const response = await advanceSeason(advanceForm);
            if (response.data.success) {
                showToast('Season advanced successfully!', 'success');
                onRefresh();
            } else {
                showToast('Failed to advance season: ' + response.data.message, 'error');
            }
        } catch (error) {
            console.error('Error advancing season:', error);
            showToast('Error advancing season: ' + error.message, 'error');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="season-control">
            <div className="control-section">
                <h2>Season Advancement</h2>
                <div className="form-group">
                    <label>
                        <input
                            type="checkbox"
                            checked={advanceForm.skipRemainingMatches}
                            onChange={(e) => setAdvanceForm({
                                ...advanceForm,
                                skipRemainingMatches: e.target.checked
                            })}
                        />
                        Skip remaining matches
                    </label>
                </div>
                <div className="form-group">
                    <label>
                        <input
                            type="checkbox"
                            checked={advanceForm.generateNewPlayers}
                            onChange={(e) => setAdvanceForm({
                                ...advanceForm,
                                generateNewPlayers: e.target.checked
                            })}
                        />
                        Generate new players
                    </label>
                </div>
                <div className="form-group">
                    <label>
                        <input
                            type="checkbox"
                            checked={advanceForm.processTransfers}
                            onChange={(e) => setAdvanceForm({
                                ...advanceForm,
                                processTransfers: e.target.checked
                            })}
                        />
                        Process pending transfers
                    </label>
                </div>
                <button
                    className="btn-danger"
                    onClick={handleAdvanceSeason}
                    disabled={loading}
                >
                    {loading ? 'Advancing...' : 'Advance Season'}
                </button>
            </div>
        </div>
    );
};

const MatchSimulation = ({ onRefresh }) => {
    const { showToast } = useToast();
    const [matchIds, setMatchIds] = useState('');
    const [forceResult, setForceResult] = useState('');
    const [loading, setLoading] = useState(false);

    const handleSimulateMatches = async () => {
        if (!matchIds.trim()) {
            showToast('Please enter match IDs', 'warning');
            return;
        }

        const ids = matchIds.split(',').map(id => parseInt(id.trim())).filter(id => !isNaN(id));
        if (ids.length === 0) {
            showToast('Please enter valid match IDs', 'warning');
            return;
        }

        setLoading(true);
        try {
            const request = {
                matchIds: ids,
                forceResult: forceResult || null
            };

            const response = await simulateMatches(request);
            if (response.data.success) {
                showToast('Successfully simulated ' + ids.length + ' matches!', 'success');
                onRefresh();
            } else {
                showToast('Failed to simulate matches: ' + response.data.message, 'error');
            }
        } catch (error) {
            console.error('Error simulating matches:', error);
            showToast('Error simulating matches: ' + error.message, 'error');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="match-simulation">
            <div className="control-section">
                <h2>Match Simulation</h2>
                <div className="form-group">
                    <label>Match IDs (comma-separated)</label>
                    <input
                        type="text"
                        value={matchIds}
                        onChange={(e) => setMatchIds(e.target.value)}
                        placeholder="1, 2, 3, 4"
                    />
                </div>
                <div className="form-group">
                    <label>Force Result (optional)</label>
                    <select
                        value={forceResult}
                        onChange={(e) => setForceResult(e.target.value)}
                    >
                        <option value="">Natural simulation</option>
                        <option value="HOME_WIN">Home team wins</option>
                        <option value="AWAY_WIN">Away team wins</option>
                        <option value="DRAW">Draw</option>
                    </select>
                </div>
                <button
                    className="btn-primary"
                    onClick={handleSimulateMatches}
                    disabled={loading}
                >
                    {loading ? 'Simulating...' : 'Simulate Matches'}
                </button>
            </div>
        </div>
    );
};

const PlayerTools = ({ onRefresh }) => {
    return <div>Player Tools (Coming Soon)</div>;
};

const FinancialTools = ({ onRefresh }) => {
    return <div>Financial Tools (Coming Soon)</div>;
};

const TestingTools = ({ dashboard }) => {
    return (
        <div>
            <h2>Active Test Scenarios</h2>
            {dashboard.activeTestScenarios && dashboard.activeTestScenarios.length > 0 ? (
                <ul>
                    {dashboard.activeTestScenarios.map(s => <li key={s.id}>{s.scenarioName}</li>)}
                </ul>
            ) : (
                <p>No active scenarios.</p>
            )}
        </div>
    );
};

const MonitoringTools = ({ dashboard }) => {
     return <div>Monitoring Tools (Coming Soon)</div>;
};

const getActionIcon = (actionType) => {
    switch (actionType) {
        case 'ADVANCE_SEASON': return 'fas fa-fast-forward';
        case 'SIMULATE_MATCHES': return 'fas fa-play';
        case 'MODIFY_PLAYER_STATS': return 'fas fa-user-edit';
        case 'ADJUST_FINANCES': return 'fas fa-dollar-sign';
        case 'FORCE_TRANSFERS': return 'fas fa-exchange-alt';
        case 'RESET_LEAGUE': return 'fas fa-undo';
        case 'GENERATE_DATA': return 'fas fa-database';
        default: return 'fas fa-cog';
    }
};

export default DebugTools;
