import React, { useContext, useState, useEffect, useRef } from 'react';
import { AuthContext } from '../context/AuthContext';
import { Link, useNavigate } from 'react-router-dom';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { API_BASE_URL } from '../services/api';
import '../styles/Navbar.css';

const Navbar = ({ onToggleMenu }) => {
  const { user, logout } = useContext(AuthContext);
  const [showDropdown, setShowDropdown] = useState(false);
  const dropdownRef = useRef(null);
  const [notification, setNotification] = useState(null);
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
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  // WebSocket for Notifications
  useEffect(() => {
      if (!user) return;

      const socket = new SockJS(API_BASE_URL + '/ws/live-match');
      stompClient.current = new Client({
          webSocketFactory: () => socket,
          onConnect: () => {
              stompClient.current.subscribe('/topic/user/' + user.id + '/notifications', (message) => {
                  const notif = JSON.parse(message.body);
                  if (notif.type === 'MATCH_STARTED') {
                      setNotification(notif);
                      setTimeout(() => setNotification(null), 10000); // Hide after 10s
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

  return (
    <nav className="navbar">
      {/* Left Section: Toggle & Title (Optional) */}
      <div className="navbar-left">
        <button className="btn btn-primary" onClick={onToggleMenu}>
          <i className="fas fa-bars"></i>
        </button>
      </div>

      {/* Center Section: Search */}
      <div className="navbar-search">
        <i className="fas fa-search"></i>
        <input type="text" placeholder="Search players, clubs, matches..." />
      </div>

      {/* Right Section: Status & User */}
      <div className="navbar-right">

        {/* Level Indicator */}
        <div className="status-item level-display">
          <div className="level-text">Level {user?.level || 1}</div>
          <div className="progress-container">
            <div
              className="progress-bar-fill"
              style={{ width: `${user?.levelProgress || 0}%` }}
            ></div>
          </div>
        </div>

        {/* Money Indicator */}
        <div className="status-item money-display">
          <i className="fas fa-coins"></i>
          <span>{moneyFormat(user?.club?.finance?.balance)}</span>
        </div>

        {/* Notifications */}
        <button className="icon-btn">
          <i className="fas fa-bell"></i>
          <span className="badge">3</span>
        </button>

        {/* User Profile Dropdown */}
        <div className="user-dropdown" ref={dropdownRef}>
          <div
            className="user-info"
            onClick={() => setShowDropdown(!showDropdown)}
          >
            <div className="avatar">
              {getInitials(user?.username)}
            </div>
            <span className="username">{user?.username}</span>
            <i className={`fas fa-chevron-${showDropdown ? 'up' : 'down'}`} style={{ fontSize: '0.8rem', opacity: 0.7 }}></i>
          </div>

          {showDropdown && (
            <div className="dropdown-menu">
              <Link to="/profile" className="dropdown-item" onClick={() => setShowDropdown(false)}>
                <i className="fas fa-user"></i> Profile
              </Link>
              <Link to="/settings" className="dropdown-item" onClick={() => setShowDropdown(false)}>
                <i className="fas fa-cog"></i> Settings
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
            // Redirect to live match on click
            navigate(`/match/live/${notification.matchId}`);
            setNotification(null);
        }}>
            <div className="notification-content">
                <i className="fas fa-futbol"></i>
                <span>{notification.message}</span>
            </div>
        </div>
      )}
    </nav>
  );
};

export default Navbar;
