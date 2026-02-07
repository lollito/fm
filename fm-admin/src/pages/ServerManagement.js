import React, { useState, useEffect } from 'react';
import { getServers, createServer, forceNextDay, deleteServer } from '../services/api';
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

  const handleNextDay = async () => {
    try {
        await forceNextDay();
        showToast('Forced next day successfully', 'success');
        fetchServers();
    } catch(e) {
        showToast('Error forcing next day', 'error');
    }
  };

  const handleDelete = async (id) => {
    if(!window.confirm('Are you sure you want to delete this server?')) return;
    try {
        await deleteServer(id);
        showToast('Server deleted successfully', 'success');
        fetchServers();
    } catch(e) {
        showToast('Error deleting server', 'error');
    }
  };

  return (
    <Layout>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h1 style={{ color: 'var(--text)', marginBottom: 0 }}>Server Management</h1>
        <button className="btn btn-warning" onClick={handleNextDay}>Force Next Day</button>
      </div>

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
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    {servers.map(s => (
                        <tr key={s.id}>
                            <td>{s.id}</td>
                            <td>{s.name}</td>
                            <td>{s.currentDate}</td>
                            <td>
                                <button className="btn btn-danger btn-sm" onClick={() => handleDelete(s.id)}>Delete</button>
                            </td>
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
