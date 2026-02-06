import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getAdminDashboard, getAdminActions } from '../services/api';
import Layout from '../components/Layout';
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
        <Layout>
            <div className="admin-dashboard">
                <div className="dashboard-header" style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center'}}>
                    <h1>Admin Dashboard</h1>
                    <div className="dashboard-actions">
                        <button className="btn btn-primary" onClick={navigateToClubManagement}>Manage Clubs</button>
                    </div>
                </div>

                {/* Statistics Cards */}
                <div className="stats-grid" style={{display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '20px', marginBottom: '30px'}}>
                    <div className="stat-card">
                        <div className="stat-content">
                            <h3>Total Clubs</h3>
                            <span className="stat-number">{dashboard.totalClubs}</span>
                        </div>
                    </div>

                    <div className="stat-card">
                        <div className="stat-content">
                            <h3>Total Players</h3>
                            <span className="stat-number">{dashboard.totalPlayers}</span>
                        </div>
                    </div>

                    <div className="stat-card">
                        <div className="stat-content">
                            <h3>Active Users</h3>
                            <span className="stat-number">{dashboard.activeUsers}</span>
                            <div className="stat-subtitle">of {dashboard.totalUsers} total</div>
                        </div>
                    </div>

                    <div className="stat-card">
                        <div className="stat-content">
                            <h3>Leagues</h3>
                            <span className="stat-number">{dashboard.totalLeagues}</span>
                        </div>
                    </div>
                </div>

                {/* System Health */}
                <div className="card mb-4">
                    <div className="card-header">System Health</div>
                    <div className="card-body">
                        <div className="health-indicators">
                            <div className="health-item">
                                <span className="health-label">Database</span>
                                <span style={{fontWeight: 'bold', color: dashboard.systemHealth.databaseStatus === 'ONLINE' ? 'var(--success)' : 'var(--danger)'}}>
                                    {dashboard.systemHealth.databaseStatus}
                                </span>
                            </div>
                            <div className="health-item">
                                <span className="health-label">Memory Usage</span>
                                <span className="health-value">{dashboard.systemHealth.memoryUsage}%</span>
                            </div>
                            <div className="health-item">
                                <span className="health-label">Active Connections</span>
                                <span className="health-value">{dashboard.systemHealth.activeConnections}</span>
                            </div>
                            <div className="health-item">
                                <span className="health-label">System Uptime</span>
                                <span className="health-value">{dashboard.systemHealth.systemUptime}</span>
                            </div>
                        </div>
                    </div>
                </div>

                {/* Recent Admin Actions */}
                <div className="card">
                    <div className="card-header">Recent Admin Actions</div>
                    <div className="card-body recent-actions" style={{padding: '0.5rem'}}>
                        <div className="actions-list">
                            {recentActions.map(action => (
                                <div key={action.id} className="action-item" style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center'}}>
                                    <div>
                                        <div style={{fontWeight: 'bold', color: 'var(--primary)'}}>{action.actionType} <span style={{color: 'var(--text-main)', fontWeight: 'normal'}}>{action.entityType}</span></div>
                                        <div style={{fontSize: '0.85rem', color: 'var(--text-muted)'}}>{action.actionDescription}</div>
                                    </div>
                                    <div style={{textAlign: 'right', fontSize: '0.85rem'}}>
                                        <div style={{fontWeight: '600'}}>{action.adminUsername}</div>
                                        <div style={{color: 'var(--text-muted)'}}>{new Date(action.actionTimestamp).toLocaleString()}</div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>
            </div>
        </Layout>
    );
};

export default AdminDashboard;
