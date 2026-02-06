import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api, { getClub } from '../services/api';
import Layout from '../components/Layout';
import useSortableData from '../hooks/useSortableData';
import InjuryList from '../components/InjuryList';
import TrainingPlan from '../components/TrainingPlan';
import TrainingHistory from '../components/TrainingHistory';
import StaffManagement from '../components/StaffManagement';
import ConfirmationModal from '../components/ConfirmationModal';

const Team = () => {
  const [players, setPlayers] = useState([]);
  const [teamId, setTeamId] = useState(null);
  const [activeTab, setActiveTab] = useState('squad');
  const [showModal, setShowModal] = useState(false);
  const [selectedPlayer, setSelectedPlayer] = useState(null);
  const { items: sortedPlayers, requestSort, sortConfig } = useSortableData(players);

  const user = JSON.parse(localStorage.getItem('user'));
  const clubId = user ? user.clubId : null;

  useEffect(() => {
    const fetchPlayers = async () => {
      try {
        const response = await api.get('/player/');
        setPlayers(response.data);
      } catch (error) {
        console.error('Error fetching players', error);
      }
    };

    const fetchClub = async () => {
        if (clubId) {
            try {
                const response = await getClub(clubId);
                if (response.data && response.data.team) {
                    setTeamId(response.data.team.id);
                }
            } catch (error) {
                console.error('Error fetching club', error);
            }
        }
    };

    fetchPlayers();
    fetchClub();
  }, [clubId]);

  const handlePutOnSale = (player) => {
    setSelectedPlayer(player);
    setShowModal(true);
  };

  const confirmPutOnSale = async () => {
    if (!selectedPlayer) return;
    try {
      await api.post('/player/' + selectedPlayer.id + '/onSale');
      setShowModal(false);
      setSelectedPlayer(null);
    } catch (error) {
      console.error('Error putting player on sale', error);
      setShowModal(false);
    }
  };

  return (
    <Layout>
      <h1 className="mt-2">Team</h1>

      <ul className="nav nav-tabs mb-3">
        <li className="nav-item">
          <button
            className={`nav-link ${activeTab === 'squad' ? 'active' : ''}`}
            onClick={() => setActiveTab('squad')}
          >
            Squad
          </button>
        </li>
        <li className="nav-item">
          <button
            className={`nav-link ${activeTab === 'training' ? 'active' : ''}`}
            onClick={() => setActiveTab('training')}
          >
            Training
          </button>
        </li>
        <li className="nav-item">
          <button
            className={`nav-link ${activeTab === 'history' ? 'active' : ''}`}
            onClick={() => setActiveTab('history')}
          >
            Training History
          </button>
        </li>
        <li className="nav-item">
          <button
            className={`nav-link ${activeTab === 'staff' ? 'active' : ''}`}
            onClick={() => setActiveTab('staff')}
          >
            Staff
          </button>
        </li>
      </ul>

      {activeTab === 'squad' && (
          <>
            {clubId && <InjuryList clubId={clubId} />}
            <table className="table table-striped">
                <thead>
                <tr>
                    <th>Action</th>
                    <th onClick={() => requestSort('role')} style={{cursor: 'pointer'}}>Role</th>
                    <th onClick={() => requestSort('name')} style={{cursor: 'pointer'}}>Name</th>
                    <th onClick={() => requestSort('surname')} style={{cursor: 'pointer'}}>Surname</th>
                    <th onClick={() => requestSort('age')} style={{cursor: 'pointer'}}>Age</th>
                    <th onClick={() => requestSort('stamina')} style={{cursor: 'pointer'}}>Stamina</th>
                    <th onClick={() => requestSort('playmaking')} style={{cursor: 'pointer'}}>Playmaking</th>
                    <th onClick={() => requestSort('scoring')} style={{cursor: 'pointer'}}>Scoring</th>
                    <th onClick={() => requestSort('winger')} style={{cursor: 'pointer'}}>Winger</th>
                    <th onClick={() => requestSort('passing')} style={{cursor: 'pointer'}}>Passing</th>
                    <th onClick={() => requestSort('defending')} style={{cursor: 'pointer'}}>Defending</th>
                    <th onClick={() => requestSort('condition')} style={{cursor: 'pointer'}}>Condition</th>
                </tr>
                </thead>
                <tbody>
                {sortedPlayers.map(p => (
                    <tr key={p.id}>
                    <td>
                        <button className="btn btn-icon" onClick={() => handlePutOnSale(p)} title="Put on sale">
                            <i className="fa fa-exchange-alt"></i>
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
          </>
      )}

      {activeTab === 'training' && teamId && (
          <TrainingPlan teamId={teamId} />
      )}

      {activeTab === 'history' && teamId && (
          <TrainingHistory teamId={teamId} />
      )}

      {activeTab === 'staff' && clubId && (
          <StaffManagement clubId={clubId} />
      )}

      {!teamId && activeTab !== 'squad' && activeTab !== 'staff' && (
          <div>Loading team data...</div>
      )}

      <ConfirmationModal
        isOpen={showModal}
        onClose={() => setShowModal(false)}
        onConfirm={confirmPutOnSale}
        title="Confirm Transfer List"
        message={`Are you sure you want to put ${selectedPlayer?.name} ${selectedPlayer?.surname} on the transfer list?`}
      />

    </Layout>
  );
};

export default Team;
