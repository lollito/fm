import React from 'react';
import { Link } from 'react-router-dom';

const Sidebar = () => {
  return (
    <div id="sidebar-wrapper">
      <div className="sidebar-heading">FM </div>
      <div className="list-group">
        <Link to="/" className="list-group-item">
          <i className="fas fa-fw fa-tachometer-alt"></i><span> Home</span>
        </Link>
        <Link to="/team" className="list-group-item">Team</Link>
        <Link to="/formation" className="list-group-item">Formation</Link>
        <Link to="/ranking" className="list-group-item">Ranking</Link>
        <Link to="/schedule" className="list-group-item">Schedule</Link>
        <Link to="/history" className="list-group-item">Match History</Link>
        <Link to="/upcoming-matches" className="list-group-item">Upcoming Matches</Link>
        <Link to="/leagues" className="list-group-item">Leagues</Link>
        <Link to="/transfers" className="list-group-item">Transfers</Link>
        <Link to="/loans" className="list-group-item" style={{ paddingLeft: '40px' }}>Loans</Link>
        <Link to="/scouting" className="list-group-item">Scouting</Link>
        <Link to="/finance" className="list-group-item">Finance</Link>
        <Link to="/infrastructure" className="list-group-item">Infrastructure</Link>
      </div>
    </div>
  );
};

export default Sidebar;
