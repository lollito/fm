import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import Register from './Register';
import { AuthContext } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import api, { getServers } from '../services/api';

// Mock the API
jest.mock('../services/api');

// Mock ToastContext hook
jest.mock('../context/ToastContext');

// Mock react-router-dom completely
const mockedNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  useNavigate: () => mockedNavigate,
  Link: ({ children, to }) => <a href={to}>{children}</a>,
  BrowserRouter: ({ children }) => <div>{children}</div>
}));

describe('Register Component', () => {
  const login = jest.fn();
  const showToast = jest.fn();

  const renderComponent = () => {
    render(
      <AuthContext.Provider value={{ login }}>
          <Register />
      </AuthContext.Provider>
    );
  };

  beforeEach(() => {
    jest.clearAllMocks();
    api.get.mockResolvedValue({ data: [{ id: 1, name: 'Country' }] });
    getServers.mockResolvedValue({ data: [{ id: 1, name: 'Server' }] });
    useToast.mockReturnValue({ showToast });
  });

  test('renders register form with inputs and button', async () => {
    renderComponent();

    // Wait for countries and servers to load
    await waitFor(() => {
      expect(api.get).toHaveBeenCalled();
      expect(getServers).toHaveBeenCalled();
    });

    expect(screen.getByText('FM Register')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Username')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Email address')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Repeat Email address')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Password')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Repeat Password')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Club Name')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /create account/i })).toBeInTheDocument();
  });

  test('displays loading state during submission', async () => {
    // Mock API to delay response
    api.post.mockImplementation(() => new Promise(resolve => setTimeout(() => resolve({ data: { token: 'token' } }), 100)));
    renderComponent();

    // Fill form
    fireEvent.change(screen.getByPlaceholderText('Username'), { target: { value: 'user' } });
    fireEvent.change(screen.getByPlaceholderText('Email address'), { target: { value: 'test@test.com' } });
    fireEvent.change(screen.getByPlaceholderText('Repeat Email address'), { target: { value: 'test@test.com' } });
    fireEvent.change(screen.getByPlaceholderText('Password'), { target: { value: 'password' } });
    fireEvent.change(screen.getByPlaceholderText('Repeat Password'), { target: { value: 'password' } });
    fireEvent.change(screen.getByPlaceholderText('Club Name'), { target: { value: 'My Club' } });

    // Click submit
    fireEvent.click(screen.getByRole('button', { name: /create account/i }));

    // Check loading state
    expect(screen.getByRole('button')).toBeDisabled();
    expect(screen.getByText('Creating Account...')).toBeInTheDocument();

    await waitFor(() => {
      expect(mockedNavigate).toHaveBeenCalledWith('/');
    });
  });

  test('displays error toast on failure', async () => {
    api.post.mockRejectedValue(new Error('Failed'));
    renderComponent();

    fireEvent.change(screen.getByPlaceholderText('Username'), { target: { value: 'user' } });
    fireEvent.change(screen.getByPlaceholderText('Email address'), { target: { value: 'test@test.com' } });
    fireEvent.change(screen.getByPlaceholderText('Repeat Email address'), { target: { value: 'test@test.com' } });
    fireEvent.change(screen.getByPlaceholderText('Password'), { target: { value: 'password' } });
    fireEvent.change(screen.getByPlaceholderText('Repeat Password'), { target: { value: 'password' } });
    fireEvent.change(screen.getByPlaceholderText('Club Name'), { target: { value: 'My Club' } });

    fireEvent.click(screen.getByRole('button', { name: /create account/i }));

    await waitFor(() => {
      expect(showToast).toHaveBeenCalledWith('Registration failed', 'error');
    });
  });
});
