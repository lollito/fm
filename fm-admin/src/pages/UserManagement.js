import React, { useState, useEffect } from 'react';
import { getUsers, getUserDashboard, banUser, unbanUser, resetUserPassword } from '../services/userManagementApi';
import UserDetailModal from '../components/UserDetailModal';
import Layout from '../components/Layout';

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

    if (loading) return (
        <Layout>
            <div className="d-flex justify-content-center align-items-center" style={{height: '100%'}}>
                Loading user management...
            </div>
        </Layout>
    );

    return (
        <Layout>
            <div className="user-management">
                {/* Header */}
                <div className="d-flex justify-content-between align-items-center mb-4">
                    <h1>User Management</h1>
                    <button className="btn btn-primary">
                        <i className="fas fa-plus mr-2"></i>Create New User
                    </button>
                </div>

                {/* Dashboard Stats */}
                {dashboard && (
                    <div className="stats-grid mb-4" style={{display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '20px'}}>
                        <div className="stat-card">
                            <div className="stat-content">
                                <h3>Total Users</h3>
                                <span className="stat-number">{dashboard.totalUsers}</span>
                            </div>
                        </div>
                        <div className="stat-card">
                            <div className="stat-content">
                                <h3>Active Users</h3>
                                <span className="stat-number">{dashboard.activeUsers}</span>
                            </div>
                        </div>
                        <div className="stat-card">
                            <div className="stat-content">
                                <h3>Verified</h3>
                                <span className="stat-number">{dashboard.verifiedUsers}</span>
                            </div>
                        </div>
                        <div className="stat-card">
                            <div className="stat-content">
                                <h3>Banned</h3>
                                <span className="stat-number">{dashboard.bannedUsers}</span>
                            </div>
                        </div>
                        <div className="stat-card">
                            <div className="stat-content">
                                <h3>New (24h)</h3>
                                <span className="stat-number">{dashboard.newUsersLast24Hours}</span>
                            </div>
                        </div>
                    </div>
                )}

                {/* Filters */}
                <div className="card mb-4">
                    <div className="card-header">
                        <i className="fas fa-filter mr-2"></i>Search & Filters
                    </div>
                    <div className="card-body">
                        <div className="row">
                            <div className="col">
                                <input
                                    type="text"
                                    className="form-control"
                                    placeholder="Search users..."
                                    value={filters.search}
                                    onChange={(e) => setFilters({...filters, search: e.target.value})}
                                />
                            </div>
                            <div className="col">
                                <select
                                    className="form-control"
                                    value={filters.isActive === null ? '' : filters.isActive.toString()}
                                    onChange={(e) => setFilters({...filters, isActive: e.target.value === '' ? null : e.target.value === 'true'})}
                                >
                                    <option value="">All Status</option>
                                    <option value="true">Active</option>
                                    <option value="false">Inactive</option>
                                </select>
                            </div>
                            <div className="col">
                                <select
                                    className="form-control"
                                    value={filters.isVerified === null ? '' : filters.isVerified.toString()}
                                    onChange={(e) => setFilters({...filters, isVerified: e.target.value === '' ? null : e.target.value === 'true'})}
                                >
                                    <option value="">All Verification</option>
                                    <option value="true">Verified</option>
                                    <option value="false">Unverified</option>
                                </select>
                            </div>
                            <div className="col">
                                <select
                                    className="form-control"
                                    value={filters.isBanned === null ? '' : filters.isBanned.toString()}
                                    onChange={(e) => setFilters({...filters, isBanned: e.target.value === '' ? null : e.target.value === 'true'})}
                                >
                                    <option value="">All Ban Status</option>
                                    <option value="false">Not Banned</option>
                                    <option value="true">Banned</option>
                                </select>
                            </div>
                        </div>
                    </div>
                </div>

                {/* Users Table */}
                <div className="card">
                    <div className="card-header d-flex justify-content-between align-items-center">
                        <span><i className="fas fa-users mr-2"></i>Users List</span>
                        <span className="badge badge-secondary">{pagination.totalElements} found</span>
                    </div>
                    <div className="card-body p-0">
                        <div className="table-responsive">
                            <table className="table mb-0">
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
                                    {users.length > 0 ? (
                                        users.map(user => (
                                            <tr key={user.id}>
                                                <td style={{fontWeight: '500'}}>{user.username}</td>
                                                <td className="text-muted">{user.email}</td>
                                                <td>{user.firstName} {user.lastName}</td>
                                                <td>{getStatusBadge(user)}</td>
                                                <td>
                                                    {user.roles && user.roles.map((role, idx) => (
                                                        <span key={idx} className="badge badge-secondary mr-2" style={{marginRight: '0.25rem'}}>
                                                            {role.name || role}
                                                        </span>
                                                    ))}
                                                </td>
                                                <td className="text-muted">{new Date(user.createdDate).toLocaleDateString()}</td>
                                                <td className="text-muted">
                                                    {user.lastLoginDate ?
                                                        new Date(user.lastLoginDate).toLocaleDateString() :
                                                        'Never'
                                                    }
                                                </td>
                                                <td>
                                                    <div className="d-flex gap-2">
                                                        <button
                                                            className="btn btn-primary btn-sm"
                                                            onClick={() => setSelectedUser(user)}
                                                            title="View Details"
                                                        >
                                                            View
                                                        </button>

                                                        {!user.isBanned ? (
                                                            <button
                                                                className="btn btn-danger btn-sm"
                                                                onClick={() => handleBanUser(user.id, 'Admin action', null)}
                                                                title="Ban User"
                                                            >
                                                                Ban
                                                            </button>
                                                        ) : (
                                                            <button
                                                                className="btn btn-success btn-sm"
                                                                onClick={() => handleUnbanUser(user.id)}
                                                                title="Unban User"
                                                            >
                                                                Unban
                                                            </button>
                                                        )}

                                                        <button
                                                            className="btn btn-warning btn-sm"
                                                            onClick={() => handleResetPassword(user.id)}
                                                            title="Reset Password"
                                                        >
                                                            Reset
                                                        </button>
                                                    </div>
                                                </td>
                                            </tr>
                                        ))
                                    ) : (
                                        <tr>
                                            <td colSpan="8" className="text-center p-4">No users found matching your criteria.</td>
                                        </tr>
                                    )}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

                {/* Pagination */}
                <div className="d-flex justify-content-between align-items-center mt-3">
                    <div className="text-muted">
                        Showing {users.length} of {pagination.totalElements} users
                    </div>
                    <div className="d-flex gap-2">
                        <button
                            className="btn btn-secondary btn-sm"
                            disabled={pagination.page === 0}
                            onClick={() => setPagination(prev => ({...prev, page: prev.page - 1}))}
                        >
                            Previous
                        </button>
                        <span className="d-flex align-items-center px-3" style={{background: 'var(--bg-card)', borderRadius: '0.25rem', border: '1px solid var(--border-color)'}}>
                            Page {pagination.page + 1} of {pagination.totalPages}
                        </span>
                        <button
                            className="btn btn-secondary btn-sm"
                            disabled={pagination.page >= pagination.totalPages - 1}
                            onClick={() => setPagination(prev => ({...prev, page: prev.page + 1}))}
                        >
                            Next
                        </button>
                    </div>
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
        </Layout>
    );
};

export default UserManagement;
