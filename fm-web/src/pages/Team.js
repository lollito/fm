import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';
import Layout from '../components/Layout';
import useSortableData from '../hooks/useSortableData';
import InjuryList from '../components/InjuryList';

const Team = () => {
  const [players, setPlayers] = useState([]);
  const { items: sortedPlayers, requestSort, sortConfig } = useSortableData(players);

  useEffect(() => {
    const fetchPlayers = async () => {
      try {
        const response = await api.get('/player/');
        setPlayers(response.data);
      } catch (error) {
        console.error('Error fetching players', error);
      }
    };
    fetchPlayers();
  }, []);

  const putOnSale = async (playerId) => {
    try {
      await api.post('/player/' + playerId + '/onSale');
      alert('Player put on sale');
    } catch (error) {
      alert('Error putting player on sale');
    }
  };

  const user = JSON.parse(localStorage.getItem('user'));
  const clubId = user ? user.clubId : null;

  return (
    <Layout>
      <h1 className="mt-2">Team</h1>
      {clubId && <InjuryList clubId={clubId} />}
      <table className="table table-striped">
        <thead>
          <tr>
            <th>Action</th>
            <th>Role</th>
            <th>Name</th>
            <th>Surname</th>
            <th>Age</th>
            <th>Stamina</th>
            <th>Playmaking</th>
            <th>Scoring</th>
            <th>Winger</th>
            <th>Passing</th>
            <th>Defending</th>
            <th>Condition</th>
          </tr>
        </thead>
        <tbody>
          {players.map(p => (
            <tr key={p.id}>
              <td>
                <button className="btn btn-link p-0" onClick={() => putOnSale(p.id)}>
                  <i className="fa fa-exchange-alt" style={{ color: '#8c2457' }}></i>
                </button>
              </td>
              <td>{p.role}</td>
              <td>{p.name}</td>
              <td>
                <Link to={'/player/' + p.id}>{p.surname}</Link>
              </td>
              <td>{p.age}</td>
              <td>{Math.round(p.stamina)}</td>
              <td>{Math.round(p.playmaking)}</td>
              <td>{Math.round(p.scoring)}</td>
              <td>{Math.round(p.winger)}</td>
              <td>{Math.round(p.passing)}</td>
              <td>{Math.round(p.defending)}</td>
              <td>{Math.round(p.condition)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </Layout>
  );
};

export default Team;
