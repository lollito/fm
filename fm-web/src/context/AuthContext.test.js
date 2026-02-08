import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import { AuthContext, AuthProvider } from './AuthContext';
import api from '../services/api';

jest.mock('../services/api', () => ({
  post: jest.fn(),
  create: jest.fn(() => ({
    get: jest.fn(),
    post: jest.fn(),
  })),
}));

// Mock localStorage
const localStorageMock = {
  getItem: jest.fn(),
  setItem: jest.fn(),
  removeItem: jest.fn(),
  clear: jest.fn(),
};

Object.defineProperty(window, 'localStorage', {
  value: localStorageMock
});

const TestConsumer = () => {
  const { user, login, logout } = React.useContext(AuthContext);

  return (
    <div>
      <div data-testid="user-display">{user ? user.username : 'No User'}</div>
      <button onClick={() => login({ username: 'testuser', token: '123' })}>
        Login
      </button>
      <button onClick={logout}>Logout</button>
    </div>
  );
};

describe('AuthContext', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('provides initial state', () => {
    localStorage.getItem.mockReturnValueOnce(null);
    render(
      <AuthProvider>
        <TestConsumer />
      </AuthProvider>
    );

    expect(screen.getByTestId('user-display')).toHaveTextContent('No User');
    expect(localStorage.getItem).toHaveBeenCalledWith('user');
  });

  test('login updates user and localStorage', async () => {
    localStorage.getItem.mockReturnValueOnce(null);
    render(
      <AuthProvider>
        <TestConsumer />
      </AuthProvider>
    );

    fireEvent.click(screen.getByText('Login'));

    await waitFor(() => {
      expect(screen.getByTestId('user-display')).toHaveTextContent('testuser');
    });
    expect(localStorage.setItem).toHaveBeenCalledWith(
      'user',
      JSON.stringify({ username: 'testuser', token: '123' })
    );
  });

  test('logout clears user and localStorage', async () => {
    // Setup initial state via getItem mock
    const user = { username: 'existing', token: 'abc' };
    localStorage.getItem.mockReturnValueOnce(JSON.stringify(user));

    render(
      <AuthProvider>
        <TestConsumer />
      </AuthProvider>
    );

    // Verify initial load
    await waitFor(() => {
      expect(screen.getByTestId('user-display')).toHaveTextContent('existing');
    });

    // Perform logout
    fireEvent.click(screen.getByText('Logout'));

    await waitFor(() => {
      expect(screen.getByTestId('user-display')).toHaveTextContent('No User');
    });
    expect(localStorage.removeItem).toHaveBeenCalledWith('user');
  });

  test('initializes from localStorage', async () => {
    const storedUser = { username: 'stored', token: 'xyz' };
    localStorage.getItem.mockReturnValueOnce(JSON.stringify(storedUser));

    render(
      <AuthProvider>
        <TestConsumer />
      </AuthProvider>
    );

    await waitFor(() => {
      expect(screen.getByTestId('user-display')).toHaveTextContent('stored');
    });
    expect(localStorage.getItem).toHaveBeenCalledWith('user');
  });
});
