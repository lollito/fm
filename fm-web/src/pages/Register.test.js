import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import Register from './Register';
import { AuthContext } from '../context/AuthContext';
import * as ToastContextModule from '../context/ToastContext';
import api, { getServers } from '../services/api';

// Mock the API
jest.mock('../services/api', () => {
  const originalModule = jest.requireActual('../services/api');
  return {
    __esModule: true,
    default: {
        ...originalModule.default,
        get: jest.fn(),
        post: jest.fn(),
    },
    getServers: jest.fn(),
  };
});

// Mock react-router-dom
const mockedNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  useNavigate: () => mockedNavigate,
  Link: ({ children, to }) => <a href={to}>{children}</a>,
}));

// Mock useToast
const mockShowToast = jest.fn();
jest.mock('../context/ToastContext', () => ({
    useToast: () => ({
        showToast: mockShowToast
    })
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
    // Mock initial data fetches
    api.get.mockImplementation((url) => {
        if (url === '/country/') {
            return Promise.resolve({ data: [{ id: 1, name: 'Country A' }] });
        }
        return Promise.resolve({ data: [] });
    });
    getServers.mockResolvedValue({ data: [{ id: 1, name: 'Server A' }] });
  });

  test('renders register form with inputs associated with labels', async () => {
    renderComponent();

    // Wait for initial data load
    await waitFor(() => expect(getServers).toHaveBeenCalled());

    expect(screen.getByText('FM Register')).toBeInTheDocument();

    // These checks verify that labels are correctly associated with inputs
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

  test('handles successful registration', async () => {
    api.post.mockResolvedValueOnce({ data: { token: 'fake-token' } });
    renderComponent();

    // Wait for initial data load
    await waitFor(() => expect(getServers).toHaveBeenCalled());

    fireEvent.change(screen.getByLabelText('Username'), { target: { value: 'newuser' } });
    fireEvent.change(screen.getByLabelText('Email address'), { target: { value: 'test@example.com' } });
    fireEvent.change(screen.getByLabelText('Repeat Email address'), { target: { value: 'test@example.com' } });
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'password' } });
    fireEvent.change(screen.getByLabelText('Repeat Password'), { target: { value: 'password' } });
    fireEvent.change(screen.getByLabelText('Club Name'), { target: { value: 'My Club' } });

    // Select country
    fireEvent.change(screen.getByLabelText('Country'), { target: { value: '1' } });

    fireEvent.click(screen.getByRole('button', { name: /create account/i }));

    await waitFor(() => {
      expect(api.post).toHaveBeenCalledWith('/user/register', expect.objectContaining({
        username: 'newuser',
        email: 'test@example.com',
        clubName: 'My Club',
        countryId: '1',
        serverId: 1 // Default selected
      }));
      expect(login).toHaveBeenCalledWith({ token: 'fake-token' });
      expect(mockedNavigate).toHaveBeenCalledWith('/');
    });
  });

  test('displays loading state during submission', async () => {
    // Mock API to delay response
    api.post.mockImplementation(() => new Promise(resolve => setTimeout(() => resolve({ data: { token: 'token' } }), 100)));
    renderComponent();

    await waitFor(() => expect(getServers).toHaveBeenCalled());

    // Fill minimum required fields
    fireEvent.change(screen.getByLabelText('Username'), { target: { value: 'user' } });
    fireEvent.change(screen.getByLabelText('Email address'), { target: { value: 'e@e.com' } });
    fireEvent.change(screen.getByLabelText('Repeat Email address'), { target: { value: 'e@e.com' } });
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'p' } });
    fireEvent.change(screen.getByLabelText('Repeat Password'), { target: { value: 'p' } });

    fireEvent.click(screen.getByRole('button', { name: /create account/i }));

    // This checks for the NEW behavior
    expect(screen.getByRole('button')).toBeDisabled();
    expect(screen.getByRole('button')).toHaveAttribute('aria-busy', 'true');
    expect(screen.getByText('Creating Account...')).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.getByRole('button')).not.toBeDisabled();
      expect(screen.getByText('Create Account')).toBeInTheDocument();
    });
  });

  test('validates passwords match', async () => {
      renderComponent();
      await waitFor(() => expect(getServers).toHaveBeenCalled());

      fireEvent.change(screen.getByLabelText('Email address'), { target: { value: 'e@e.com' } });
      fireEvent.change(screen.getByLabelText('Repeat Email address'), { target: { value: 'e@e.com' } });
      fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'pass1' } });
      fireEvent.change(screen.getByLabelText('Repeat Password'), { target: { value: 'pass2' } });

      fireEvent.click(screen.getByRole('button', { name: /create account/i }));

      expect(mockShowToast).toHaveBeenCalledWith('Passwords do not match', 'error');
      expect(api.post).not.toHaveBeenCalled();
  });
});
