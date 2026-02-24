
import React from 'react';
import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import Register from './Register';
import { AuthContext } from '../context/AuthContext';
import { ToastContext } from '../context/ToastContext';

// Mock dependencies
const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockNavigate,
  Link: ({ children, to }) => <a href={to}>{children}</a>,
}));

jest.mock('../services/api', () => {
  const mockGet = jest.fn((url) => {
    if (url === '/country/') {
      return Promise.resolve({ data: [{ id: 1, name: 'Country 1' }] });
    }
    return Promise.resolve({ data: [] });
  });

  return {
    __esModule: true,
    default: {
      get: mockGet,
      post: jest.fn(),
    },
    getServers: jest.fn().mockResolvedValue({ data: [{ id: 1, name: 'Server 1' }] }),
  };
});

// Mock ToastContext
jest.mock('../context/ToastContext', () => ({
  useToast: () => ({ showToast: jest.fn() }),
}));

const mockLogin = jest.fn();

describe('Register Component', () => {
  test('renders Register component correctly', async () => {
    render(
      <AuthContext.Provider value={{ login: mockLogin }}>
        <Register />
      </AuthContext.Provider>
    );

    expect(screen.getByText('FM Register')).toBeInTheDocument();
    expect(screen.getByLabelText('Username')).toBeInTheDocument(); // usage of getByLabelText verifies accessibility
    expect(screen.getByLabelText('Email address')).toBeInTheDocument();
    expect(screen.getByText('Create Account')).toBeInTheDocument();
  });
});
