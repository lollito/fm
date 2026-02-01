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
    <div className="container">
      <div className="row">
        <div className="col-sm-9 col-md-7 col-lg-5 mx-auto">
          <div className="card card-signin my-5">
            <div className="card-body">
              <h5 className="card-title text-center">FM - A Football Manager Game</h5>
              <form className="form-signup" onSubmit={handleSubmit}>
                <div className="form-label-group">
                  <input
                    type="text"
                    name="username"
                    className="form-control"
                    placeholder="Username"
                    value={formData.username}
                    onChange={handleChange}
                    required
                    autoFocus
                  />
                  <label>Username</label>
                </div>
                <div className="form-label-group">
                  <input
                    type="email"
                    name="email"
                    className="form-control"
                    placeholder="Email address"
                    value={formData.email}
                    onChange={handleChange}
                    required
                  />
                  <label>Email address</label>
                </div>
                <div className="form-label-group">
                  <input
                    type="email"
                    name="emailConfirm"
                    className="form-control"
                    placeholder="Repeat Email address"
                    value={formData.emailConfirm}
                    onChange={handleChange}
                    required
                  />
                  <label>Repeat Email address</label>
                </div>
                <div className="form-label-group">
                  <input
                    type="password"
                    name="password"
                    className="form-control"
                    placeholder="Password"
                    value={formData.password}
                    onChange={handleChange}
                    required
                  />
                  <label>Password</label>
                </div>
                <div className="form-label-group">
                  <input
                    type="password"
                    name="passwordConfirm"
                    className="form-control"
                    placeholder="Repeat Password"
                    value={formData.passwordConfirm}
                    onChange={handleChange}
                    required
                  />
                  <label>Repeat Password</label>
                </div>
                <div className="form-group">
                  <select
                    name="countryId"
                    className="form-control"
                    value={formData.countryId}
                    onChange={handleChange}
                    required
                  >
                    <option value="">Country</option>
                    {countries.map(c => (
                      <option key={c.id} value={c.id}>{c.name}</option>
                    ))}
                  </select>
                </div>
                <div className="form-label-group">
                  <input
                    type="text"
                    name="clubName"
                    className="form-control"
                    placeholder="Club Name"
                    value={formData.clubName}
                    onChange={handleChange}
                    required
                  />
                  <label>Club Name</label>
                </div>
                <button className="btn btn-lg btn-primary btn-block text-uppercase" type="submit">Create Account</button>
              </form>
              <hr />
              <div className="text-center">
                <Link className="small" to="/login">Already Registered?</Link>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Register;
