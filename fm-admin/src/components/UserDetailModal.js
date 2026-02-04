import React, { useState, useEffect } from 'react';
import { getUserActivities, getUserSessions, terminateSession } from '../services/userManagementApi';

const UserDetailModal = ({ user, onClose, onUpdate }) => {
    const [activities, setActivities] = useState([]);
    const [sessions, setSessions] = useState([]);
    const [activeTab, setActiveTab] = useState('profile');

    useEffect(() => {
        if (activeTab === 'activities') {
            loadUserActivities();
        } else if (activeTab === 'sessions') {
            loadUserSessions();
        }
    }, [activeTab, user.id]);

    const loadUserActivities = async () => {
        try {
            const response = await getUserActivities(user.id);
            setActivities(response.data.content);
        } catch (error) {
            console.error('Error loading user activities:', error);
        }
    };

    const loadUserSessions = async () => {
        try {
            const response = await getUserSessions(user.id);
            setSessions(response.data);
        } catch (error) {
            console.error('Error loading user sessions:', error);
        }
    };

    const handleTerminateSession = async (sessionId) => {
        try {
            await terminateSession(sessionId);
            loadUserSessions();
        } catch (error) {
            console.error('Error terminating session:', error);
        }
    }

    const getActivityIcon = (activityType) => {
        switch (activityType) {
            case 'LOGIN': return 'fas fa-sign-in-alt';
            case 'LOGOUT': return 'fas fa-sign-out-alt';
            case 'PASSWORD_CHANGE': return 'fas fa-key';
            case 'PROFILE_UPDATE': return 'fas fa-user-edit';
            default: return 'fas fa-info-circle';
        }
    };

    return (
        <div className="modal-overlay">
            <div className="modal-content large">
                <div className="modal-header">
                    <h2>User Details - {user.username}</h2>
                    <button onClick={onClose}>×</button>
                </div>

                <div className="modal-tabs">
                    <button
                        className={activeTab === 'profile' ? 'active' : ''}
                        onClick={() => setActiveTab('profile')}
                    >
                        Profile
                    </button>
                    <button
                        className={activeTab === 'activities' ? 'active' : ''}
                        onClick={() => setActiveTab('activities')}
                    >
                        Activities
                    </button>
                    <button
                        className={activeTab === 'sessions' ? 'active' : ''}
                        onClick={() => setActiveTab('sessions')}
                    >
                        Sessions
                    </button>
                </div>

                <div className="modal-body">
                    {activeTab === 'profile' && (
                        <div className="user-profile">
                            <div className="profile-section">
                                <h3>Basic Information</h3>
                                <div className="profile-grid">
                                    <div className="profile-item">
                                        <label>Username:</label>
                                        <span>{user.username}</span>
                                    </div>
                                    <div className="profile-item">
                                        <label>Email:</label>
                                        <span>{user.email}</span>
                                    </div>
                                    <div className="profile-item">
                                        <label>Full Name:</label>
                                        <span>{user.firstName} {user.lastName}</span>
                                    </div>
                                    <div className="profile-item">
                                        <label>Phone:</label>
                                        <span>{user.phoneNumber || 'Not provided'}</span>
                                    </div>
                                    <div className="profile-item">
                                        <label>Country:</label>
                                        <span>{user.country || 'Not provided'}</span>
                                    </div>
                                    <div className="profile-item">
                                        <label>Language:</label>
                                        <span>{user.preferredLanguage || 'Not set'}</span>
                                    </div>
                                </div>
                            </div>

                            <div className="profile-section">
                                <h3>Account Status</h3>
                                <div className="profile-grid">
                                    <div className="profile-item">
                                        <label>Status:</label>
                                        <span className={`status ${user.isActive ? 'active' : 'inactive'}`}>
                                            {user.isActive ? 'Active' : 'Inactive'}
                                        </span>
                                    </div>
                                    <div className="profile-item">
                                        <label>Verified:</label>
                                        <span className={`status ${user.isVerified ? 'verified' : 'unverified'}`}>
                                            {user.isVerified ? 'Verified' : 'Unverified'}
                                        </span>
                                    </div>
                                    <div className="profile-item">
                                        <label>Banned:</label>
                                        <span className={`status ${user.isBanned ? 'banned' : 'not-banned'}`}>
                                            {user.isBanned ? 'Yes' : 'No'}
                                        </span>
                                    </div>
                                    {user.isBanned && (
                                        <>
                                            <div className="profile-item">
                                                <label>Ban Reason:</label>
                                                <span>{user.banReason}</span>
                                            </div>
                                            <div className="profile-item">
                                                <label>Banned Until:</label>
                                                <span>
                                                    {user.bannedUntil ?
                                                        new Date(user.bannedUntil).toLocaleString() :
                                                        'Permanent'
                                                    }
                                                </span>
                                            </div>
                                        </>
                                    )}
                                </div>
                            </div>

                            <div className="profile-section">
                                <h3>Account History</h3>
                                <div className="profile-grid">
                                    <div className="profile-item">
                                        <label>Created:</label>
                                        <span>{new Date(user.createdDate).toLocaleString()}</span>
                                    </div>
                                    <div className="profile-item">
                                        <label>Last Login:</label>
                                        <span>
                                            {user.lastLoginDate ?
                                                new Date(user.lastLoginDate).toLocaleString() :
                                                'Never'
                                            }
                                        </span>
                                    </div>
                                    <div className="profile-item">
                                        <label>Failed Login Attempts:</label>
                                        <span>{user.failedLoginAttempts}</span>
                                    </div>
                                    <div className="profile-item">
                                        <label>Password Changed:</label>
                                        <span>
                                            {user.passwordChangedDate ?
                                                new Date(user.passwordChangedDate).toLocaleString() :
                                                'Never'
                                            }
                                        </span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    )}

                    {activeTab === 'activities' && (
                        <div className="user-activities">
                            <h3>Recent Activities</h3>
                            <div className="activities-list">
                                {activities.map(activity => (
                                    <div key={activity.id} className="activity-item">
                                        <div className="activity-icon">
                                            <i className={getActivityIcon(activity.activityType)}></i>
                                        </div>
                                        <div className="activity-content">
                                            <div className="activity-description">
                                                {activity.activityDescription}
                                            </div>
                                            <div className="activity-meta">
                                                <span className="activity-time">
                                                    {new Date(activity.activityTimestamp).toLocaleString()}
                                                </span>
                                                <span className={`activity-severity ${activity.severity ? activity.severity.toLowerCase() : ''}`}>
                                                    {activity.severity}
                                                </span>
                                                {activity.ipAddress && (
                                                    <span className="activity-ip">
                                                        IP: {activity.ipAddress}
                                                    </span>
                                                )}
                                            </div>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}

                    {activeTab === 'sessions' && (
                        <div className="user-sessions">
                            <h3>User Sessions</h3>
                            <div className="sessions-list">
                                {sessions.map(session => (
                                    <div key={session.id} className="session-item">
                                        <div className="session-info">
                                            <div className="session-device">
                                                <strong>{session.deviceInfo || 'Unknown Device'}</strong>
                                            </div>
                                            <div className="session-details">
                                                <span>IP: {session.ipAddress}</span>
                                                <span>Location: {session.location || 'Unknown'}</span>
                                                <span>Login: {new Date(session.loginTime).toLocaleString()}</span>
                                                {session.lastActivityTime && (
                                                    <span>Last Activity: {new Date(session.lastActivityTime).toLocaleString()}</span>
                                                )}
                                            </div>
                                        </div>
                                        <div className="session-status">
                                            <span className={`status-badge ${session.status ? session.status.toLowerCase() : ''}`}>
                                                {session.status}
                                            </span>
                                            {session.isActive && (
                                                <button
                                                    className="btn-danger btn-sm"
                                                    onClick={() => handleTerminateSession(session.id)}
                                                >
                                                    Terminate
                                                </button>
                                            )}
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default UserDetailModal;
