import React, { useContext } from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import { AuthContext, AuthProvider } from './AuthContext';

// Mock localStorage
const localStorageMock = (function() {
  let store = {};
  return {
    getItem: function(key) {
      return store[key] || null;
    },
    setItem: function(key, value) {
      store[key] = value.toString();
    },
    removeItem: function(key) {
      delete store[key];
    },
    clear: function() {
      store = {};
    }
  };
})();

Object.defineProperty(window, 'localStorage', {
  value: localStorageMock
});

// Test component to consume context
const TestComponent = () => {
  const { user, login, logout } = useContext(AuthContext);
  return (
    <div>
      <div data-testid="user-value">{user ? JSON.stringify(user) : 'null'}</div>
      <button onClick={() => login({ name: 'Test User' })}>Login</button>
      <button onClick={logout}>Logout</button>
    </div>
  );
};

describe('AuthContext', () => {
  beforeEach(() => {
    window.localStorage.clear();
  });

  test('initializes with null user if localStorage is empty', () => {
    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );
    expect(screen.getByTestId('user-value')).toHaveTextContent('null');
  });

  test('initializes with user from localStorage', () => {
    window.localStorage.setItem('user', JSON.stringify({ name: 'Stored User' }));
    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );
    expect(screen.getByTestId('user-value')).toHaveTextContent('{"name":"Stored User"}');
  });

  test('login updates user and localStorage', () => {
    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );

    const loginButton = screen.getByText('Login');

    fireEvent.click(loginButton);

    expect(screen.getByTestId('user-value')).toHaveTextContent('{"name":"Test User"}');
    expect(window.localStorage.getItem('user')).toBe('{"name":"Test User"}');
  });

  test('logout clears user and localStorage', () => {
    window.localStorage.setItem('user', JSON.stringify({ name: 'Stored User' }));
    render(
      <AuthProvider>
        <TestComponent />
      </AuthProvider>
    );

    expect(screen.getByTestId('user-value')).toHaveTextContent('{"name":"Stored User"}');

    const logoutButton = screen.getByText('Logout');

    fireEvent.click(logoutButton);

    expect(screen.getByTestId('user-value')).toHaveTextContent('null');
    expect(window.localStorage.getItem('user')).toBeNull();
  });
});
