/**
 * @jest-environment jsdom
 */
import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import Register from './Register';
import { AuthContext } from '../context/AuthContext';
import { ToastProvider } from '../context/ToastContext';
import api, { getServers } from '../services/api';

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
  BrowserRouter: ({ children }) => <div>{children}</div>
}));

describe('Register Component', () => {
  const login = jest.fn();

  const renderComponent = () => {
    render(
      <AuthContext.Provider value={{ login }}>
        <ToastProvider>
          <Register />
        </ToastProvider>
      </AuthContext.Provider>
    );
  };

  beforeEach(() => {
    jest.clearAllMocks();
    api.get.mockResolvedValue({ data: [{ id: 1, name: 'Country A' }] });
    getServers.mockResolvedValue({ data: [{ id: 1, name: 'Server A' }] });
  });

  test('renders register form with accessible inputs and button', async () => {
    renderComponent();

    expect(screen.getByText('FM Register')).toBeInTheDocument();

    // Check for inputs using getByLabelText to verify association
    expect(screen.getByLabelText(/Username/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/^Email address/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Repeat Email address/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/^Password/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Repeat Password/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Club Name/i)).toBeInTheDocument();

    // Selects
    expect(screen.getByLabelText(/Country/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Server/i)).toBeInTheDocument();

    expect(screen.getByRole('button', { name: /create account/i })).toBeInTheDocument();

    await waitFor(() => {
        expect(api.get).toHaveBeenCalledWith('/country/');
        expect(getServers).toHaveBeenCalled();
    });
  });

  test('handles successful registration and loading state', async () => {
    // Delay response to check loading state
    api.post.mockImplementation(() => new Promise(resolve => setTimeout(() => resolve({ data: { token: 'fake-token' } }), 100)));
    renderComponent();

    // Fill form
    fireEvent.change(screen.getByLabelText(/Username/i), { target: { value: 'user' } });
    fireEvent.change(screen.getByLabelText(/^Email address/i), { target: { value: 'test@example.com' } });
    fireEvent.change(screen.getByLabelText(/Repeat Email address/i), { target: { value: 'test@example.com' } });
    fireEvent.change(screen.getByLabelText(/^Password/i), { target: { value: 'password' } });
    fireEvent.change(screen.getByLabelText(/Repeat Password/i), { target: { value: 'password' } });
    fireEvent.change(screen.getByLabelText(/Club Name/i), { target: { value: 'My Club' } });

    // Wait for dropdowns to be populated
    await waitFor(() => expect(screen.getByText('Country A')).toBeInTheDocument());

    // Select country
    fireEvent.change(screen.getByLabelText(/Country/i), { target: { value: '1' } });

    // Submit
    const submitButton = screen.getByRole('button', { name: /create account/i });
    fireEvent.click(submitButton);

    // Check loading state
    expect(screen.getByText('Creating Account...')).toBeInTheDocument();
    expect(submitButton).toBeDisabled();
    expect(submitButton).toHaveAttribute('aria-busy', 'true');

    await waitFor(() => {
      expect(api.post).toHaveBeenCalledWith('/user/register', expect.objectContaining({
        username: 'user',
        email: 'test@example.com',
        clubName: 'My Club'
      }));
      expect(login).toHaveBeenCalledWith({ token: 'fake-token' });
      expect(mockedNavigate).toHaveBeenCalledWith('/');
    });
  });
});
