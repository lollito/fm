import React from 'react';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import QuestList from './QuestList';
import * as api from '../../services/api';

// Mock the API module
jest.mock('../../services/api');

const mockQuests = [
  {
    id: 1,
    description: 'Win a match',
    currentValue: 0,
    targetValue: 1,
    status: 'ACTIVE',
    frequency: 'DAILY',
    rewardXp: 100,
    rewardMoney: 10000
  },
  {
    id: 2,
    description: 'Score 5 goals',
    currentValue: 5,
    targetValue: 5,
    status: 'COMPLETED',
    frequency: 'WEEKLY',
    rewardXp: 500,
    rewardMoney: 50000
  }
];

describe('QuestList Component', () => {
  beforeEach(() => {
    api.getQuests.mockResolvedValue({ data: mockQuests });
    api.claimQuest.mockResolvedValue({});
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  test('renders quests grouped by frequency', async () => {
    render(<QuestList />);

    expect(screen.getByText('Loading quests...')).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.queryByText('Loading quests...')).not.toBeInTheDocument();
    });

    expect(screen.getByText('Daily Quests')).toBeInTheDocument();
    expect(screen.getByText('Weekly Quests')).toBeInTheDocument();
    expect(screen.getByText('Win a match')).toBeInTheDocument();
    expect(screen.getByText('Score 5 goals')).toBeInTheDocument();
  });

  test('handles claim reward', async () => {
    render(<QuestList />);

    await waitFor(() => {
      expect(screen.getByText('Claim Reward')).toBeInTheDocument();
    });

    const claimButton = screen.getByText('Claim Reward');
    fireEvent.click(claimButton);

    expect(screen.getByText('Claiming...')).toBeInTheDocument();

    await waitFor(() => {
        expect(api.claimQuest).toHaveBeenCalledWith(2);
    });

    // It refetches quests after claim
    await waitFor(() => {
        expect(api.getQuests).toHaveBeenCalledTimes(2); // Initial + after claim
    });
  });

  test('displays progress correctly', async () => {
      render(<QuestList />);
      await waitFor(() => {
          expect(screen.getByText('0 / 1')).toBeInTheDocument();
          expect(screen.getByText('5 / 5')).toBeInTheDocument();
      });
  });
});
