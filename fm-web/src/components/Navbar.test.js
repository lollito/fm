import React from 'react';
import { render, screen, fireEvent, act } from '@testing-library/react';
import Navbar from './Navbar';
import { AuthContext } from '../context/AuthContext';
import { BrowserRouter } from 'react-router-dom';
import * as api from '../services/api';
import { Client } from '@stomp/stompjs';
import '@testing-library/jest-dom';

// Mock dependencies
jest.mock('../services/api');
jest.mock('sockjs-client', () => {
    return jest.fn().mockImplementation(() => ({}));
});
jest.mock('@stomp/stompjs');

const mockUser = {
  username: 'TestManager',
  level: 5,
  club: {
    finance: {
      balance: 1500000
    }
  }
};

const mockLogout = jest.fn();

describe('Navbar Accessibility', () => {
  beforeEach(() => {
    // Setup API mocks
    api.getUnreadNotifications.mockResolvedValue({ data: [] });
    api.getManagerProfile.mockResolvedValue({ data: { level: 5, currentXp: 500, xpForNextLevel: 1000 } });

    // Setup Stomp mock
    Client.mockImplementation(() => ({
      activate: jest.fn(),
      deactivate: jest.fn(),
      subscribe: jest.fn()
    }));
  });

  const renderNavbar = async () => {
    await act(async () => {
      render(
        <AuthContext.Provider value={{ user: mockUser, logout: mockLogout }}>
          <BrowserRouter>
            <Navbar onToggleMenu={jest.fn()} />
          </BrowserRouter>
        </AuthContext.Provider>
      );
    });
  };

  test('Menu button has aria-label', async () => {
    await renderNavbar();
    const menuButton = screen.getByRole('button', { name: /toggle menu/i });
    expect(menuButton).toBeInTheDocument();
  });

  test('Search input has aria-label', async () => {
    await renderNavbar();
    const searchInput = screen.getByRole('textbox', { name: /search/i });
    expect(searchInput).toBeInTheDocument();
  });

  test('Notification button has aria-label', async () => {
    await renderNavbar();
    const notificationButton = screen.getByRole('button', { name: /notifications/i });
    expect(notificationButton).toBeInTheDocument();
  });

  test('Level progress bar has role and aria attributes', async () => {
    await renderNavbar();
    const progressbar = await screen.findByRole('progressbar');
    expect(progressbar).toBeInTheDocument();
    expect(progressbar).toHaveAttribute('aria-label', 'Level Progress');
    expect(progressbar).toHaveAttribute('aria-valuenow', '50'); // 500 / 1000 * 100
    expect(progressbar).toHaveAttribute('aria-valuemin', '0');
    expect(progressbar).toHaveAttribute('aria-valuemax', '100');
  });

  test('User profile trigger is keyboard accessible', async () => {
    await renderNavbar();
    const userTrigger = screen.getByText('TestManager').closest('.user-info');
    expect(userTrigger).toHaveAttribute('role', 'button');
    expect(userTrigger).toHaveAttribute('tabIndex', '0');
    expect(userTrigger).toHaveAttribute('aria-haspopup', 'true');

    // Test keyboard interaction
    await act(async () => {
        fireEvent.keyDown(userTrigger, { key: 'Enter', code: 'Enter' });
    });
    expect(screen.getByText('Logout')).toBeInTheDocument();
  });
});
