import React, { useState, useEffect } from 'react';
import { getUsers, getUserDashboard, banUser, unbanUser, resetUserPassword } from '../services/userManagementApi';
import UserDetailModal from '../components/UserDetailModal';

const UserManagement = () => {
    const [users, setUsers] = useState([]);
    const [dashboard, setDashboard] = useState(null);
    const [selectedUser, setSelectedUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const [filters, setFilters] = useState({
        search: '',
        isActive: null,
        isVerified: null,
        isBanned: null,
        role: ''
    });
    const [pagination, setPagination] = useState({
        page: 0,
        size: 20,
        totalElements: 0,
        totalPages: 0
    });

    useEffect(() => {
        loadUserData();
    }, [filters, pagination.page]);

    const loadUserData = async () => {
        try {
            const [usersResponse, dashboardResponse] = await Promise.all([
                getUsers(pagination.page, pagination.size, filters),
                getUserDashboard()
            ]);

            setUsers(usersResponse.data.content);
            setPagination(prev => ({
                ...prev,
                totalElements: usersResponse.data.totalElements,
                totalPages: usersResponse.data.totalPages
            }));
            setDashboard(dashboardResponse.data);
        } catch (error) {
            console.error('Error loading user data:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleBanUser = async (userId, reason, duration) => {
        try {
            await banUser(userId, { reason, banDuration: duration });
            loadUserData(); // Refresh data
        } catch (error) {
            console.error('Error banning user:', error);
        }
    };

    const handleUnbanUser = async (userId) => {
        try {
            await unbanUser(userId);
            loadUserData(); // Refresh data
        } catch (error) {
            console.error('Error unbanning user:', error);
        }
    };

    const handleResetPassword = async (userId) => {
        if (window.confirm('Are you sure you want to reset this user\'s password? They will receive a temporary password via email.')) {
            try {
                await resetUserPassword(userId);
                alert('Password reset successfully. User will receive temporary password via email.');
            } catch (error) {
                console.error('Error resetting password:', error);
            }
        }
    };

    const getStatusBadge = (user) => {
        if (user.isBanned) return <span className="badge badge-danger">Banned</span>;
        if (!user.isActive) return <span className="badge badge-secondary">Inactive</span>;
        if (!user.isVerified) return <span className="badge badge-warning">Unverified</span>;
        return <span className="badge badge-success">Active</span>;
    };

    if (loading) return <div className="loading">Loading user management...</div>;

    return (
        <div className="user-management">
            <div className="management-header">
                <h1>User Management</h1>
                <button className="btn-primary">Create New User</button>
            </div>

            {/* Dashboard Stats */}
            {dashboard && (
            <div className="user-stats">
                <div className="stat-card">
                    <h3>Total Users</h3>
                    <span className="stat-number">{dashboard.totalUsers}</span>
                </div>
                <div className="stat-card">
                    <h3>Active Users</h3>
                    <span className="stat-number">{dashboard.activeUsers}</span>
                </div>
                <div className="stat-card">
                    <h3>Verified Users</h3>
                    <span className="stat-number">{dashboard.verifiedUsers}</span>
                </div>
                <div className="stat-card">
                    <h3>Banned Users</h3>
                    <span className="stat-number">{dashboard.bannedUsers}</span>
                </div>
                <div className="stat-card">
                    <h3>New (24h)</h3>
                    <span className="stat-number">{dashboard.newUsersLast24Hours}</span>
                </div>
                <div className="stat-card">
                    <h3>Active Sessions</h3>
                    <span className="stat-number">{dashboard.activeSessionsCount}</span>
                </div>
            </div>
            )}

            {/* Filters */}
            <div className="user-filters">
                <div className="filter-group">
                    <input
                        type="text"
                        placeholder="Search users..."
                        value={filters.search}
                        onChange={(e) => setFilters({...filters, search: e.target.value})}
                    />
                </div>
                <div className="filter-group">
                    <select
                        value={filters.isActive === null ? '' : filters.isActive.toString()}
                        onChange={(e) => setFilters({...filters, isActive: e.target.value === '' ? null : e.target.value === 'true'})}
                    >
                        <option value="">All Status</option>
                        <option value="true">Active</option>
                        <option value="false">Inactive</option>
                    </select>
                </div>
                <div className="filter-group">
                    <select
                        value={filters.isVerified === null ? '' : filters.isVerified.toString()}
                        onChange={(e) => setFilters({...filters, isVerified: e.target.value === '' ? null : e.target.value === 'true'})}
                    >
                        <option value="">All Verification</option>
                        <option value="true">Verified</option>
                        <option value="false">Unverified</option>
                    </select>
                </div>
                <div className="filter-group">
                    <select
                        value={filters.isBanned === null ? '' : filters.isBanned.toString()}
                        onChange={(e) => setFilters({...filters, isBanned: e.target.value === '' ? null : e.target.value === 'true'})}
                    >
                        <option value="">All Ban Status</option>
                        <option value="false">Not Banned</option>
                        <option value="true">Banned</option>
                    </select>
                </div>
            </div>

            {/* Users Table */}
            <div className="users-table">
                <table>
                    <thead>
                        <tr>
                            <th>Username</th>
                            <th>Email</th>
                            <th>Name</th>
                            <th>Status</th>
                            <th>Roles</th>
                            <th>Created</th>
                            <th>Last Login</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {users.map(user => (
                            <tr key={user.id}>
                                <td>{user.username}</td>
                                <td>{user.email}</td>
                                <td>{user.firstName} {user.lastName}</td>
                                <td>{getStatusBadge(user)}</td>
                                <td>
                                    {user.roles && user.roles.map(role => (
                                        <span key={role.id || role} className="role-badge">{role.name || role}</span>
                                    ))}
                                </td>
                                <td>{new Date(user.createdDate).toLocaleDateString()}</td>
                                <td>
                                    {user.lastLoginDate ?
                                        new Date(user.lastLoginDate).toLocaleDateString() :
                                        'Never'
                                    }
                                </td>
                                <td>
                                    <div className="action-buttons">
                                        <button
                                            className="btn-secondary btn-sm"
                                            onClick={() => setSelectedUser(user)}
                                        >
                                            View
                                        </button>

                                        {!user.isBanned ? (
                                            <button
                                                className="btn-warning btn-sm"
                                                onClick={() => handleBanUser(user.id, 'Admin action', null)}
                                            >
                                                Ban
                                            </button>
                                        ) : (
                                            <button
                                                className="btn-success btn-sm"
                                                onClick={() => handleUnbanUser(user.id)}
                                            >
                                                Unban
                                            </button>
                                        )}

                                        <button
                                            className="btn-info btn-sm"
                                            onClick={() => handleResetPassword(user.id)}
                                        >
                                            Reset Password
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>

            {/* Pagination */}
            <div className="pagination">
                <button
                    disabled={pagination.page === 0}
                    onClick={() => setPagination(prev => ({...prev, page: prev.page - 1}))}
                >
                    Previous
                </button>
                <span>
                    Page {pagination.page + 1} of {pagination.totalPages}
                </span>
                <button
                    disabled={pagination.page >= pagination.totalPages - 1}
                    onClick={() => setPagination(prev => ({...prev, page: prev.page + 1}))}
                >
                    Next
                </button>
            </div>

            {/* User Detail Modal */}
            {selectedUser && (
                <UserDetailModal
                    user={selectedUser}
                    onClose={() => setSelectedUser(null)}
                    onUpdate={loadUserData}
                />
            )}
        </div>
    );
};

export default UserManagement;
