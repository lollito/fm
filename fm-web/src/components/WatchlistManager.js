import React, { useState, useEffect } from 'react';
import { getClubWatchlist, addPlayerToWatchlist, removeFromWatchlist,
         getWatchlistNotifications, getWatchlistStats, markNotificationAsRead } from '../services/api';

const WatchlistManager = ({ clubId }) => {
    const [watchlist, setWatchlist] = useState(null);
    const [notifications, setNotifications] = useState([]);
    const [stats, setStats] = useState(null);
    const [selectedTab, setSelectedTab] = useState('players');
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (clubId) {
            loadWatchlistData();
        }
    }, [clubId]);

    const loadWatchlistData = async () => {
        try {
            const [watchlistResponse, notificationsResponse, statsResponse] = await Promise.all([
                getClubWatchlist(clubId),
                getWatchlistNotifications(clubId),
                getWatchlistStats(clubId)
            ]);

            setWatchlist(watchlistResponse.data);
            setNotifications(notificationsResponse.data);
            setStats(statsResponse.data);
        } catch (error) {
            console.error('Error loading watchlist data:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleRemovePlayer = async (entryId) => {
        try {
            await removeFromWatchlist(entryId);
            loadWatchlistData(); // Refresh data
        } catch (error) {
            console.error('Error removing player from watchlist:', error);
        }
    };

    const markAsRead = async (notificationId) => {
        try {
            await markNotificationAsRead(notificationId);
            setNotifications(notifications.map(n =>
                n.id === notificationId ? { ...n, isRead: true } : n
            ));
        } catch (error) {
            console.error('Error marking notification as read:', error);
        }
    };

    const getPriorityColor = (priority) => {
        const colors = {
            LOW: '#4caf50',
            MEDIUM: '#ff9800',
            HIGH: '#f44336',
            URGENT: '#9c27b0'
        };
        return colors[priority] || '#666';
    };

    const getCategoryIcon = (category) => {
        const icons = {
            TARGET: '🎯',
            BACKUP: '🔄',
            FUTURE: '🌟',
            COMPARISON: '📊',
            LOAN_TARGET: '📝'
        };
        return icons[category] || '👤';
    };

    const getNotificationIcon = (type) => {
        const icons = {
            PERFORMANCE: '⚽',
            TRANSFER_STATUS: '🔄',
            INJURY: '🏥',
            CONTRACT_EXPIRY: '📅',
            PRICE_CHANGE: '💰',
            MATCH_PERFORMANCE: '🏆',
            AVAILABILITY: '✅',
            COMPETITION: '⚠️'
        };
        return icons[type] || '📢';
    };

    const formatValueChange = (addedValue, currentValue) => {
        if (!addedValue || !currentValue) return { change: 0, percentage: 0, isPositive: true, color: '#666', arrow: '-' };
        const change = currentValue - addedValue;
        const percentage = addedValue !== 0 ? ((change / addedValue) * 100).toFixed(1) : 0;
        const isPositive = change >= 0;

        return {
            change: Math.abs(change),
            percentage: Math.abs(percentage),
            isPositive,
            color: isPositive ? '#4caf50' : '#f44336',
            arrow: isPositive ? '↗️' : '↘️'
        };
    };

    if (loading) return <div>Loading watchlist...</div>;

    return (
        <div className="watchlist-manager">
            <div className="watchlist-header">
                <h2>Transfer Watchlist</h2>
                <div className="watchlist-summary">
                    <div className="summary-stat">
                        <span>Players:</span>
                        <span>{watchlist?.totalEntries}/{watchlist?.maxEntries}</span>
                    </div>
                    <div className="summary-stat">
                        <span>Available:</span>
                        <span>{stats?.availablePlayers}</span>
                    </div>
                    <div className="summary-stat">
                        <span>Total Value:</span>
                        <span>${stats?.totalValue?.toLocaleString()}</span>
                    </div>
                </div>
            </div>

            <div className="watchlist-tabs">
                <button
                    className={selectedTab === 'players' ? 'active' : ''}
                    onClick={() => setSelectedTab('players')}
                >
                    Players ({watchlist?.totalEntries})
                </button>
                <button
                    className={selectedTab === 'notifications' ? 'active' : ''}
                    onClick={() => setSelectedTab('notifications')}
                >
                    Notifications ({notifications.filter(n => !n.isRead).length})
                </button>
                <button
                    className={selectedTab === 'stats' ? 'active' : ''}
                    onClick={() => setSelectedTab('stats')}
                >
                    Statistics
                </button>
            </div>

            {selectedTab === 'players' && (
                <div className="watchlist-players">
                    <div className="players-grid">
                        {watchlist?.entries?.map(entry => {
                            const valueChange = formatValueChange(entry.addedValue, entry.currentValue);

                            return (
                                <div key={entry.id} className="watchlist-player-card">
                                    <div className="player-header">
                                        <div className="player-name">
                                            <h4>{entry.player.name} {entry.player.surname}</h4>
                                            <span className="player-position">{entry.player.role}</span>
                                        </div>
                                        <div className="player-priority">
                                            <span
                                                className="priority-badge"
                                                style={{ backgroundColor: getPriorityColor(entry.priority) }}
                                            >
                                                {entry.priority}
                                            </span>
                                        </div>
                                    </div>

                                    <div className="player-info">
                                        <div className="info-row">
                                            <span>Club:</span>
                                            <span>{entry.player.team?.club?.name}</span>
                                        </div>
                                        <div className="info-row">
                                            <span>Age:</span>
                                            <span>{entry.player.age}</span>
                                        </div>
                                        <div className="info-row">
                                            <span>Category:</span>
                                            <span>
                                                {getCategoryIcon(entry.category)} {entry.category}
                                            </span>
                                        </div>
                                    </div>

                                    <div className="value-tracking">
                                        <div className="value-row">
                                            <span>Added Value:</span>
                                            <span>${entry.addedValue?.toLocaleString()}</span>
                                        </div>
                                        <div className="value-row">
                                            <span>Current Value:</span>
                                            <span>${entry.currentValue?.toLocaleString()}</span>
                                        </div>
                                        <div className="value-change">
                                            <span
                                                className="change-indicator"
                                                style={{ color: valueChange.color }}
                                            >
                                                {valueChange.arrow} {valueChange.percentage}%
                                            </span>
                                        </div>
                                    </div>

                                    <div className="rating-tracking">
                                        <div className="rating-row">
                                            <span>Added Rating:</span>
                                            <span>{entry.addedRating?.toFixed(1)}</span>
                                        </div>
                                        <div className="rating-row">
                                            <span>Current Rating:</span>
                                            <span>{entry.currentRating?.toFixed(1)}</span>
                                        </div>
                                    </div>

                                    {entry.notes && (
                                        <div className="player-notes">
                                            <strong>Notes:</strong>
                                            <p>{entry.notes}</p>
                                        </div>
                                    )}

                                    <div className="notification-settings">
                                        <div className="notification-toggles">
                                            {entry.notifyOnPerformance && <span title="Performance">⚽</span>}
                                            {entry.notifyOnTransferStatus && <span title="Transfer Status">🔄</span>}
                                            {entry.notifyOnInjury && <span title="Injury">🏥</span>}
                                            {entry.notifyOnContractExpiry && <span title="Contract">📅</span>}
                                            {entry.notifyOnPriceChange && <span title="Price">💰</span>}
                                        </div>
                                        <span className="notification-count">
                                            {entry.totalNotifications} notifications
                                        </span>
                                    </div>

                                    <div className="player-actions">
                                        <button className="view-btn">
                                            View Player
                                        </button>
                                        <button className="edit-btn">
                                            Edit
                                        </button>
                                        <button
                                            className="remove-btn"
                                            onClick={() => handleRemovePlayer(entry.id)}
                                        >
                                            Remove
                                        </button>
                                    </div>

                                    <div className="added-date">
                                        <small>
                                            Added: {new Date(entry.addedDate).toLocaleDateString()}
                                        </small>
                                    </div>
                                </div>
                            );
                        })}
                    </div>
                </div>
            )}

            {selectedTab === 'notifications' && (
                <div className="watchlist-notifications">
                    <div className="notifications-list">
                        {notifications.map(notification => (
                            <div
                                key={notification.id}
                                className={`notification-item ${!notification.isRead ? 'unread' : ''} ${notification.isImportant ? 'important' : ''}`}
                            >
                                <div className="notification-icon">
                                    {getNotificationIcon(notification.type)}
                                </div>
                                <div className="notification-content">
                                    <div className="notification-header">
                                        <h4>{notification.title}</h4>
                                        <span className="notification-time">
                                            {new Date(notification.createdDate).toLocaleDateString()}
                                        </span>
                                    </div>
                                    <p className="notification-message">
                                        {notification.message}
                                    </p>
                                    {notification.detailedMessage && (
                                        <p className="notification-details">
                                            {notification.detailedMessage}
                                        </p>
                                    )}
                                </div>
                                <div className="notification-actions">
                                    {!notification.isRead && (
                                        <button
                                            className="mark-read-btn"
                                            onClick={() => markAsRead(notification.id)}
                                        >
                                            Mark Read
                                        </button>
                                    )}
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            )}

            {selectedTab === 'stats' && (
                <div className="watchlist-stats">
                    <div className="stats-grid">
                        <div className="stat-card">
                            <h3>Overview</h3>
                            <div className="stat-item">
                                <span>Total Players:</span>
                                <span>{stats?.totalPlayers}</span>
                            </div>
                            <div className="stat-item">
                                <span>Available for Transfer:</span>
                                <span>{stats?.availablePlayers}</span>
                            </div>
                            <div className="stat-item">
                                <span>Contracts Expiring Soon:</span>
                                <span>{stats?.contractsExpiringSoon}</span>
                            </div>
                            <div className="stat-item">
                                <span>Recently Performed Well:</span>
                                <span>{stats?.recentlyPerformed}</span>
                            </div>
                        </div>

                        <div className="stat-card">
                            <h3>Value Tracking</h3>
                            <div className="stat-item">
                                <span>Total Watchlist Value:</span>
                                <span>${stats?.totalValue?.toLocaleString()}</span>
                            </div>
                            <div className="stat-item">
                                <span>Average Player Value:</span>
                                <span>${stats?.averageValue?.toLocaleString()}</span>
                            </div>
                            <div className="stat-item">
                                <span>Values Increased:</span>
                                <span className="positive">{stats?.priceIncreased}</span>
                            </div>
                            <div className="stat-item">
                                <span>Values Decreased:</span>
                                <span className="negative">{stats?.priceDecreased}</span>
                            </div>
                        </div>

                        <div className="stat-card">
                            <h3>Activity</h3>
                            <div className="stat-item">
                                <span>Total Notifications:</span>
                                <span>{notifications.length}</span>
                            </div>
                            <div className="stat-item">
                                <span>Unread Notifications:</span>
                                <span>{notifications.filter(n => !n.isRead).length}</span>
                            </div>
                            <div className="stat-item">
                                <span>Important Notifications:</span>
                                <span>{notifications.filter(n => n.isImportant).length}</span>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default WatchlistManager;
