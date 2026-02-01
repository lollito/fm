import React, { useState, useContext } from 'react';
import { useNavigate, Link } from 'react-router-dom';
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
    <div className="container" style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
      <div className="card" style={{ width: '100%', maxWidth: '400px' }}>
        <div className="card-body">
          <h2 className="card-title text-center" style={{ textAlign: 'center', color: 'var(--accent-color)' }}>FM Login</h2>
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label>Username</label>
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

            <div className="form-group">
              <label>Password</label>
              <input
                type="password"
                className="form-control"
                placeholder="Password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>

            <button className="btn btn-primary" style={{ width: '100%' }} type="submit">Sign in</button>
          </form>
          <hr style={{ border: '0.5px solid rgba(255,255,255,0.1)', margin: '20px 0' }} />
          <div style={{ textAlign: 'center' }}>
            <Link className="small" to="/register" style={{ color: 'var(--accent-color)' }}>Create an Account!</Link>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Login;
