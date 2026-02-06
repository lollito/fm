/**
 * @jest-environment jsdom
 */
import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import PlayerHistory from './PlayerHistory';
import * as api from '../services/api';

// Mock the API service
jest.mock('../services/api');

// Mock Recharts
jest.mock('recharts', () => ({
    ResponsiveContainer: ({ children }) => <div className="recharts-responsive-container">{children}</div>,
    LineChart: ({ children }) => <div className="recharts-line-chart">{children}</div>,
    Line: () => <div className="recharts-line" />,
    BarChart: ({ children }) => <div className="recharts-bar-chart">{children}</div>,
    Bar: () => <div className="recharts-bar" />,
    XAxis: () => <div className="recharts-x-axis" />,
    YAxis: () => <div className="recharts-y-axis" />,
    CartesianGrid: () => <div className="recharts-cartesian-grid" />,
    Tooltip: () => <div className="recharts-tooltip" />,
    Legend: () => <div className="recharts-legend" />
}));

describe('PlayerHistory Component', () => {
    test('renders loading state initially', () => {
        render(<PlayerHistory playerId={1} />);
        expect(screen.getByText('Loading player history...')).toBeInTheDocument();
    });

    test('renders player data correctly', async () => {
        const mockData = {
            player: {
                name: 'John',
                surname: 'Doe',
                age: 25,
                role: 'STRIKER'
            },
            careerStats: {
                totalMatchesPlayed: 100,
                totalGoals: 50,
                totalAssists: 20,
                clubsPlayed: 3,
                leagueTitles: 1,
                highestTransferValue: 1000000
            },
            seasonStats: [],
            achievements: [],
            transferHistory: []
        };

        api.getPlayerHistory.mockResolvedValue({ data: mockData });

        render(<PlayerHistory playerId={1} />);

        await waitFor(() => {
            // Use regex for looser matching or exact string if confident
            expect(screen.getByText('John Doe')).toBeInTheDocument();
        });
    });

    test('handles error state gracefully', async () => {
        api.getPlayerHistory.mockRejectedValue(new Error('Failed to fetch'));

        render(<PlayerHistory playerId={1} />);

        await waitFor(() => {
             expect(screen.getByText('Player history not found')).toBeInTheDocument();
        });
    });
});
