import React, { useState, useEffect } from 'react';
import api from '../services/api';
import Layout from '../components/Layout';

const Ranking = () => {
  const [ranking, setRanking] = useState([]);

  useEffect(() => {
    api.get('/ranking/').then(res => setRanking(res.data));
  }, []);

  return (
    <Layout>
      <h1 className="mt-2">Ranking</h1>
      <table className="table table-striped">
        <thead>
          <tr>
            <th>Pos</th>
            <th>Club</th>
            <th>Played</th>
            <th>Won</th>
            <th>Drawn</th>
            <th>Lost</th>
            <th>Points</th>
          </tr>
        </thead>
        <tbody>
          {ranking.map((r, i) => (
            <tr key={i}>
              <td>{i + 1}</td>
              <td>{r.club.name}</td>
              <td>{r.played}</td>
              <td>{r.won}</td>
              <td>{r.drawn}</td>
              <td>{r.lost}</td>
              <td>{r.points}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </Layout>
  );
};

export default Ranking;
