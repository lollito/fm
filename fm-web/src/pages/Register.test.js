/**
 * @jest-environment jsdom
 */
import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import Register from './Register';
import { AuthContext } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import api from '../services/api';

// Mock the API
jest.mock('../services/api', () => ({
  __esModule: true,
  default: {
    get: jest.fn(),
    post: jest.fn(),
  },
  getServers: jest.fn(),
}));

// Mock react-router-dom completely
const mockedNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  useNavigate: () => mockedNavigate,
  Link: ({ children, to }) => <a href={to}>{children}</a>,
  BrowserRouter: ({ children }) => <div>{children}</div>
}));

// Mock useToast
const mockShowToast = jest.fn();
jest.mock('../context/ToastContext', () => ({
  useToast: () => ({ showToast: mockShowToast })
}));

describe('Register Component', () => {
  const login = jest.fn();

  const renderComponent = () => {
    render(
      <AuthContext.Provider value={{ login }}>
        <Register />
      </AuthContext.Provider>
    );
  };

  beforeEach(() => {
    jest.clearAllMocks();
    api.get.mockResolvedValue({ data: [{ id: 1, name: 'Country A' }] });
    require('../services/api').getServers.mockResolvedValue({ data: [{ id: 1, name: 'Server 1' }] });
  });

  test('renders registration form with accessible inputs', async () => {
    renderComponent();

    // Wait for countries and servers to load
    await waitFor(() => expect(api.get).toHaveBeenCalledWith('/country/'));

    // Check for accessible labels
    expect(screen.getByLabelText('Username')).toBeInTheDocument();
    expect(screen.getByLabelText('Email address')).toBeInTheDocument();
    expect(screen.getByLabelText('Repeat Email address')).toBeInTheDocument();
    expect(screen.getByLabelText('Password')).toBeInTheDocument();
    expect(screen.getByLabelText('Repeat Password')).toBeInTheDocument();
    expect(screen.getByLabelText('Country')).toBeInTheDocument();
    expect(screen.getByLabelText('Server')).toBeInTheDocument();
    expect(screen.getByLabelText('Club Name')).toBeInTheDocument();

    expect(screen.getByRole('button', { name: /create account/i })).toBeInTheDocument();
  });

  test('displays loading state during submission', async () => {
    // Mock API to delay response
    api.post.mockImplementation(() => new Promise(resolve => setTimeout(() => resolve({ data: { token: 'token' } }), 100)));
    renderComponent();

    // Fill form
    fireEvent.change(screen.getByLabelText('Username'), { target: { value: 'user' } });
    fireEvent.change(screen.getByLabelText('Email address'), { target: { value: 'test@example.com' } });
    fireEvent.change(screen.getByLabelText('Repeat Email address'), { target: { value: 'test@example.com' } });
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'password' } });
    fireEvent.change(screen.getByLabelText('Repeat Password'), { target: { value: 'password' } });
    fireEvent.change(screen.getByLabelText('Club Name'), { target: { value: 'Club FC' } });

    // Submit
    fireEvent.click(screen.getByRole('button', { name: /create account/i }));

    // Check loading state
    expect(screen.getByRole('button')).toBeDisabled();
    expect(screen.getByText('Creating Account...')).toBeInTheDocument();
    expect(screen.getByRole('button')).toHaveAttribute('aria-busy', 'true');

    // Wait for finish
    await waitFor(() => {
      expect(screen.getByRole('button')).not.toBeDisabled();
      expect(screen.getByText('Create Account')).toBeInTheDocument();
      expect(screen.getByRole('button')).toHaveAttribute('aria-busy', 'false');
    });
  });
});
