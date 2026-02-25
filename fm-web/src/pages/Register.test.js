/** @jest-environment jsdom */
import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import Register from './Register';
import { AuthContext } from '../context/AuthContext';
import api, { getServers } from '../services/api';

// Mock the API
jest.mock('../services/api', () => {
    return {
        __esModule: true,
        default: {
            get: jest.fn(),
            post: jest.fn(),
        },
        getServers: jest.fn()
    };
});

// Mock react-router-dom
const mockedNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  useNavigate: () => mockedNavigate,
  Link: ({ children, to }) => <a href={to}>{children}</a>,
  BrowserRouter: ({ children }) => <div>{children}</div>
}));

// Mock ToastContext
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
    api.get.mockResolvedValue({ data: [{ id: 1, name: 'Country A' }] });
    getServers.mockResolvedValue({ data: [{ id: 1, name: 'Server A' }] });
  });

  test('renders register form with inputs and button', async () => {
    renderComponent();
    expect(screen.getByText('FM Register')).toBeInTheDocument();

    // Check for accessible labels
    expect(screen.getByLabelText('Username')).toBeInTheDocument();
    expect(screen.getByLabelText('Email address')).toBeInTheDocument();
    expect(screen.getByLabelText('Repeat Email address')).toBeInTheDocument();
    expect(screen.getByLabelText('Password')).toBeInTheDocument();
    expect(screen.getByLabelText('Repeat Password')).toBeInTheDocument();
    expect(screen.getByLabelText('Club Name')).toBeInTheDocument();

    expect(screen.getByRole('button', { name: /create account/i })).toBeInTheDocument();

    // Wait for countries and servers to load
    await waitFor(() => {
        expect(screen.getByLabelText('Country')).toBeInTheDocument();
        expect(screen.getByLabelText('Server')).toBeInTheDocument();
    });
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

    // Wait for async selects to populate
    await waitFor(() => expect(screen.getByLabelText('Country')).not.toBeDisabled());

    fireEvent.change(screen.getByLabelText('Country'), { target: { value: '1' } });
    fireEvent.change(screen.getByLabelText('Server'), { target: { value: '1' } });

    const submitButton = screen.getByRole('button', { name: /create account/i });
    fireEvent.click(submitButton);

    expect(submitButton).toBeDisabled();
    expect(submitButton).toHaveAttribute('aria-busy', 'true');
    expect(screen.getByText('Creating Account...')).toBeInTheDocument();

    await waitFor(() => {
      expect(submitButton).not.toBeDisabled();
      expect(submitButton).toHaveAttribute('aria-busy', 'false');
      expect(screen.getByText('Create Account')).toBeInTheDocument();
    });
  });
});
