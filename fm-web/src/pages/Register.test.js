import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import Register from './Register';
import { AuthContext } from '../context/AuthContext';
import api from '../services/api';
import * as ToastContext from '../context/ToastContext';

// Mock the API
jest.mock('../services/api', () => ({
  __esModule: true,
  default: {
    get: jest.fn(),
    post: jest.fn(),
  },
  getServers: jest.fn(),
}));

// Mock react-router-dom
const mockedNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  useNavigate: () => mockedNavigate,
  Link: ({ children, to }) => <a href={to}>{children}</a>,
}));

// Mock ToastContext
const mockShowToast = jest.fn();
jest.mock('../context/ToastContext', () => ({
  useToast: () => ({ showToast: mockShowToast }),
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
    // Default mocks
    api.get.mockResolvedValue({ data: [{ id: 1, name: 'Test Country' }] });
    // getServers is a named export, mocked above
    require('../services/api').getServers.mockResolvedValue({ data: [{ id: 1, name: 'Test Server' }] });
  });

  test('renders registration form', async () => {
    renderComponent();

    // Wait for async effects (fetching countries/servers)
    await waitFor(() => {
      expect(screen.getByText('FM Register')).toBeInTheDocument();
    });

    expect(screen.getByPlaceholderText('Username')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Email address')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Password')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Club Name')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /create account/i })).toBeInTheDocument();
  });

  test('displays loading state during submission', async () => {
    // Delay the API response to simulate loading
    api.post.mockImplementation(() => new Promise(resolve => setTimeout(() => resolve({ data: { token: 'token' } }), 100)));

    renderComponent();

    // Fill the form
    fireEvent.change(screen.getByPlaceholderText('Username'), { target: { value: 'testuser' } });
    fireEvent.change(screen.getByPlaceholderText('Email address'), { target: { value: 'test@example.com' } });
    fireEvent.change(screen.getByPlaceholderText('Repeat Email address'), { target: { value: 'test@example.com' } });
    fireEvent.change(screen.getByPlaceholderText('Password'), { target: { value: 'password123' } });
    fireEvent.change(screen.getByPlaceholderText('Repeat Password'), { target: { value: 'password123' } });
    fireEvent.change(screen.getByPlaceholderText('Club Name'), { target: { value: 'Test Club' } });

    // Wait for selects to be populated
    await waitFor(() => {
        fireEvent.change(screen.getByRole('combobox', { name: /country/i }), { target: { value: '1' } });
        fireEvent.change(screen.getByRole('combobox', { name: /server/i }), { target: { value: '1' } });
    });

    const submitButton = screen.getByRole('button', { name: /create account/i });
    fireEvent.click(submitButton);

    // Expect loading state
    expect(submitButton).toBeDisabled();
    // The text should change to "Creating Account..."
    expect(screen.getByText(/creating account/i)).toBeInTheDocument();

    // Wait for completion
    await waitFor(() => {
      expect(mockedNavigate).toHaveBeenCalledWith('/');
    });
  });

  test('handles submission error and resets loading state', async () => {
    api.post.mockRejectedValue(new Error('Failed'));

    renderComponent();

    // Fill the form
    fireEvent.change(screen.getByPlaceholderText('Username'), { target: { value: 'testuser' } });
    fireEvent.change(screen.getByPlaceholderText('Email address'), { target: { value: 'test@example.com' } });
    fireEvent.change(screen.getByPlaceholderText('Repeat Email address'), { target: { value: 'test@example.com' } });
    fireEvent.change(screen.getByPlaceholderText('Password'), { target: { value: 'password123' } });
    fireEvent.change(screen.getByPlaceholderText('Repeat Password'), { target: { value: 'password123' } });
    fireEvent.change(screen.getByPlaceholderText('Club Name'), { target: { value: 'Test Club' } });

     // Wait for selects to be populated
     await waitFor(() => {
        fireEvent.change(screen.getByRole('combobox', { name: /country/i }), { target: { value: '1' } });
        fireEvent.change(screen.getByRole('combobox', { name: /server/i }), { target: { value: '1' } });
    });

    const submitButton = screen.getByRole('button', { name: /create account/i });
    fireEvent.click(submitButton);

    // Expect error toast
    await waitFor(() => {
      expect(mockShowToast).toHaveBeenCalledWith('Registration failed', 'error');
    });

    // Expect loading state reset
    expect(submitButton).not.toBeDisabled();
    expect(screen.getByText('Create Account')).toBeInTheDocument();
  });
});
