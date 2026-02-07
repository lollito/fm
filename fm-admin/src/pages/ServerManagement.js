import React, { useState, useEffect } from 'react';
import { getServers, createServer } from '../services/api';
import { useToast } from '../context/ToastContext';
import Layout from '../components/Layout';

const ServerManagement = () => {
  const { showToast } = useToast();
  const [servers, setServers] = useState([]);
  const [newServerName, setNewServerName] = useState('');

  const fetchServers = async () => {
    try {
        const response = await getServers();
        setServers(response.data);
    } catch(e) {
        console.error(e);
    }
  };

  useEffect(() => {
    fetchServers();
  }, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    if(!newServerName) return;
    try {
        await createServer(newServerName);
        setNewServerName('');
        fetchServers();
        showToast('Server created successfully', 'success');
    } catch(e) {
        showToast('Error creating server', 'error');
    }
  }

  return (
    <Layout>
      <h1 style={{ color: 'var(--text)', marginBottom: '32px' }}>Server Management</h1>

      <div className="card mb-4">
        <div className="card-header">Create Server</div>
        <div className="card-body">
            <form onSubmit={handleCreate} className="form-inline">
                <div className="form-group mr-2">
                    <input
                        type="text"
                        className="form-control"
                        placeholder="Server Name"
                        value={newServerName}
                        onChange={e => setNewServerName(e.target.value)}
                    />
                </div>
                <button type="submit" className="btn btn-primary">Create</button>
            </form>
        </div>
      </div>

      <div className="card">
        <div className="card-header">Existing Servers</div>
        <div className="card-body">
            <table className="table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Name</th>
                        <th>Current Date</th>
                    </tr>
                </thead>
                <tbody>
                    {servers.map(s => (
                        <tr key={s.id}>
                            <td>{s.id}</td>
                            <td>{s.name}</td>
                            <td>{s.currentDate}</td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
      </div>
    </Layout>
  );
};

export default ServerManagement;
