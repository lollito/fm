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

      <hr className="sidebar-divider" />
    </ul>
  );
};

export default Sidebar;
