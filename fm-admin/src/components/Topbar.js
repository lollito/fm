import React, { useContext } from 'react';
import { AuthContext } from '../context/AuthContext';

const Topbar = () => {
  const { user, logout } = useContext(AuthContext);

  return (
    <nav className="topbar shadow">
      <div style={{ marginLeft: 'auto', display: 'flex', alignItems: 'center' }}>
        <span style={{ marginRight: '1rem', color: '#5a5c69', fontWeight: '700' }}>{user?.username}</span>
        <button className="btn btn-primary btn-sm" onClick={logout}>
          <i className="fas fa-sign-out-alt fa-sm"></i> Logout
        </button>
      </div>
    </nav>
  );
};

export default Topbar;
