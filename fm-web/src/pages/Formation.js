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
  const [formation, setFormation] = useState({ moduleId: '', mentality: '', playersId: Array(11).fill(null) });
  const [selectedModule, setSelectedModule] = useState(null);

  const fetchPlayers = useCallback(async () => {
    const res = await api.get('/player/');
    setPlayers(res.data);
  }, []);

  const fetchFormation = useCallback(async () => {
    try {
      const res = await api.get('/formation/');
      if (res.data) {
        const pIds = Array(11).fill(null);
        if (res.data.players) {
            res.data.players.forEach((p, idx) => {
                if (p && idx < 11) pIds[idx] = p.id;
            });
        }
        setFormation({
          moduleId: res.data.module.id,
          mentality: res.data.mentality,
          playersId: pIds
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
    setFormation({ ...formation, moduleId: parseInt(moduleId) });
  };

  const onDragStart = (e, playerId, sourceSlotIndex = "") => {
    e.dataTransfer.setData('playerId', playerId);
    e.dataTransfer.setData('sourceSlotIndex', sourceSlotIndex);
  };

  const onDrop = (e, slotIndex) => {
    e.preventDefault();
    const draggedPlayerId = parseInt(e.dataTransfer.getData('playerId'));
    const sourceSlotIndex = e.dataTransfer.getData('sourceSlotIndex');

    setFormation(prev => {
        const newPlayersId = [...prev.playersId];

        if (sourceSlotIndex !== "") {
            // Swapping between slots
            const srcIdx = parseInt(sourceSlotIndex);
            const targetPlayerId = newPlayersId[slotIndex];
            newPlayersId[slotIndex] = draggedPlayerId;
            newPlayersId[srcIdx] = targetPlayerId;
        } else {
            // From roster to slot
            const existingIndex = newPlayersId.indexOf(draggedPlayerId);
            if (existingIndex !== -1) {
                newPlayersId[existingIndex] = null;
            }
            newPlayersId[slotIndex] = draggedPlayerId;
        }
        return { ...prev, playersId: newPlayersId };
    });
  };

  const unassignPlayer = (slotIndex) => {
    setFormation(prev => {
        const newPlayersId = [...prev.playersId];
        newPlayersId[slotIndex] = null;
        return { ...prev, playersId: newPlayersId };
    });
  }

  const saveFormation = async () => {
    if (formation.playersId.some(id => id === null)) {
        alert('You must fill all 11 positions.');
        return;
    }
    const params = new URLSearchParams();
    params.append('moduleId', formation.moduleId);
    params.append('mentality', formation.mentality);
    formation.playersId.forEach(id => params.append('playersId', id));

    try {
        await api.post('/formation/', params);
        alert('Formation saved');
        fetchFormation();
        fetchPlayers();
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

  const getPlayerRoleName = (player) => {
    if (!player || player.role === undefined || player.role === null) return '';
    if (typeof player.role === 'string') return player.role;
    if (typeof player.role === 'number') return ROLE_NAMES[player.role];
    return player.role.name || '';
  };

  const renderSlot = (role, top, left, label, index) => {
    const playerId = formation.playersId[index];
    const player = players.find(p => p.id === playerId);

    const pRole = getPlayerRoleName(player);
    const isOutOfRole = player && pRole !== ROLE_NAMES[role];

    return (
      <div
        key={`${role}-${top}-${left}-${index}`}
        className={`pos ${isOutOfRole ? 'out-of-role' : ''}`}
        draggable={!!player}
        onDragStart={(e) => player && onDragStart(e, player.id, index)}
        onDragOver={(e) => e.preventDefault()}
        onDrop={(e) => onDrop(e, index)}
        onClick={() => player && unassignPlayer(index)}
        style={{ position: 'absolute', top, left, transform: 'translate(-50%, -50%)', border: isOutOfRole ? '2px solid var(--warning)' : '2px solid white' }}
      >
        {isOutOfRole && <div style={{ position: 'absolute', top: -10, right: -10, background: 'var(--warning)', borderRadius: '50%', width: 20, height: 20, display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'black', fontSize: '12px' }}>!</div>}
        <span className="player-name">
            {player ? `${player.surname} (${player.average})` : label}
        </span>
      </div>
    );
  };

  const positions = [];
  if (selectedModule) {
    // GK
    positions.push({ role: PLAYER_ROLES.GK, top: '85%', left: '50%', label: 'GK', index: 0 });

    // DEF
    for (let i = 0; i < (selectedModule.cd || 0); i++)
      positions.push({ role: PLAYER_ROLES.DEFENDER, top: '70%', left: (20 + (60 / (selectedModule.cd + 1)) * (i + 1)) + '%', label: 'DEF', index: positions.length });

    // WB
    for (let i = 0; i < (selectedModule.wb || 0); i++)
      positions.push({ role: PLAYER_ROLES.WINGBACK, top: '60%', left: (i % 2 === 0 ? '15%' : '85%') + '%', label: 'WB', index: positions.length });

    // MF
    for (let i = 0; i < (selectedModule.mf || 0); i++)
      positions.push({ role: PLAYER_ROLES.MIDFIELDER, top: '45%', left: (20 + (60 / (selectedModule.mf + 1)) * (i + 1)) + '%', label: 'MF', index: positions.length });

    // WNG
    for (let i = 0; i < (selectedModule.wng || 0); i++)
      positions.push({ role: PLAYER_ROLES.WING, top: '35%', left: (i % 2 === 0 ? '10%' : '90%') + '%', label: 'WNG', index: positions.length });

    // FW
    for (let i = 0; i < (selectedModule.fw || 0); i++)
      positions.push({ role: PLAYER_ROLES.FORWARD, top: '15%', left: (20 + (60 / (selectedModule.fw + 1)) * (i + 1)) + '%', label: 'FW', index: positions.length });
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
                          <td>{getPlayerRoleName(p)}</td>
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
