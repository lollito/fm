import React from 'react';
import { Link, useLocation } from 'react-router-dom';

const Sidebar = () => {
  const location = useLocation();

  return (
    <ul id="accordionSidebar">
      <Link className="sidebar-brand" to="/">
        <div className="sidebar-brand-icon rotate-n-15">
          <i className="fas fa-laugh-wink"></i>
        </div>
        <div className="sidebar-brand-text mx-3">FM ADMIN</div>
      </Link>

      <hr className="sidebar-divider" />

      <li className={'nav-item ' + (location.pathname === '/' ? 'active' : '')}>
        <Link className="nav-link" to="/">
          <i className="fas fa-fw fa-tachometer-alt"></i>
          <span> Dashboard</span>
        </Link>
      </li>

      <li className={'nav-item ' + (location.pathname === '/users' ? 'active' : '')}>
        <Link className="nav-link" to="/users">
          <i className="fas fa-fw fa-users"></i>
          <span> User Management</span>
        </Link>
      </li>

      <li className={'nav-item ' + (location.pathname === '/servers' ? 'active' : '')}>
        <Link className="nav-link" to="/servers">
          <i className="fas fa-fw fa-server"></i>
          <span> Server Management</span>
        </Link>
      </li>

      <li className={'nav-item ' + (location.pathname === '/live-matches' ? 'active' : '')}>
        <Link className="nav-link" to="/live-matches">
          <i className="fas fa-fw fa-video"></i>
          <span> Live Matches</span>
        </Link>
      </li>

      <li className={'nav-item ' + (location.pathname === '/manager' ? 'active' : '')}>
        <Link className="nav-link" to="/manager">
          <i className="fas fa-fw fa-user-tie"></i>
          <span> Manager Profile</span>
        </Link>
      </li>

      <hr className="sidebar-divider" />
    </ul>
  );
};

export default Sidebar;
