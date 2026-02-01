import React, { useState, useEffect } from 'react';
import api from '../services/api';
import Layout from '../components/Layout';

const Leagues = () => {
  const [leagues, setLeagues] = useState([]);

  useEffect(() => {
    api.get('/league/').then(res => setLeagues(res.data));
  }, []);

  return (
    <Layout>
      <h1 className="mt-2">Leagues</h1>
      <ul className="list-group">
        {leagues.map(l => (
          <li key={l.id} className="list-group-item">{l.name}</li>
        ))}
      </ul>
    </Layout>
  );
};

export default Leagues;
