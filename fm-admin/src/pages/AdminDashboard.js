import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getAdminDashboard, getAdminActions } from '../services/api';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  ArcElement,
  Title,
  Tooltip,
  Legend,
} from 'chart.js';

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  ArcElement,
  Title,
  Tooltip,
  Legend
);

const AdminDashboard = () => {
    const navigate = useNavigate();
    const [dashboard, setDashboard] = useState(null);
    const [recentActions, setRecentActions] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadDashboardData();
    }, []);

    const loadDashboardData = async () => {
        try {
            const [dashboardResponse, actionsResponse] = await Promise.all([
                getAdminDashboard(),
                getAdminActions(0, 10)
            ]);

            setDashboard(dashboardResponse.data);
            setRecentActions(actionsResponse.data.content);
        } catch (error) {
            console.error('Error loading dashboard data:', error);
        } finally {
            setLoading(false);
        }
    };

    if (loading) return <div className="loading">Loading admin dashboard...</div>;
    if (!dashboard) return <div>Error loading dashboard.</div>;

    const navigateToClubManagement = () => navigate('/clubs');

    return (
        <div className="admin-dashboard" style={{padding: '20px'}}>
            <div className="dashboard-header" style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px'}}>
                <h1>Admin Dashboard</h1>
                <div className="dashboard-actions">
                    <button className="btn-primary" onClick={navigateToClubManagement}>Manage Clubs</button>
                </div>
            </div>

            {/* Statistics Cards */}
            <div className="stats-grid" style={{display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '20px', marginBottom: '30px'}}>
                <div className="stat-card" style={cardStyle}>
                    <div className="stat-content">
                        <h3>Total Clubs</h3>
                        <span className="stat-number" style={numberStyle}>{dashboard.totalClubs}</span>
                    </div>
                </div>

                <div className="stat-card" style={cardStyle}>
                    <div className="stat-content">
                        <h3>Total Players</h3>
                        <span className="stat-number" style={numberStyle}>{dashboard.totalPlayers}</span>
                    </div>
                </div>

                <div className="stat-card" style={cardStyle}>
                    <div className="stat-content">
                        <h3>Active Users</h3>
                        <span className="stat-number" style={numberStyle}>{dashboard.activeUsers}</span>
                        <div className="stat-subtitle">of {dashboard.totalUsers} total</div>
                    </div>
                </div>

                <div className="stat-card" style={cardStyle}>
                    <div className="stat-content">
                        <h3>Leagues</h3>
                        <span className="stat-number" style={numberStyle}>{dashboard.totalLeagues}</span>
                    </div>
                </div>
            </div>

            {/* System Health */}
            <div className="system-health" style={{marginBottom: '30px', padding: '20px', backgroundColor: '#f8f9fa', borderRadius: '8px'}}>
                <h2>System Health</h2>
                <div className="health-indicators" style={{display: 'flex', gap: '30px', marginTop: '15px'}}>
                    <div className="health-item">
                        <span className="health-label">Database: </span>
                        <span style={{fontWeight: 'bold', color: dashboard.systemHealth.databaseStatus === 'ONLINE' ? 'green' : 'red'}}>
                            {dashboard.systemHealth.databaseStatus}
                        </span>
                    </div>
                    <div className="health-item">
                        <span className="health-label">Memory Usage: </span>
                        <span className="health-value">{dashboard.systemHealth.memoryUsage}%</span>
                    </div>
                    <div className="health-item">
                        <span className="health-label">Active Connections: </span>
                        <span className="health-value">{dashboard.systemHealth.activeConnections}</span>
                    </div>
                    <div className="health-item">
                        <span className="health-label">System Uptime: </span>
                        <span className="health-value">{dashboard.systemHealth.systemUptime}</span>
                    </div>
                </div>
            </div>

            {/* Recent Admin Actions */}
            <div className="recent-actions">
                <h2>Recent Admin Actions</h2>
                <div className="actions-list" style={{marginTop: '15px'}}>
                    {recentActions.map(action => (
                        <div key={action.id} className="action-item" style={{padding: '10px', borderBottom: '1px solid #eee', display: 'flex', justifyContent: 'space-between'}}>
                            <div>
                                <b>{action.actionType}</b> {action.entityType}
                                <div style={{fontSize: '0.9em', color: '#666'}}>{action.actionDescription}</div>
                            </div>
                            <div style={{textAlign: 'right', fontSize: '0.9em'}}>
                                <div>{action.adminUsername}</div>
                                <div style={{color: '#999'}}>{new Date(action.actionTimestamp).toLocaleString()}</div>
                            </div>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
};

const cardStyle = {
    backgroundColor: 'white',
    padding: '20px',
    borderRadius: '8px',
    boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
};

const numberStyle = {
    fontSize: '2em',
    fontWeight: 'bold',
    color: '#007bff',
    display: 'block',
    marginTop: '10px'
};

export default AdminDashboard;
