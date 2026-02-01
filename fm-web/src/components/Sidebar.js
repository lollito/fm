import React from 'react';
import { Link } from 'react-router-dom';

const Sidebar = () => {
  return (
    <div className="bg-primary text-white border-right" id="sidebar-wrapper">
      <div className="sidebar-heading">FM </div>
      <div className="list-group list-group-flush">
        <Link to="/" className="list-group-item list-group-item-action bg-primary text-white">
          <i className="fas fa-fw fa-tachometer-alt"></i><span>Home</span>
        </Link>
        <Link to="/team" className="list-group-item list-group-item-action bg-primary text-white">Team</Link>
        <Link to="/formation" className="list-group-item list-group-item-action bg-primary text-white">Formation</Link>
        <Link to="/ranking" className="list-group-item list-group-item-action bg-primary text-white">Ranking</Link>
        <Link to="/schedule" className="list-group-item list-group-item-action bg-primary text-white">Schedule</Link>
        <Link to="/leagues" className="list-group-item list-group-item-action bg-primary text-white">Leagues</Link>
        <Link to="/transfers" className="list-group-item list-group-item-action bg-primary text-white">Transfers</Link>
        <Link to="/stadium" className="list-group-item list-group-item-action bg-primary text-white">Stadium</Link>
      </div>
    </div>
  );
};

export default Sidebar;
