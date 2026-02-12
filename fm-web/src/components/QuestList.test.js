import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import QuestList from './QuestList';
import { getQuests, claimQuest } from '../services/api';
import '@testing-library/jest-dom';

// Mock the API module
jest.mock('../services/api', () => ({
  getQuests: jest.fn(),
  claimQuest: jest.fn()
}));

describe('QuestList', () => {
  const mockQuests = [
    {
      id: 1,
      description: 'Score 5 goals',
      status: 'ACTIVE',
      currentValue: 2,
      targetValue: 5,
      rewardXp: 50,
      frequency: 'DAILY'
    },
    {
      id: 2,
      description: 'Win 3 matches',
      status: 'COMPLETED',
      currentValue: 3,
      targetValue: 3,
      rewardXp: 100,
      frequency: 'DAILY'
    }
  ];

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('renders quests and accessibility attributes correctly', async () => {
    getQuests.mockResolvedValue({ data: mockQuests });

    render(<QuestList />);

    // Wait for quests to load
    await waitFor(() => {
      expect(screen.getByText('Score 5 goals')).toBeInTheDocument();
    });

    // Check for progress bar aria-label
    const progressBar1 = screen.getByRole('progressbar', { name: 'Progress for Score 5 goals' });
    expect(progressBar1).toBeInTheDocument();
    expect(progressBar1).toHaveAttribute('aria-valuenow', '40'); // (2/5)*100

    const progressBar2 = screen.getByRole('progressbar', { name: 'Progress for Win 3 matches' });
    expect(progressBar2).toBeInTheDocument();
    expect(progressBar2).toHaveAttribute('aria-valuenow', '100');

    // Check for Claim Reward button and its aria-label
    const claimButton = screen.getByRole('button', { name: 'Claim reward for Win 3 matches' });
    expect(claimButton).toBeInTheDocument();
    expect(claimButton).toHaveTextContent('Claim Reward');
  });

  test('shows loading spinner when claiming reward', async () => {
    getQuests.mockResolvedValue({ data: mockQuests });
    claimQuest.mockImplementation(() => new Promise(resolve => setTimeout(resolve, 100))); // Simulate delay

    render(<QuestList />);

    await waitFor(() => {
      expect(screen.getByText('Win 3 matches')).toBeInTheDocument();
    });

    const claimButton = screen.getByRole('button', { name: 'Claim reward for Win 3 matches' });

    // Click claim
    fireEvent.click(claimButton);

    // Should show loading state
    expect(claimButton).toBeDisabled();
    expect(claimButton).toHaveTextContent('Claiming...');

    // Check for spinner element
    // Note: react-testing-library queries return HTMLElements, so we can use querySelector
    // But safely, we can just assert on text content as above.
    // To be extra sure about the spinner class:
    // Since the button contains spans, text content is aggregated.

    expect(claimQuest).toHaveBeenCalledWith(2);

    // Wait for finish
    await waitFor(() => {
        // After claiming, it fetches quests again.
        // Since we mocked fetchQuests to return the same thing, it will re-render.
        // The claiming state should be reset.
        expect(claimButton).not.toBeDisabled();
        expect(claimButton).toHaveTextContent('Claim Reward');
    });
  });
});
