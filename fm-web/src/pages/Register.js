import React, { useState, useEffect, useContext } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api, { getServers } from '../services/api';
import { AuthContext } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';

const Register = () => {
  const { showToast } = useToast();
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    emailConfirm: '',
    password: '',
    passwordConfirm: '',
    countryId: '',
    clubName: '',
    serverId: ''
  });
  const [countries, setCountries] = useState([]);
  const [servers, setServers] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const { login } = useContext(AuthContext);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchCountries = async () => {
      try {
        const response = await api.get('/country/');
        setCountries(response.data);
      } catch (error) {
        console.error('Error fetching countries', error);
      }
    };
    const fetchServers = async () => {
        try {
          const response = await getServers();
          setServers(response.data);
          if (response.data.length > 0) {
              setFormData(prev => ({ ...prev, serverId: response.data[0].id }));
          }
        } catch (error) {
          console.error('Error fetching servers', error);
        }
      };
    fetchCountries();
    fetchServers();
  }, []);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (formData.email !== formData.emailConfirm) {
      showToast('Emails do not match', 'error');
      return;
    }
    if (formData.password !== formData.passwordConfirm) {
      showToast('Passwords do not match', 'error');
      return;
    }

    setIsLoading(true);
    try {
      const response = await api.post('/user/register', formData);
      login(response.data);
      navigate('/');
    } catch (error) {
      showToast('Registration failed', 'error');
      setIsLoading(false);
    }
  };

  return (
    <div className="container" style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', padding: '20px' }}>
      <div className="card" style={{ width: '100%', maxWidth: '500px' }}>
        <div className="card-body">
          <h2 className="card-title text-center" style={{ textAlign: 'center', color: 'var(--accent-color)' }}>FM Register</h2>
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label htmlFor="username">Username</label>
              <input
                id="username"
                type="text"
                name="username"
                className="form-control"
                placeholder="Username"
                value={formData.username}
                onChange={handleChange}
                required
                disabled={isLoading}
              />
            </div>
            <div className="form-group">
              <label htmlFor="email">Email address</label>
              <input
                id="email"
                type="email"
                name="email"
                className="form-control"
                placeholder="Email address"
                value={formData.email}
                onChange={handleChange}
                required
                disabled={isLoading}
              />
            </div>
            <div className="form-group">
              <label htmlFor="emailConfirm">Repeat Email address</label>
              <input
                id="emailConfirm"
                type="email"
                name="emailConfirm"
                className="form-control"
                placeholder="Repeat Email address"
                value={formData.emailConfirm}
                onChange={handleChange}
                required
                disabled={isLoading}
              />
            </div>
            <div className="form-group">
              <label htmlFor="password">Password</label>
              <input
                id="password"
                type="password"
                name="password"
                className="form-control"
                placeholder="Password"
                value={formData.password}
                onChange={handleChange}
                required
                disabled={isLoading}
              />
            </div>
            <div className="form-group">
              <label htmlFor="passwordConfirm">Repeat Password</label>
              <input
                id="passwordConfirm"
                type="password"
                name="passwordConfirm"
                className="form-control"
                placeholder="Repeat Password"
                value={formData.passwordConfirm}
                onChange={handleChange}
                required
                disabled={isLoading}
              />
            </div>
            <div className="form-group">
              <label htmlFor="countryId">Country</label>
              <select
                id="countryId"
                name="countryId"
                className="form-control"
                value={formData.countryId}
                onChange={handleChange}
                required
                disabled={isLoading}
              >
                <option value="">Select Country</option>
                {countries.map(c => (
                  <option key={c.id} value={c.id}>{c.name}</option>
                ))}
              </select>
            </div>
            <div className="form-group">
                <label htmlFor="serverId">Server</label>
                <select
                  id="serverId"
                  name="serverId"
                  className="form-control"
                  value={formData.serverId}
                  onChange={handleChange}
                  required
                  disabled={isLoading}
                >
                  <option value="">Select Server</option>
                  {servers.map(s => (
                    <option key={s.id} value={s.id}>{s.name}</option>
                  ))}
                </select>
              </div>
            <div className="form-group">
              <label htmlFor="clubName">Club Name</label>
              <input
                id="clubName"
                type="text"
                name="clubName"
                className="form-control"
                placeholder="Club Name"
                value={formData.clubName}
                onChange={handleChange}
                required
                disabled={isLoading}
              />
            </div>
            <button
              className="btn btn-primary"
              style={{ width: '100%' }}
              type="submit"
              disabled={isLoading}
              aria-busy={isLoading}
            >
              {isLoading ? <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span> : null}
              {isLoading ? 'Creating Account...' : 'Create Account'}
            </button>
          </form>
          <hr style={{ border: '0.5px solid rgba(255,255,255,0.1)', margin: '20px 0' }} />
          <div style={{ textAlign: 'center' }}>
            <Link className="small" to="/login" style={{ color: 'var(--accent-color)' }}>Already Registered?</Link>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Register;
