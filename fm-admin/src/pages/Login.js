import React, { useState, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import { AuthContext } from '../context/AuthContext';

const Login = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const { login } = useContext(AuthContext);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const response = await api.post('/user/login', { username, password });
      login(response.data);
      navigate('/');
    } catch (error) {
      alert('Login failed');
    }
  };

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', backgroundColor: '#4e73df' }}>
      <div className="card shadow" style={{ width: '100%', maxWidth: '400px' }}>
        <div className="card-body" style={{ padding: '3rem' }}>
          <h1 className="h4 text-gray-900 mb-4 text-center" style={{ textAlign: 'center' }}>Admin Login</h1>
          <form onSubmit={handleSubmit}>
            <div style={{ marginBottom: '1rem' }}>
              <input
                type="text"
                className="form-control"
                placeholder="Username"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                required
                autoFocus
              />
            </div>
            <div style={{ marginBottom: '1rem' }}>
              <input
                type="password"
                className="form-control"
                placeholder="Password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>
            <button className="btn btn-primary" style={{ width: '100%' }} type="submit">Login</button>
          </form>
        </div>
      </div>
    </div>
  );
};

export default Login;
