import React, { useState, useEffect } from 'react';
import api from '../services/api';
import Layout from '../components/Layout';
import useSortableData from '../hooks/useSortableData';

const Transfers = () => {
  const [playersOnSale, setPlayersOnSale] = useState([]);
  const { items: sortedPlayers, requestSort, sortConfig } = useSortableData(playersOnSale);

  useEffect(() => {
    api.get('/player/onSale').then(res => setPlayersOnSale(res.data));
  }, []);

  const buyPlayer = async (playerId) => {
    try {
      await api.post('/player/' + playerId + '/buy');
      alert('Player bought');
      setPlayersOnSale(playersOnSale.filter(p => p.id !== playerId));
    } catch (e) {
      alert('Error buying player');
    }
  };

  const getSortIcon = (key) => {
    if (!sortConfig || sortConfig.key !== key) return <i className="fas fa-sort" style={{ marginLeft: '5px', opacity: 0.3 }}></i>;
    return sortConfig.direction === 'ascending' ?
        <i className="fas fa-sort-up" style={{ marginLeft: '5px' }}></i> :
        <i className="fas fa-sort-down" style={{ marginLeft: '5px' }}></i>;
  };

  return (
    <Layout>
      <h1 className="mt-2">Transfer Market</h1>
      <table className="table table-striped">
        <thead>
          <tr>
            <th onClick={() => requestSort('name')} style={{ cursor: 'pointer' }}>Name {getSortIcon('name')}</th>
            <th onClick={() => requestSort('surname')} style={{ cursor: 'pointer' }}>Surname {getSortIcon('surname')}</th>
            <th onClick={() => requestSort('price')} style={{ cursor: 'pointer' }}>Price {getSortIcon('price')}</th>
            <th>Action</th>
          </tr>
        </thead>
        <tbody>
          {sortedPlayers.map(p => (
            <tr key={p.id}>
              <td>{p.name}</td>
              <td>{p.surname}</td>
              <td>{p.price}</td>
              <td>
                <button className="btn btn-success btn-sm" onClick={() => buyPlayer(p.id)}>Buy</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </Layout>
  );
};

export default Transfers;
