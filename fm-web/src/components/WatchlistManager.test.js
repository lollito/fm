import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import WatchlistManager from './WatchlistManager';
import * as api from '../services/api';

jest.mock('../services/api');

describe('WatchlistManager Component Accessibility', () => {
    const mockWatchlist = {
        totalEntries: 2,
        maxEntries: 10,
        entries: [
            {
                id: 1,
                player: {
                    name: 'John',
                    surname: 'Doe',
                    role: 'STRIKER',
                    age: 25,
                    team: { club: { name: 'FC Test' } }
                },
                addedValue: 1000000,
                currentValue: 1200000,
                priority: 'HIGH',
                category: 'TARGET',
                addedDate: new Date().toISOString()
            }
        ]
    };

    const mockNotifications = [
        {
            id: 1,
            title: 'Test Notification',
            message: 'Test Message',
            type: 'PERFORMANCE',
            createdDate: new Date().toISOString(),
            isRead: false
        }
    ];

    const mockStats = {
        availablePlayers: 5,
        totalValue: 5000000
    };

    beforeEach(() => {
        api.getClubWatchlist.mockResolvedValue({ data: mockWatchlist });
        api.getWatchlistNotifications.mockResolvedValue({ data: mockNotifications });
        api.getWatchlistStats.mockResolvedValue({ data: mockStats });
    });

    test('renders tabs with correct accessibility attributes', async () => {
        render(<WatchlistManager clubId={1} />);

        await waitFor(() => {
            expect(screen.getByText('Transfer Watchlist')).toBeInTheDocument();
        });

        // Verify tablist role
        const tabList = screen.getByRole('tablist');
        expect(tabList).toBeInTheDocument();
        expect(tabList).toHaveAttribute('aria-label', 'Watchlist Sections');

        // Verify tab roles and attributes
        const playersTab = screen.getByRole('tab', { name: /Players/i });
        expect(playersTab).toHaveAttribute('aria-selected', 'true');
        expect(playersTab).toHaveAttribute('aria-controls', 'players-panel');
        expect(playersTab).toHaveAttribute('id', 'players-tab');

        const notificationsTab = screen.getByRole('tab', { name: /Notifications/i });
        expect(notificationsTab).toHaveAttribute('aria-selected', 'false');
        expect(notificationsTab).toHaveAttribute('aria-controls', 'notifications-panel');
        expect(notificationsTab).toHaveAttribute('id', 'notifications-tab');

        const statsTab = screen.getByRole('tab', { name: /Statistics/i });
        expect(statsTab).toHaveAttribute('aria-selected', 'false');
        expect(statsTab).toHaveAttribute('aria-controls', 'stats-panel');
        expect(statsTab).toHaveAttribute('id', 'stats-tab');

        // Verify tabpanel role and attributes
        const panel = screen.getByRole('tabpanel');
        expect(panel).toHaveAttribute('id', 'players-panel');
        expect(panel).toHaveAttribute('aria-labelledby', 'players-tab');
        expect(panel).toHaveAttribute('tabIndex', '0');
    });

    test('renders emoji icons with accessible roles and labels', async () => {
        render(<WatchlistManager clubId={1} />);

        await waitFor(() => {
            expect(screen.getByText('John Doe')).toBeInTheDocument();
        });

        // Verify that emojis are wrapped in role="img" with aria-label
        // The mock data has category 'TARGET' which maps to '🎯'
        const targetIcon = screen.getByRole('img', { name: /Target/i });
        expect(targetIcon).toBeInTheDocument();
        expect(targetIcon).toHaveTextContent('🎯');
    });
});
