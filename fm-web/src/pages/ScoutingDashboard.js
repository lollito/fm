import React, { useState, useEffect, useContext } from 'react';
import { AuthContext } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import Layout from '../components/Layout';
import {
    getClubScouts,
    getClubAssignments,
    getScoutingReports,
    assignPlayerScouting,
    addToWatchlist
} from '../services/api';
import '../styles/ScoutingDashboard.css';

const ScoutingDashboard = () => {
    const { showToast } = useToast();
    const { user } = useContext(AuthContext);
    const [scouts, setScouts] = useState([]);
    const [assignments, setAssignments] = useState([]);
    const [reports, setReports] = useState([]);
    const [selectedTab, setSelectedTab] = useState('scouts');
    const [loading, setLoading] = useState(true);

    // Modal State
    const [showAssignModal, setShowAssignModal] = useState(false);
    const [selectedScout, setSelectedScout] = useState(null);
    const [targetPlayerId, setTargetPlayerId] = useState('');
    const [priority, setPriority] = useState(3);
    const [instructions, setInstructions] = useState('');

    const clubId = user?.clubId || user?.club?.id;

    useEffect(() => {
        if (clubId) {
            loadScoutingData();
        }
    }, [clubId]);

    const loadScoutingData = async () => {
        try {
            const [scoutsResponse, assignmentsResponse, reportsResponse] = await Promise.all([
                getClubScouts(clubId),
                getClubAssignments(clubId),
                getScoutingReports(clubId)
            ]);

            setScouts(scoutsResponse.data);
            setAssignments(assignmentsResponse.data);
            setReports(reportsResponse.data.content);
        } catch (error) {
            console.error('Error loading scouting data:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleAssignClick = (scout) => {
        setSelectedScout(scout);
        setShowAssignModal(true);
    };

    const handleAssignSubmit = async (e) => {
        e.preventDefault();
        try {
            await assignPlayerScouting({
                scoutId: selectedScout.id,
                playerId: parseInt(targetPlayerId),
                priority,
                instructions
            });
            setShowAssignModal(false);
            setTargetPlayerId('');
            setInstructions('');
            loadScoutingData(); // Reload data
            showToast('Assignment created successfully!', 'success');
        } catch (error) {
            console.error(error);
            showToast('Failed to assign scout: ' + (error.response?.data?.message || error.message), 'error');
        }
    };

    const handleAddToWatchlist = async (reportId) => {
        const notes = prompt("Enter notes for watchlist:");
        if (notes === null) return;

        try {
            await addToWatchlist(reportId, notes);
            showToast('Added to watchlist!', 'success');
        } catch (error) {
            console.error(error);
            showToast('Failed to add to watchlist', 'error');
        }
    };

    const getScoutStatusColor = (status) => {
        const colors = {
            ACTIVE: '#4caf50',
            INJURED: '#f44336',
            SUSPENDED: '#ff9800'
        };
        return colors[status] || '#666';
    };

    const getAssignmentStatusIcon = (status) => {
        const icons = {
            ASSIGNED: '📋',
            IN_PROGRESS: '🔍',
            COMPLETED: '✅',
            CANCELLED: '❌'
        };
        return icons[status] || '❓';
    };

    const getRecommendationColor = (recommendation) => {
        const colors = {
            AVOID: '#f44336',
            MONITOR: '#ff9800',
            CONSIDER: '#2196f3',
            RECOMMEND: '#4caf50',
            PRIORITY: '#9c27b0'
        };
        return colors[recommendation] || '#666';
    };

    const calculateAssignmentProgress = (assignment) => {
        const now = new Date();
        const assigned = new Date(assignment.assignedDate);
        const expected = new Date(assignment.expectedCompletionDate);

        if (assignment.status === 'COMPLETED') return 100;
        if (assignment.status === 'CANCELLED') return 0;

        const totalDuration = expected.getTime() - assigned.getTime();
        const elapsed = now.getTime() - assigned.getTime();

        return Math.min(100, Math.max(0, (elapsed / totalDuration) * 100));
    };

    if (loading) return <Layout><div>Loading scouting dashboard...</div></Layout>;

    return (
        <Layout>
            <div className="scouting-dashboard">
                <div className="dashboard-header">
                    <h2>Scouting Network</h2>
                    <div className="scouting-stats">
                        <div className="stat">
                            <span>Active Scouts:</span>
                            <span>{scouts.filter(s => s.status === 'ACTIVE').length}</span>
                        </div>
                        <div className="stat">
                            <span>Active Assignments:</span>
                            <span>{assignments.filter(a => a.status === 'IN_PROGRESS').length}</span>
                        </div>
                        <div className="stat">
                            <span>Completed Reports:</span>
                            <span>{reports.length}</span>
                        </div>
                    </div>
                </div>

                <div className="dashboard-tabs">
                    <button
                        className={selectedTab === 'scouts' ? 'active' : ''}
                        onClick={() => setSelectedTab('scouts')}
                    >
                        Scouts ({scouts.length})
                    </button>
                    <button
                        className={selectedTab === 'assignments' ? 'active' : ''}
                        onClick={() => setSelectedTab('assignments')}
                    >
                        Assignments ({assignments.length})
                    </button>
                    <button
                        className={selectedTab === 'reports' ? 'active' : ''}
                        onClick={() => setSelectedTab('reports')}
                    >
                        Reports ({reports.length})
                    </button>
                </div>

                {selectedTab === 'scouts' && (
                    <div className="scouts-section">
                        <div className="scouts-grid">
                            {scouts.map(scout => (
                                <div key={scout.id} className="scout-card">
                                    <div className="scout-header">
                                        <h4>{scout.name} {scout.surname}</h4>
                                        <span
                                            className="scout-status"
                                            style={{ color: getScoutStatusColor(scout.status) }}
                                        >
                                            {scout.status}
                                        </span>
                                    </div>

                                    <div className="scout-info">
                                        <div className="info-row">
                                            <span>Ability:</span>
                                            <span>{'⭐'.repeat(Math.ceil(scout.ability / 4))}</span>
                                        </div>
                                        <div className="info-row">
                                            <span>Specialization:</span>
                                            <span>{scout.specialization}</span>
                                        </div>
                                        <div className="info-row">
                                            <span>Region:</span>
                                            <span>{scout.regionName || 'Global'}</span>
                                        </div>
                                        <div className="info-row">
                                            <span>Experience:</span>
                                            <span>{scout.experience} years</span>
                                        </div>
                                        <div className="info-row">
                                            <span>Salary:</span>
                                            <span>${scout.monthlySalary?.toLocaleString()}/month</span>
                                        </div>
                                    </div>

                                    <div className="scout-assignments">
                                        <strong>Current Assignments:</strong>
                                        <span>{scout.assignments?.filter(a => a.status === 'IN_PROGRESS').length || 0}</span>
                                    </div>

                                    <div className="scout-actions">
                                        <button
                                            className="assign-btn"
                                            onClick={() => handleAssignClick(scout)}
                                            disabled={scout.status !== 'ACTIVE'}
                                        >
                                            New Assignment
                                        </button>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                )}

                {selectedTab === 'assignments' && (
                    <div className="assignments-section">
                        <div className="assignments-list">
                            {assignments.map(assignment => (
                                <div key={assignment.id} className="assignment-card">
                                    <div className="assignment-header">
                                        <span className="assignment-icon">
                                            {getAssignmentStatusIcon(assignment.status)}
                                        </span>
                                        <div className="assignment-info">
                                            <h4>
                                                {assignment.type === 'PLAYER' ?
                                                    (assignment.targetPlayerName + ' ' + assignment.targetPlayerSurname) :
                                                    assignment.type
                                                }
                                            </h4>
                                            <span className="assignment-scout">
                                                Scout: {assignment.scout.name} {assignment.scout.surname}
                                            </span>
                                        </div>
                                        <div className="assignment-status">
                                            <span className={`status ${assignment.status.toLowerCase()}`}>
                                                {assignment.status}
                                            </span>
                                        </div>
                                    </div>

                                    <div className="assignment-details">
                                        <div className="detail-row">
                                            <span>Assigned:</span>
                                            <span>{new Date(assignment.assignedDate).toLocaleDateString()}</span>
                                        </div>
                                        <div className="detail-row">
                                            <span>Expected Completion:</span>
                                            <span>{new Date(assignment.expectedCompletionDate).toLocaleDateString()}</span>
                                        </div>
                                        <div className="detail-row">
                                            <span>Priority:</span>
                                            <span>{'🔥'.repeat(assignment.priority)}</span>
                                        </div>
                                    </div>

                                    {assignment.instructions && (
                                        <div className="assignment-instructions">
                                            <strong>Instructions:</strong>
                                            <p>{assignment.instructions}</p>
                                        </div>
                                    )}

                                    <div className="assignment-progress">
                                        <div className="progress-bar">
                                            <div
                                                className="progress-fill"
                                                style={{
                                                    width: calculateAssignmentProgress(assignment) + '%'
                                                }}
                                            ></div>
                                        </div>
                                        <span>{Math.round(calculateAssignmentProgress(assignment))}% Complete</span>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                )}

                {selectedTab === 'reports' && (
                    <div className="reports-section">
                        <div className="reports-grid">
                            {reports.map(report => (
                                <div key={report.id} className="report-card">
                                    <div className="report-header">
                                        <h4>{report.playerName} {report.playerSurname}</h4>
                                        <span
                                            className="recommendation"
                                            style={{ color: getRecommendationColor(report.recommendation) }}
                                        >
                                            {report.recommendation}
                                        </span>
                                    </div>

                                    <div className="report-ratings">
                                        <div className="rating">
                                            <span>Overall:</span>
                                            <span className="rating-value">{report.overallRating}/100</span>
                                        </div>
                                        <div className="rating">
                                            <span>Potential:</span>
                                            <span className="rating-value">{report.potentialRating}/100</span>
                                        </div>
                                        <div className="rating">
                                            <span>Confidence:</span>
                                            <span className="rating-value">{report.confidenceLevel}/10</span>
                                        </div>
                                    </div>

                                    <div className="report-assessment">
                                        <div className="strengths">
                                            <strong>Strengths:</strong>
                                            <p>{report.strengths}</p>
                                        </div>
                                        <div className="weaknesses">
                                            <strong>Weaknesses:</strong>
                                            <p>{report.weaknesses}</p>
                                        </div>
                                    </div>

                                    <div className="report-market-info">
                                        <div className="market-detail">
                                            <span>Est. Value:</span>
                                            <span>${report.estimatedValue?.toLocaleString()}</span>
                                        </div>
                                        <div className="market-detail">
                                            <span>Est. Wage:</span>
                                            <span>${report.estimatedWage?.toLocaleString()}/week</span>
                                        </div>
                                        <div className="market-detail">
                                            <span>Available:</span>
                                            <span>{report.isAvailableForTransfer ? 'Yes' : 'No'}</span>
                                        </div>
                                    </div>

                                    <div className="report-actions">
                                        <button
                                            className="watchlist-btn"
                                            onClick={() => handleAddToWatchlist(report.id)}
                                        >
                                            Add to Watchlist
                                        </button>
                                        <button className="details-btn">
                                            View Details
                                        </button>
                                    </div>

                                    <div className="report-meta">
                                        <small>
                                            Report by {report.scout.name} {report.scout.surname} -
                                            {new Date(report.reportDate).toLocaleDateString()}
                                        </small>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                )}

                {/* Assignment Modal */}
                {showAssignModal && (
                    <div className="modal-overlay">
                        <div className="modal-content">
                            <h3>Assign Scout: {selectedScout?.name} {selectedScout?.surname}</h3>
                            <form onSubmit={handleAssignSubmit}>
                                <div className="form-group">
                                    <label>Target Player ID:</label>
                                    <input
                                        type="number"
                                        required
                                        value={targetPlayerId}
                                        onChange={(e) => setTargetPlayerId(e.target.value)}
                                        placeholder="Enter Player ID"
                                    />
                                    <small className="text-muted">Enter the ID of the player to scout.</small>
                                </div>

                                <div className="form-group">
                                    <label>Priority:</label>
                                    <select
                                        value={priority}
                                        onChange={(e) => setPriority(parseInt(e.target.value))}
                                    >
                                        <option value={1}>1 - Low</option>
                                        <option value={2}>2 - Medium-Low</option>
                                        <option value={3}>3 - Medium</option>
                                        <option value={4}>4 - Medium-High</option>
                                        <option value={5}>5 - High</option>
                                    </select>
                                </div>

                                <div className="form-group">
                                    <label>Instructions:</label>
                                    <textarea
                                        value={instructions}
                                        onChange={(e) => setInstructions(e.target.value)}
                                        placeholder="Specific instructions..."
                                        rows={3}
                                    />
                                </div>

                                <div className="modal-actions">
                                    <button
                                        type="button"
                                        className="cancel-btn"
                                        onClick={() => setShowAssignModal(false)}
                                    >
                                        Cancel
                                    </button>
                                    <button
                                        type="submit"
                                        className="submit-btn"
                                    >
                                        Assign
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>
                )}
            </div>
        </Layout>
    );
};

export default ScoutingDashboard;
