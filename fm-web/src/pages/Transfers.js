import React, { useState, useEffect } from 'react';
import api from '../services/api';
import Layout from '../components/Layout';

const Transfers = () => {
  const [playersOnSale, setPlayersOnSale] = useState([]);

  useEffect(() => {
    api.get('/player/onSale').then(res => setPlayersOnSale(res.data));
  }, []);

  const buyPlayer = async (playerId) => {
    try {
      await api.post(`/player/${playerId}/buy`);
      alert('Player bought');
      setPlayersOnSale(playersOnSale.filter(p => p.id !== playerId));
    } catch (e) {
      alert('Error buying player');
    }
  };

  return (
    <Layout>
      <h1 className="mt-2">Transfer Market</h1>
      <table className="table table-striped">
        <thead>
          <tr>
            <th>Name</th>
            <th>Surname</th>
            <th>Price</th>
            <th>Action</th>
          </tr>
        </thead>
        <tbody>
          {playersOnSale.map(p => (
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
