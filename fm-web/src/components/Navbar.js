import React, { useContext } from 'react';
import { AuthContext } from '../context/AuthContext';
import { Link } from 'react-router-dom';

const Navbar = ({ onToggleMenu }) => {
  const { user, logout } = useContext(AuthContext);

  return (
    <nav className="navbar navbar-expand-lg navbar-light bg-light border-bottom">
      <button className="btn btn-primary" id="menu-toggle" onClick={onToggleMenu}>
        <i className="fas fa-bars fa-1x"></i>
      </button>

      <button className="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarSupportedContent" aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">
        <span className="navbar-toggler-icon"></span>
      </button>

      <div className="progress bg-info float-left ml-3" style={{ width: '150px' }}>
        <div id="level" className="progress-bar bg-primary" role="progressbar" aria-valuenow="0" aria-valuemin="0" aria-valuemax="100">
          Lvl. {user?.level || 0}
        </div>
      </div>

      <div className="collapse navbar-collapse" id="navbarSupportedContent">
        <ul className="navbar-nav ml-auto mt-2 mt-lg-0">
          <li className="nav-item">
            <Link className="nav-link text-success" to="#">
              <span id="balance">{user?.club?.finance?.balance || 0}</span>
              <i className="fas fa-lg fa-money-bill-alt"></i>
            </Link>
          </li>
          <li className="nav-item dropdown">
            <a className="nav-link dropdown-toggle" href="#" id="navbarDropdown" role="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
              <span id="username">{user?.username}</span>
            </a>
            <div className="dropdown-menu dropdown-menu-right" aria-labelledby="navbarDropdown">
              <Link className="dropdown-item" to="/profile">Profile</Link>
              <div className="dropdown-divider"></div>
              <button className="dropdown-item" onClick={logout}>Logout</button>
            </div>
          </li>
        </ul>
      </div>
    </nav>
  );
};

export default Navbar;
