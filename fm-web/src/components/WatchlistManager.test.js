import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import WatchlistManager from './WatchlistManager';
import { getClubWatchlist, getWatchlistNotifications, getWatchlistStats, removeFromWatchlist, markNotificationAsRead } from '../services/api';

// Mock the API module
jest.mock('../services/api', () => ({
    getClubWatchlist: jest.fn(),
    addPlayerToWatchlist: jest.fn(),
    removeFromWatchlist: jest.fn(),
    getWatchlistNotifications: jest.fn(),
    getWatchlistStats: jest.fn(),
    markNotificationAsRead: jest.fn()
}));

const mockWatchlist = {
    totalEntries: 2,
    maxEntries: 10,
    entries: [
        {
            id: 1,
            player: { id: 101, name: 'John', surname: 'Doe', role: 'Striker', team: { club: { name: 'FC Test' } }, age: 25 },
            priority: 'HIGH',
            category: 'TARGET',
            addedValue: 1000000,
            currentValue: 1200000,
            addedRating: 7.5,
            currentRating: 7.8,
            addedDate: '2023-01-01',
            notifyOnPerformance: true,
            totalNotifications: 2
        },
        {
            id: 2,
            player: { id: 102, name: 'Jane', surname: 'Smith', role: 'Midfielder', team: { club: { name: 'FC Test 2' } }, age: 22 },
            priority: 'MEDIUM',
            category: 'FUTURE',
            addedValue: 500000,
            currentValue: 550000,
            addedRating: 6.5,
            currentRating: 6.8,
            addedDate: '2023-02-01',
            totalNotifications: 0
        }
    ]
};

const mockNotifications = [
    {
        id: 1,
        type: 'PERFORMANCE',
        title: 'Great Goal',
        message: 'Scored a hat-trick',
        isRead: false,
        isImportant: true,
        createdDate: '2023-03-01'
    }
];

const mockStats = {
    totalPlayers: 2,
    availablePlayers: 1,
    totalValue: 1750000,
    averageValue: 875000,
    priceIncreased: 2,
    priceDecreased: 0
};

describe('WatchlistManager', () => {
    beforeEach(() => {
        jest.clearAllMocks();
        getClubWatchlist.mockResolvedValue({ data: mockWatchlist });
        getWatchlistNotifications.mockResolvedValue({ data: mockNotifications });
        getWatchlistStats.mockResolvedValue({ data: mockStats });
    });

    test('renders loading state initially', async () => {
        // We defer resolution to check loading state
        let resolvePromise;
        const promise = new Promise(resolve => { resolvePromise = resolve; });
        getClubWatchlist.mockReturnValue(promise);

        render(<WatchlistManager clubId={1} />);
        expect(screen.getByRole('status')).toBeInTheDocument();

        resolvePromise({ data: mockWatchlist });
    });

    test('renders watchlist data correctly', async () => {
        render(<WatchlistManager clubId={1} />);

        // Wait for title to appear, which means loading is done
        await screen.findByText('Transfer Watchlist');

        expect(screen.queryByRole('status')).not.toBeInTheDocument();
        expect(screen.getByText('John Doe')).toBeInTheDocument();
        expect(screen.getByText('Jane Smith')).toBeInTheDocument();
    });

    test('renders tabs with correct ARIA attributes', async () => {
        render(<WatchlistManager clubId={1} />);
        await screen.findByText('Transfer Watchlist');

        const tabList = screen.getByRole('tablist');
        expect(tabList).toBeInTheDocument();

        const playersTab = screen.getByRole('tab', { name: /Players/ });
        expect(playersTab).toHaveAttribute('aria-selected', 'true');
        expect(playersTab).toHaveAttribute('aria-controls', 'players-panel');

        const notificationsTab = screen.getByRole('tab', { name: /Notifications/ });
        expect(notificationsTab).toHaveAttribute('aria-selected', 'false');

        const statsTab = screen.getByRole('tab', { name: /Statistics/ });
        expect(statsTab).toHaveAttribute('aria-selected', 'false');
    });

    test('switches tabs and content correctly', async () => {
        render(<WatchlistManager clubId={1} />);
        await screen.findByText('Transfer Watchlist');

        const notificationsTab = screen.getByRole('tab', { name: /Notifications/ });
        fireEvent.click(notificationsTab);

        expect(notificationsTab).toHaveAttribute('aria-selected', 'true');
        expect(screen.getByRole('tabpanel', { name: /Notifications/ })).toBeInTheDocument();
        expect(screen.getByText('Great Goal')).toBeInTheDocument();
    });

    test('displays empty state for players', async () => {
        getClubWatchlist.mockResolvedValue({ data: { entries: [], totalEntries: 0 } });
        render(<WatchlistManager clubId={1} />);
        await screen.findByText('Transfer Watchlist');

        expect(screen.getByText('No players in watchlist')).toBeInTheDocument();
        expect(screen.queryByText('John Doe')).not.toBeInTheDocument();
    });

    test('displays empty state for notifications', async () => {
        getWatchlistNotifications.mockResolvedValue({ data: [] });
        render(<WatchlistManager clubId={1} />);
        await screen.findByText('Transfer Watchlist');

        const notificationsTab = screen.getByRole('tab', { name: /Notifications/ });
        fireEvent.click(notificationsTab);

        expect(screen.getByText('No notifications')).toBeInTheDocument();
        expect(screen.queryByText('Great Goal')).not.toBeInTheDocument();
    });
});
