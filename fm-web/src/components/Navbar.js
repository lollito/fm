import React, { useContext, useState, useEffect, useRef } from 'react';
import { AuthContext } from '../context/AuthContext';
import { Link, useNavigate } from 'react-router-dom';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { API_BASE_URL, getUnreadNotifications, markSystemNotificationRead, getManagerProfile } from '../services/api';
import '../styles/Navbar.css';

const Navbar = ({ onToggleMenu }) => {
  const { user, logout } = useContext(AuthContext);
  const [showDropdown, setShowDropdown] = useState(false);
  const dropdownRef = useRef(null);
  const [showNotificationDropdown, setShowNotificationDropdown] = useState(false);
  const notificationRef = useRef(null);
  const [notification, setNotification] = useState(null);
  const [unreadNotifications, setUnreadNotifications] = useState([]);
  const [managerProfile, setManagerProfile] = useState(null);
  const navigate = useNavigate();
  const stompClient = useRef(null);

  const moneyFormat = (labelValue) => {
    if (!labelValue) return '0';
    return Math.abs(Number(labelValue)) >= 1.0e+9
      ? (Math.abs(Number(labelValue)) / 1.0e+9).toFixed(1) + "B"
      : Math.abs(Number(labelValue)) >= 1.0e+6
      ? (Math.abs(Number(labelValue)) / 1.0e+6).toFixed(1) + "M"
      : Math.abs(Number(labelValue)) >= 1.0e+3
      ? (Math.abs(Number(labelValue)) / 1.0e+3).toFixed(1) + "K"
      : Math.abs(Number(labelValue));
  };

  const getInitials = (name) => {
    if (!name) return 'U';
    return name.substring(0, 2).toUpperCase();
  };

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setShowDropdown(false);
      }
      if (notificationRef.current && !notificationRef.current.contains(event.target)) {
        setShowNotificationDropdown(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  const fetchNotifications = async () => {
      try {
          const response = await getUnreadNotifications();
          setUnreadNotifications(response.data);
      } catch (err) {
          console.error("Failed to fetch notifications", err);
      }
  };

  const fetchManagerProfile = async () => {
      try {
          const response = await getManagerProfile();
          setManagerProfile(response.data);
      } catch (err) {
          console.error("Failed to fetch manager profile", err);
      }
  };

  useEffect(() => {
      if (user) {
          fetchNotifications();
          fetchManagerProfile();
      }
  }, [user]);

  // WebSocket for Notifications
  useEffect(() => {
      if (!user) return;

      const socket = new SockJS(API_BASE_URL + '/ws/live-match');
      stompClient.current = new Client({
          webSocketFactory: () => socket,
          onConnect: () => {
              stompClient.current.subscribe('/user/queue/notifications', (message) => {
                  const notif = JSON.parse(message.body);
                  if (notif.type === 'MATCH_STARTED') {
                      setNotification(notif);
                  } else if (notif.type === 'MATCH_ENDED') {
                      setNotification(prev => (prev && prev.matchId === notif.matchId) ? null : prev);
                  } else {
                      // Handle generic notifications (quests, etc)
                      // If it's a persistent notification, add to unread list and show toast
                      // Assuming backend sends UserNotification object structure here too, or similar
                      // For now, just re-fetch unread list to be safe
                      fetchNotifications();
                      fetchManagerProfile(); // Update XP if needed

                      // Show a temporary toast for non-match notifications
                      setNotification({ message: notif.message, type: 'GENERIC' });
                      setTimeout(() => setNotification(null), 5000);
                  }
              });
          },
          onStompError: (frame) => console.error('STOMP error:', frame)
      });
      stompClient.current.activate();

      return () => {
          if (stompClient.current) stompClient.current.deactivate();
      };
  }, [user]);

  const handleMarkRead = async (id, e) => {
      e.stopPropagation();
      try {
          await markSystemNotificationRead(id);
          setUnreadNotifications(prev => prev.filter(n => n.id !== id));
      } catch (err) {
          console.error("Failed to mark read", err);
      }
  };

  const calculateLevelProgress = () => {
      if (!managerProfile) return 0;
      // Example calculation: currentXp / xpForNextLevel * 100
      // But API returns pre-calculated or raw values?
      // DTO has: currentXp, xpForNextLevel
      if (managerProfile.xpForNextLevel === 0) return 100;
      return (managerProfile.currentXp / managerProfile.xpForNextLevel) * 100;
  };

  return (
    <nav className="navbar">
      {/* Left Section: Toggle & Title (Optional) */}
      <div className="navbar-left">
        <button className="btn btn-primary" onClick={onToggleMenu} aria-label="Toggle menu">
          <i className="fas fa-bars"></i>
        </button>
      </div>

      {/* Center Section: Search */}
      <div className="navbar-search">
        <i className="fas fa-search"></i>
        <input type="text" placeholder="Search players, clubs, matches..." aria-label="Search players, clubs, matches..." />
      </div>

      {/* Right Section: Status & User */}
      <div className="navbar-right">

        {/* Level Indicator */}
        <div className="status-item level-display">
          <div className="level-text">Level {managerProfile?.level || user?.level || 1}</div>
          <div
            className="progress-container"
            role="progressbar"
            aria-valuenow={calculateLevelProgress()}
            aria-valuemin="0"
            aria-valuemax="100"
            aria-label="Level Progress"
          >
            <div
              className="progress-bar-fill"
              style={{ width: `${calculateLevelProgress()}%` }}
            ></div>
          </div>
        </div>

        {/* Money Indicator */}
        <div className="status-item money-display">
          <i className="fas fa-coins"></i>
          <span>{moneyFormat(user?.club?.finance?.balance)}</span>
        </div>

        {/* Notifications */}
        <div className="notification-dropdown-container" ref={notificationRef}>
            <button className="icon-btn" onClick={() => setShowNotificationDropdown(!showNotificationDropdown)} aria-label="Notifications">
            <i className="fas fa-bell"></i>
            {unreadNotifications.length > 0 && <span className="badge">{unreadNotifications.length}</span>}
            </button>

            {showNotificationDropdown && (
                <div className="dropdown-menu notification-menu show">
                    <div className="dropdown-header">Notifications</div>
                    <div className="notification-list">
                        {unreadNotifications.length === 0 ? (
                            <div className="dropdown-item text-muted">No new notifications</div>
                        ) : (
                            unreadNotifications.map(notif => (
                                <div key={notif.id} className="dropdown-item notification-item" onClick={() => {
                                    if (notif.actionUrl) navigate(notif.actionUrl);
                                }}>
                                    <div className="notif-content">
                                        <div className="notif-title">{notif.title}</div>
                                        <div className="notif-message">{notif.message}</div>
                                        <small className="text-muted">{new Date(notif.createdAt).toLocaleTimeString()}</small>
                                    </div>
                                    <button className="btn btn-sm btn-link text-muted" onClick={(e) => handleMarkRead(notif.id, e)}>
                                        <i className="fas fa-check"></i>
                                    </button>
                                </div>
                            ))
                        )}
                    </div>
                    <div className="dropdown-footer">
                        <Link to="/notifications" onClick={() => setShowNotificationDropdown(false)}>View All</Link>
                    </div>
                </div>
            )}
        </div>

        {/* User Profile Dropdown */}
        <div className="user-dropdown" ref={dropdownRef}>
          <div
            className="user-info"
            role="button"
            tabIndex="0"
            aria-haspopup="true"
            aria-expanded={showDropdown}
            onClick={() => setShowDropdown(!showDropdown)}
            onKeyDown={(e) => {
              if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                setShowDropdown(!showDropdown);
              }
            }}
          >
            <div className="avatar">
              {getInitials(user?.username)}
            </div>
            <span className="username">{user?.username}</span>
            <i className={`fas fa-chevron-${showDropdown ? 'up' : 'down'}`} style={{ fontSize: '0.8rem', opacity: 0.7 }}></i>
          </div>

          {showDropdown && (
            <div className="dropdown-menu show">
              <Link to="/manager" className="dropdown-item" onClick={() => setShowDropdown(false)}>
                <i className="fas fa-user-tie"></i> Manager Profile
              </Link>
              <Link to="/profile" className="dropdown-item" onClick={() => setShowDropdown(false)}>
                <i className="fas fa-user"></i> User Settings
              </Link>
              <div className="dropdown-divider"></div>
              <button
                className="dropdown-item"
                onClick={() => {
                  logout();
                  setShowDropdown(false);
                }}
                style={{ color: 'var(--danger)' }}
              >
                <i className="fas fa-sign-out-alt"></i> Logout
              </button>
            </div>
          )}
        </div>
      </div>

      {/* Toast Notification */}
      {notification && (
        <div className="notification-toast" onClick={() => {
            if (notification.matchId) {
                navigate(`/match/live/${notification.matchId}`);
            }
            setNotification(null);
        }}>
            <div className="notification-content">
                {notification.matchId ? <i className="fas fa-futbol bouncing-ball"></i> : <i className="fas fa-info-circle"></i>}
                <span>{notification.message}</span>
            </div>
        </div>
      )}
    </nav>
  );
};

export default Navbar;
