import React, { useState, useEffect, useContext } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../services/api';
import { AuthContext } from '../context/AuthContext';

const Register = () => {
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    emailConfirm: '',
    password: '',
    passwordConfirm: '',
    countryId: '',
    clubName: ''
  });
  const [countries, setCountries] = useState([]);
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
    fetchCountries();
  }, []);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (formData.email !== formData.emailConfirm) {
      alert('Emails do not match');
      return;
    }
    if (formData.password !== formData.passwordConfirm) {
      alert('Passwords do not match');
      return;
    }
    try {
      const response = await api.post('/user/register', formData);
      login(response.data);
      navigate('/');
    } catch (error) {
      alert('Registration failed');
    }
  };

  return (
    <div className="container" style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', padding: '20px' }}>
      <div className="card" style={{ width: '100%', maxWidth: '500px' }}>
        <div className="card-body">
          <h2 className="card-title text-center" style={{ textAlign: 'center', color: 'var(--accent-color)' }}>FM Register</h2>
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label>Username</label>
              <input
                type="text"
                name="username"
                className="form-control"
                placeholder="Username"
                value={formData.username}
                onChange={handleChange}
                required
              />
            </div>
            <div className="form-group">
              <label>Email address</label>
              <input
                type="email"
                name="email"
                className="form-control"
                placeholder="Email address"
                value={formData.email}
                onChange={handleChange}
                required
              />
            </div>
            <div className="form-group">
              <label>Repeat Email address</label>
              <input
                type="email"
                name="emailConfirm"
                className="form-control"
                placeholder="Repeat Email address"
                value={formData.emailConfirm}
                onChange={handleChange}
                required
              />
            </div>
            <div className="form-group">
              <label>Password</label>
              <input
                type="password"
                name="password"
                className="form-control"
                placeholder="Password"
                value={formData.password}
                onChange={handleChange}
                required
              />
            </div>
            <div className="form-group">
              <label>Repeat Password</label>
              <input
                type="password"
                name="passwordConfirm"
                className="form-control"
                placeholder="Repeat Password"
                value={formData.passwordConfirm}
                onChange={handleChange}
                required
              />
            </div>
            <div className="form-group">
              <label>Country</label>
              <select
                name="countryId"
                className="form-control"
                value={formData.countryId}
                onChange={handleChange}
                required
              >
                <option value="">Select Country</option>
                {countries.map(c => (
                  <option key={c.id} value={c.id}>{c.name}</option>
                ))}
              </select>
            </div>
            <div className="form-group">
              <label>Club Name</label>
              <input
                type="text"
                name="clubName"
                className="form-control"
                placeholder="Club Name"
                value={formData.clubName}
                onChange={handleChange}
                required
              />
            </div>
            <button className="btn btn-primary" style={{ width: '100%' }} type="submit">Create Account</button>
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
