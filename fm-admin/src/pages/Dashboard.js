import React, { useState, useEffect } from 'react';
import api from '../services/api';
import Layout from '../components/Layout';

const Dashboard = () => {
  const [stats, setStats] = useState({
    users: 0,
    clubs: 0,
    matches: 0,
    leagues: 0
  });

  useEffect(() => {
    const fetchStats = async () => {
      const [u, c, m, l] = await Promise.all([
        api.get('/user/count'),
        api.get('/club/count'),
        api.get('/match/count'),
        api.get('/league/count')
      ]);
      setStats({
        users: u.data,
        clubs: c.data,
        matches: m.data,
        leagues: l.data
      });
    };
    fetchStats();
  }, []);

  const Card = ({ title, value, icon, color }) => (
    <div className="col-xl-3 col-md-6 mb-4">
      <div className={`card border-left-${color} shadow h-100 py-2`}>
        <div className="card-body">
          <div className="row no-gutters align-items-center">
            <div className="col mr-2">
              <div className={`text-xs font-weight-bold text-${color} text-uppercase mb-1`}>{title}</div>
              <div className="h5 mb-0 font-weight-bold text-gray-800">{value}</div>
            </div>
            <div className="col-auto">
              <i className={`fas fa-${icon} fa-2x text-gray-300`}></i>
            </div>
          </div>
        </div>
      </div>
    </div>
  );

  return (
    <Layout>
      <div className="d-sm-flex align-items-center justify-content-between mb-4">
        <h1 className="h3 mb-0 text-gray-800">Admin Dashboard</h1>
      </div>
      <div className="row">
        <Card title="Users" value={stats.users} icon="user" color="warning" />
        <Card title="Clubs" value={stats.clubs} icon="users" color="warning" />
        <Card title="Matches" value={stats.matches} icon="gamepad" color="warning" />
        <Card title="Leagues" value={stats.leagues} icon="shield-alt" color="warning" />
      </div>
    </Layout>
  );
};

export default Dashboard;
