import React, { useState, useEffect, useCallback } from 'react';
import api from '../services/api';
import Layout from '../components/Layout';
import '../styles/formation.css';

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
          playersId: res.data.players.map(p => p.id)
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
      const [mRes, mentRes] = await Promise.all([
        api.get('/module/'),
        api.get('/mentality/')
      ]);
      setModules(mRes.data);
      setMentalities(mentRes.data);
      fetchPlayers();
      fetchFormation();
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
    const playerId = e.dataTransfer.getData('playerId');
    await api.post(`/player/${playerId}/change-role?role=${slotRole}`);
    fetchPlayers();
    // Update local state or re-fetch formation
    const newPlayersId = [...formation.playersId, parseInt(playerId)];
    setFormation({ ...formation, playersId: newPlayersId });
  };

  const saveFormation = async () => {
    // Collect playersId from UI if needed, or use state
    // The original project uses form serialization.
    // Here we can build the params.
    const params = new URLSearchParams();
    params.append('moduleId', formation.moduleId);
    params.append('mentality', formation.mentality);
    // This part is tricky with multiple playersId in URLSearchParams
    // The backend expects multiple playersId parameters
    formation.playersId.forEach(id => params.append('playersId', id));

    await api.post('/formation/', params);
    alert('Formation saved');
  };

  const autoSelect = async () => {
    await api.get('/formation/auto');
    fetchFormation();
    fetchPlayers();
  };

  const renderSlot = (role, top, left, label) => {
    // Find player in this slot? Original logic was a bit different
    // For simplicity, let's just render the slots based on the module
    return (
      <div
        className="pos"
        onDragOver={(e) => e.preventDefault()}
        onDrop={(e) => onDrop(e, role)}
        style={{ position: 'absolute', top, left, transform: 'translate(-50%, -50%)' }}
      >
        <span className="player-name">{label}</span>
      </div>
    );
  };

  const positions = [];
  if (selectedModule) {
    // GK
    positions.push({ role: 0, top: '90%', left: '50%', label: 'GK' });
    // Defs
    for (let i = 0; i < selectedModule.def; i++)
      positions.push({ role: 1, top: '80%', left: (10 + (80 / (selectedModule.def + 1)) * (i + 1)) + '%', label: 'DEF' });
    // WBs
    for (let i = 0; i < selectedModule.wb; i++)
      positions.push({ role: 2, top: '70%', left: (i % 2 === 0 ? '10%' : '90%') + '%', label: 'WB' });
    // MFs
    for (let i = 0; i < selectedModule.mf; i++)
      positions.push({ role: 3, top: '50%', left: (10 + (80 / (selectedModule.mf + 1)) * (i + 1)) + '%', label: 'MF' });
    // Wings
    for (let i = 0; i < selectedModule.wng; i++)
      positions.push({ role: 4, top: '40%', left: (i % 2 === 0 ? '5%' : '95%') + '%', label: 'WNG' });
    // FWs
    for (let i = 0; i < selectedModule.fw; i++)
      positions.push({ role: 5, top: '20%', left: (10 + (80 / (selectedModule.fw + 1)) * (i + 1)) + '%', label: 'FW' });
  }

  return (
    <Layout>
      <div className="row mt-4">
        <div className="col-sm-12 col-md-5">
          <div className="card shadow mb-4">
            <div className="card-header py-3"><h6 className="m-0 font-weight-bold text-primary">Team Roster</h6></div>
            <div className="card-body">
              <table className="table table-sm">
                <thead>
                  <tr><th>Role</th><th>Player</th><th>Avg</th></tr>
                </thead>
                <tbody>
                  {players.map(p => (
                    <tr key={p.id}>
                      <td>{p.role}</td>
                      <td>
                        <div draggable onDragStart={(e) => onDragStart(e, p.id)} style={{ cursor: 'grab' }}>
                          <i className="fas fa-futbol"></i> {p.surname}
                        </div>
                      </td>
                      <td>{p.average}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
        <div className="col-sm-12 col-md-7">
          <div className="card shadow mb-4">
            <div className="card-header py-3"><h6 className="m-0 font-weight-bold text-primary">Formation & Tactics</h6></div>
            <div className="card-body">
              <div className="row mb-3">
                <div className="col-md-6">
                  <label>Module</label>
                  <select className="form-control" value={formation.moduleId} onChange={handleModuleChange}>
                    <option value="">Select Module</option>
                    {modules.map(m => <option key={m.id} value={m.id}>{m.name}</option>)}
                  </select>
                </div>
                <div className="col-md-6">
                  <label>Mentality</label>
                  <select className="form-control" value={formation.mentality} onChange={(e) => setFormation({...formation, mentality: e.target.value})}>
                    <option value="">Select Mentality</option>
                    {mentalities.map(m => <option key={m} value={m}>{m}</option>)}
                  </select>
                </div>
              </div>
              <div className="pitch" style={{ position: 'relative', height: '500px', backgroundColor: 'green' }}>
                <div className="formation-container">
                  {positions.map((p, i) => renderSlot(p.role, p.top, p.left, p.label))}
                </div>
              </div>
              <div className="mt-4">
                <button className="btn btn-info mr-2" onClick={autoSelect}>Auto Select</button>
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
