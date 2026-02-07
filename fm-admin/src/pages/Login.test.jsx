/**
 * @jest-environment jsdom
 */
import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import Login from './Login';
import { AuthContext } from '../context/AuthContext';
import api from '../services/api';

// Mock react-router-dom
const mockedNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  useNavigate: () => mockedNavigate,
}));

// Mock api service
jest.mock('../services/api', () => ({
  __esModule: true,
  default: {
    post: jest.fn(),
  },
}));

describe('Login Page', () => {
  const mockLoginContext = jest.fn();

  beforeAll(() => {
    // Mock window.alert
    window.alert = jest.fn();
  });

  beforeEach(() => {
    jest.clearAllMocks();
  });

  const renderComponent = () => {
    render(
      <AuthContext.Provider value={{ login: mockLoginContext }}>
        <Login />
      </AuthContext.Provider>
    );
  };

  test('renders login form correctly', () => {
    renderComponent();
    expect(screen.getByText('Admin Login')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Username')).toBeInTheDocument();
    expect(screen.getByPlaceholderText('Password')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /login/i })).toBeInTheDocument();
  });

  test('successful login', async () => {
    const mockUser = { username: 'admin', accessToken: 'token123' };
    api.post.mockResolvedValue({ data: mockUser });

    renderComponent();

    fireEvent.change(screen.getByPlaceholderText('Username'), { target: { value: 'admin' } });
    fireEvent.change(screen.getByPlaceholderText('Password'), { target: { value: 'password' } });
    fireEvent.click(screen.getByRole('button', { name: /login/i }));

    await waitFor(() => {
      expect(api.post).toHaveBeenCalledWith('/user/login', { username: 'admin', password: 'password' });
    });

    expect(mockLoginContext).toHaveBeenCalledWith(mockUser);
    expect(mockedNavigate).toHaveBeenCalledWith('/');
  });

  test('failed login', async () => {
    api.post.mockRejectedValue(new Error('Login failed'));

    renderComponent();

    fireEvent.change(screen.getByPlaceholderText('Username'), { target: { value: 'wrong' } });
    fireEvent.change(screen.getByPlaceholderText('Password'), { target: { value: 'wrong' } });
    fireEvent.click(screen.getByRole('button', { name: /login/i }));

    await waitFor(() => {
      expect(api.post).toHaveBeenCalled();
    });

    expect(window.alert).toHaveBeenCalledWith('Login failed');
    expect(mockLoginContext).not.toHaveBeenCalled();
    expect(mockedNavigate).not.toHaveBeenCalled();
  });
});
