import React, { useState, useEffect } from 'react';
import api from '../services/api';
import Layout from '../components/Layout';
import useSortableData from '../hooks/useSortableData';

const Leagues = () => {
  const [leagues, setLeagues] = useState([]);
  const { items: sortedLeagues, requestSort, sortConfig } = useSortableData(leagues);

  useEffect(() => {
    api.get('/league/').then(res => setLeagues(res.data));
  }, []);

  const getSortIcon = (key) => {
    if (!sortConfig || sortConfig.key !== key) return <i className="fas fa-sort" style={{ marginLeft: '5px', opacity: 0.3 }}></i>;
    return sortConfig.direction === 'ascending' ?
        <i className="fas fa-sort-up" style={{ marginLeft: '5px' }}></i> :
        <i className="fas fa-sort-down" style={{ marginLeft: '5px' }}></i>;
  };

  return (
    <Layout>
      <h1 className="mt-2">Leagues</h1>
      <table className="table table-striped">
        <thead>
          <tr>
            <th onClick={() => requestSort('name')} style={{ cursor: 'pointer' }}>Name {getSortIcon('name')}</th>
            <th onClick={() => requestSort('season.year')} style={{ cursor: 'pointer' }}>Season {getSortIcon('season.year')}</th>
          </tr>
        </thead>
        <tbody>
          {sortedLeagues.map(l => (
            <tr key={l.id}>
              <td>{l.name}</td>
              <td>{l.season?.year}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </Layout>
  );
};

export default Leagues;
