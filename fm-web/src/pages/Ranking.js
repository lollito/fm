import React, { useState, useEffect } from 'react';
import api from '../services/api';
import Layout from '../components/Layout';
import useSortableData from '../hooks/useSortableData';

const Ranking = () => {
  const [ranking, setRanking] = useState([]);
  const { items: sortedRanking, requestSort, sortConfig } = useSortableData(ranking);

  useEffect(() => {
    api.get('/ranking/').then(res => setRanking(res.data));
  }, []);

  const getSortIcon = (key) => {
    if (!sortConfig || sortConfig.key !== key) return <i className="fas fa-sort" style={{ marginLeft: '5px', opacity: 0.3 }}></i>;
    return sortConfig.direction === 'ascending' ?
        <i className="fas fa-sort-up" style={{ marginLeft: '5px' }}></i> :
        <i className="fas fa-sort-down" style={{ marginLeft: '5px' }}></i>;
  };

  return (
    <Layout>
      <h1 className="mt-2">Ranking</h1>
      <table className="table table-striped">
        <thead>
          <tr>
            <th>Pos</th>
            <th onClick={() => requestSort('club.name')} style={{ cursor: 'pointer' }}>
                Club {getSortIcon('club.name')}
            </th>
            <th onClick={() => requestSort('played')} style={{ cursor: 'pointer' }}>
                Played {getSortIcon('played')}
            </th>
            <th onClick={() => requestSort('won')} style={{ cursor: 'pointer' }}>
                Won {getSortIcon('won')}
            </th>
            <th onClick={() => requestSort('drawn')} style={{ cursor: 'pointer' }}>
                Drawn {getSortIcon('drawn')}
            </th>
            <th onClick={() => requestSort('lost')} style={{ cursor: 'pointer' }}>
                Lost {getSortIcon('lost')}
            </th>
            <th onClick={() => requestSort('points')} style={{ cursor: 'pointer' }}>
                Points {getSortIcon('points')}
            </th>
          </tr>
        </thead>
        <tbody>
          {sortedRanking.map((r, i) => (
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
