import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import LiveMatchViewer from './LiveMatchViewer';
import { getLiveMatch, joinLiveMatch, leaveLiveMatch } from '../services/api';

// Mock dependencies
jest.mock('react-router-dom', () => ({
  useParams: () => ({ matchId: '1' }),
}));

jest.mock('../services/api', () => ({
  getLiveMatch: jest.fn(),
  joinLiveMatch: jest.fn(),
  leaveLiveMatch: jest.fn(),
  API_BASE_URL: 'http://localhost:8080'
}));

jest.mock('./Layout', () => ({ children }) => <div>{children}</div>);

// Mock SockJS and Stomp
jest.mock('sockjs-client', () => {
    return jest.fn(() => ({
        close: jest.fn()
    }));
});

// Mock @stomp/stompjs
// Using a class for the mock to handle new Client()
jest.mock('@stomp/stompjs', () => {
    return {
        __esModule: true,
        Client: class {
            constructor() {
                this.activate = jest.fn();
                this.deactivate = jest.fn();
                this.subscribe = jest.fn();
                this.webSocketFactory = () => {};
            }
        }
    };
});

// Mock scrollIntoView
window.HTMLElement.prototype.scrollIntoView = jest.fn();

describe('LiveMatchViewer', () => {
  const mockMatchData = {
    match: {
      id: 1,
      home: { name: 'Home FC' },
      away: { name: 'Away United' }
    },
    homeScore: 2,
    awayScore: 1,
    currentMinute: 75,
    additionalTime: 3,
    currentPhase: 'SECOND_HALF',
    weatherConditions: 'Rainy',
    temperature: 15,
    intensity: 'HIGH',
    spectatorCount: 50000,
    events: [
      {
        id: 101,
        minute: 10,
        additionalTime: 0,
        eventType: 'GOAL',
        description: 'Goal by Player 1!',
        severity: 'NORMAL',
        homeScore: 1,
        awayScore: 0,
        isKeyEvent: true
      },
      {
        id: 102,
        minute: 45,
        additionalTime: 2,
        eventType: 'HALF_TIME',
        description: 'Half Time',
        severity: 'NORMAL',
        homeScore: 1,
        awayScore: 0,
        isKeyEvent: false
      }
    ]
  };

  beforeEach(() => {
    jest.clearAllMocks();
    getLiveMatch.mockResolvedValue({ data: mockMatchData });
    joinLiveMatch.mockResolvedValue({});
    leaveLiveMatch.mockResolvedValue({});
  });

  test('renders match details and events', async () => {
    render(<LiveMatchViewer />);

    expect(screen.getByText('Loading live match...')).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.queryByText('Loading live match...')).not.toBeInTheDocument();
    });

    // Check Match Header
    expect(screen.getByText('Home FC')).toBeInTheDocument();
    expect(screen.getByText('Away United')).toBeInTheDocument();
    expect(screen.getByText("75+3'")).toBeInTheDocument();
    expect(screen.getByText('2nd Half')).toBeInTheDocument();

    // Check Match Events
    expect(screen.getByText('Goal by Player 1!')).toBeInTheDocument();
    expect(screen.getByText('Half Time')).toBeInTheDocument();
  });
});
