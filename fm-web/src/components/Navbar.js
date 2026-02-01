import React, { useContext } from 'react';
import { AuthContext } from '../context/AuthContext';
import { Link } from 'react-router-dom';

const Navbar = ({ onToggleMenu }) => {
  const { user, logout } = useContext(AuthContext);

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

  return (
    <nav className="navbar">
      <button className="btn btn-primary" onClick={onToggleMenu}>
        <i className="fas fa-bars"></i>
      </button>

      <div style={{ marginLeft: '20px', display: 'flex', alignItems: 'center', flexGrow: 1 }}>
        <div className="progress" style={{ width: '150px' }}>
          <div className="progress-bar" style={{ width: `${user?.levelProgress || 0}%` }}>
            Lvl. {user?.level || 0}
          </div>
        </div>

        <div style={{ marginLeft: 'auto', display: 'flex', alignItems: 'center' }}>
          <div style={{ color: '#4caf50', marginRight: '20px', fontWeight: 'bold' }}>
            {moneyFormat(user?.club?.finance?.balance)} <i className="fas fa-money-bill-alt"></i>
          </div>
          <div style={{ marginRight: '20px' }}>{user?.username}</div>
          <button className="btn" style={{ backgroundColor: 'transparent', color: 'var(--text-color)' }} onClick={logout}>
            <i className="fas fa-sign-out-alt"></i> Logout
          </button>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
