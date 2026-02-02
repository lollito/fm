import React, { useState, useEffect, useCallback } from 'react';
import api from '../services/api';
import Layout from '../components/Layout';

const PLAYER_ROLES = {
  GK: 0,
  DEFENDER: 1,
  WINGBACK: 2,
  MIDFIELDER: 3,
  WING: 4,
  FORWARD: 5
};

const ROLE_NAMES = {
  [PLAYER_ROLES.GK]: 'GOALKEEPER',
  [PLAYER_ROLES.DEFENDER]: 'DEFENDER',
  [PLAYER_ROLES.WINGBACK]: 'WINGBACK',
  [PLAYER_ROLES.MIDFIELDER]: 'MIDFIELDER',
  [PLAYER_ROLES.WING]: 'WING',
  [PLAYER_ROLES.FORWARD]: 'FORWARD'
};

const Formation = () => {
  const [players, setPlayers] = useState([]);
  const [modules, setModules] = useState([]);
  const [mentalities, setMentalities] = useState([]);
  const [formation, setFormation] = useState({ moduleId: '', mentality: '', playersId: [] });
  const [selectedModule, setSelectedModule] = useState(null);

  const fetchPlayers = useCallback(async () => {
    const res = await api.get('/player/');
    setPlayers(res.data);
  }, []);

  const fetchFormation = useCallback(async () => {
    try {
      const res = await api.get('/formation/');
      if (res.data) {
        setFormation({
          moduleId: res.data.module.id,
          mentality: res.data.mentality,
          playersId: res.data.players.filter(p => p != null).map(p => p.id)
        });
        const mRes = await api.get('/module/');
        const mod = mRes.data.find(m => m.id === res.data.module.id);
        setSelectedModule(mod);
      }
    } catch (e) {
      console.error('No formation found');
    }
  }, []);

  useEffect(() => {
    const init = async () => {
      try {
        const [mRes, mentRes] = await Promise.all([
          api.get('/module/'),
          api.get('/mentality/')
        ]);
        setModules(mRes.data);
        setMentalities(mentRes.data);
        fetchPlayers();
        fetchFormation();
      } catch (e) {
        console.error('Error initializing data', e);
      }
    };
    init();
  }, [fetchPlayers, fetchFormation]);

  const handleModuleChange = (e) => {
    const moduleId = e.target.value;
    const mod = modules.find(m => m.id === parseInt(moduleId));
    setSelectedModule(mod);
    setFormation({ ...formation, moduleId });
  };

  const onDragStart = (e, playerId) => {
    e.dataTransfer.setData('playerId', playerId);
  };

  const onDrop = async (e, slotRole) => {
    e.preventDefault();
    const playerId = parseInt(e.dataTransfer.getData('playerId'));
    try {
        await api.post(`/player/${playerId}/change-role?role=${slotRole}`);
        setFormation(prev => {
            if (prev.playersId.includes(playerId)) return prev;
            return { ...prev, playersId: [...prev.playersId, playerId] };
        });
        fetchPlayers();
    } catch (error) {
        alert('Error changing role: ' + (error.response?.data?.message || error.message));
    }
  };

  const saveFormation = async () => {
    if (formation.playersId.length !== 11) {
        alert('You must select exactly 11 players. Current: ' + formation.playersId.length);
        return;
    }
    const params = new URLSearchParams();
    params.append('moduleId', formation.moduleId);
    params.append('mentality', formation.mentality);
    formation.playersId.forEach(id => params.append('playersId', id));

    try {
        await api.post('/formation/', params);
        alert('Formation saved');
    } catch (error) {
        alert('Error saving formation: ' + (error.response?.data?.message || error.message));
    }
  };

  const autoSelect = async () => {
    try {
        await api.get('/formation/auto');
        await fetchFormation();
        await fetchPlayers();
    } catch (error) {
        alert('Error in auto select: ' + (error.response?.data?.message || error.message));
    }
  };

  const renderSlot = (role, top, left, label, index) => {
    const assignedPlayers = players.filter(p => {
        const pRole = typeof p.role === 'string' ? p.role : (p.role?.name || ROLE_NAMES[p.role]);
        return pRole === ROLE_NAMES[role] && formation.playersId.includes(p.id);
    });

    const player = assignedPlayers[index];

    return (
      <div
        key={`${role}-${top}-${left}-${index}`}
        className="pos"
        onDragOver={(e) => e.preventDefault()}
        onDrop={(e) => onDrop(e, role)}
        style={{ position: 'absolute', top, left, transform: 'translate(-50%, -50%)' }}
      >
        <span className="player-name">{player ? player.surname : label}</span>
      </div>
    );
  };

  const positions = [];
  if (selectedModule) {
    let roleCount = { 0: 0, 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 };

    // GK
    positions.push({ role: PLAYER_ROLES.GK, top: '85%', left: '50%', label: 'GK', index: roleCount[PLAYER_ROLES.GK]++ });

    // DEF
    for (let i = 0; i < (selectedModule.cd || 0); i++)
      positions.push({ role: PLAYER_ROLES.DEFENDER, top: '70%', left: (20 + (60 / (selectedModule.cd + 1)) * (i + 1)) + '%', label: 'DEF', index: roleCount[PLAYER_ROLES.DEFENDER]++ });

    // WB
    for (let i = 0; i < (selectedModule.wb || 0); i++)
      positions.push({ role: PLAYER_ROLES.WINGBACK, top: '60%', left: (i % 2 === 0 ? '15%' : '85%') + '%', label: 'WB', index: roleCount[PLAYER_ROLES.WINGBACK]++ });

    // MF
    for (let i = 0; i < (selectedModule.mf || 0); i++)
      positions.push({ role: PLAYER_ROLES.MIDFIELDER, top: '45%', left: (20 + (60 / (selectedModule.mf + 1)) * (i + 1)) + '%', label: 'MF', index: roleCount[PLAYER_ROLES.MIDFIELDER]++ });

    // WNG
    for (let i = 0; i < (selectedModule.wng || 0); i++)
      positions.push({ role: PLAYER_ROLES.WING, top: '35%', left: (i % 2 === 0 ? '10%' : '90%') + '%', label: 'WNG', index: roleCount[PLAYER_ROLES.WING]++ });

    // FW
    for (let i = 0; i < (selectedModule.fw || 0); i++)
      positions.push({ role: PLAYER_ROLES.FORWARD, top: '15%', left: (20 + (60 / (selectedModule.fw + 1)) * (i + 1)) + '%', label: 'FW', index: roleCount[PLAYER_ROLES.FORWARD]++ });
  }

  return (
    <Layout>
      <div className="row">
        <div className="col-4">
          <div className="card" style={{ maxHeight: '800px', overflowY: 'auto' }}>
            <div className="card-header">Team Roster</div>
            <div className="card-body">
              <table className="table">
                <thead>
                  <tr><th>Role</th><th>Player</th><th>Avg</th></tr>
                </thead>
                <tbody>
                  {players.map(p => {
                    const isInFormation = formation.playersId.includes(p.id);
                    return (
                        <tr key={p.id} style={{ opacity: isInFormation ? 0.6 : 1 }}>
                          <td>{p.role}</td>
                          <td>
                            <div draggable onDragStart={(e) => onDragStart(e, p.id)} style={{ cursor: 'grab' }}>
                              <i className={`fas ${isInFormation ? 'fa-check-circle text-success' : 'fa-futbol'}`}></i> {p.surname}
                            </div>
                          </td>
                          <td>{p.average}</td>
                        </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          </div>
        </div>
        <div className="col">
          <div className="card">
            <div className="card-header">Formation & Tactics</div>
            <div className="card-body">
              <div className="row" style={{ marginBottom: '20px' }}>
                <div className="col">
                  <label>Module</label>
                  <select className="form-control" value={formation.moduleId} onChange={handleModuleChange}>
                    <option value="">Select Module</option>
                    {modules.map(m => <option key={m.id} value={m.id}>{m.name}</option>)}
                  </select>
                </div>
                <div className="col">
                  <label>Mentality</label>
                  <select className="form-control" value={formation.mentality} onChange={(e) => setFormation({...formation, mentality: e.target.value})}>
                    <option value="">Select Mentality</option>
                    {mentalities.map(m => <option key={m} value={m}>{m}</option>)}
                  </select>
                </div>
              </div>
              <div className="pitch">
                <div className="formation-container">
                  {positions.map((p) => renderSlot(p.role, p.top, p.left, p.label, p.index))}
                </div>
              </div>
              <div style={{ marginTop: '20px', display: 'flex', gap: '10px' }}>
                <button className="btn btn-outline" onClick={autoSelect}>Auto Select</button>
                <button className="btn btn-primary" onClick={saveFormation}>Save Formation</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Layout>
  );
};

export default Formation;
