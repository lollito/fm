import React, { useState, useEffect } from 'react';
import api from '../services/api';
import Layout from '../components/Layout';
import QuestList from '../components/Quest/QuestList';

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

  const Card = ({ title, value }) => (
    <div className="col">
      <div className="card">
        <div className="text-xs">{title}</div>
        <div className="h5">{value}</div>
      </div>
    </div>
  );

  return (
    <Layout>
      <h1 style={{ color: 'var(--text)', marginBottom: '32px' }}>Dashboard</h1>
      <div className="row mb-4">
        <Card title="Users" value={stats.users} />
        <Card title="Clubs" value={stats.clubs} />
        <Card title="Matches" value={stats.matches} />
        <Card title="Leagues" value={stats.leagues} />
      </div>

      <div className="row">
        <div className="col">
          <div className="card">
            <div className="card-header">Active Quests</div>
            <div className="card-body">
              <QuestList limit={3} />
            </div>
          </div>
        </div>
      </div>
    </Layout>
  );
};

export default Dashboard;
