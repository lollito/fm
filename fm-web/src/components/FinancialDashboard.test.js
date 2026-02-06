/**
 * @jest-environment jsdom
 */
import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import FinancialDashboard from './FinancialDashboard';
import * as api from '../services/api';

// Mock the API service
jest.mock('../services/api');

// Mock Recharts
jest.mock('recharts', () => ({
    ResponsiveContainer: ({ children }) => <div className="recharts-responsive-container">{children}</div>,
    PieChart: ({ children }) => <div className="recharts-pie-chart">{children}</div>,
    Pie: () => <div className="recharts-pie" />,
    Cell: () => <div className="recharts-cell" />,
    Tooltip: () => <div className="recharts-tooltip" />,
    Legend: () => <div className="recharts-legend" />,
    LineChart: ({ children }) => <div className="recharts-line-chart">{children}</div>,
    Line: () => <div className="recharts-line" />,
    XAxis: () => <div className="recharts-x-axis" />,
    YAxis: () => <div className="recharts-y-axis" />,
    CartesianGrid: () => <div className="recharts-cartesian-grid" />
}));

describe('FinancialDashboard Component', () => {
    const mockClubId = 1;

    beforeEach(() => {
        jest.clearAllMocks();
    });

    test('renders loading state initially', () => {
        // Setup mocks to not resolve immediately or just to exist
        api.getFinancialDashboard.mockImplementation(() => new Promise(() => {}));
        api.getTransactions.mockImplementation(() => new Promise(() => {}));

        render(<FinancialDashboard clubId={mockClubId} />);
        expect(screen.getByText('Loading financial dashboard...')).toBeInTheDocument();
    });

    test('renders "No data available" when API returns null data', async () => {
        // Mock getFinancialDashboard to return null data
        api.getFinancialDashboard.mockResolvedValue({ data: null });
        api.getTransactions.mockResolvedValue({ data: { content: [], totalPages: 0 } });

        render(<FinancialDashboard clubId={mockClubId} />);

        await waitFor(() => {
            expect(screen.getByText('No data available')).toBeInTheDocument();
        });
    });

    test('renders financial dashboard with data', async () => {
        const mockDashboardData = {
            currentBalance: 1500000,
            monthlyIncome: 200000,
            monthlyExpenses: 150000,
            netProfit: 50000,
            netWorth: 5000000,
            debt: 100000,
            profitMargin: 0.25,
            matchdayRevenue: 50000,
            sponsorshipRevenue: 50000,
            merchandiseRevenue: 50000,
            transferRevenue: 50000,
            prizeMoneyRevenue: 0,
            tvRightsRevenue: 0,
            playerSalaries: 100000,
            staffSalaries: 20000,
            transferExpenses: 10000,
            facilityMaintenance: 10000,
            operationalCosts: 10000
        };

        const mockTransactionsData = {
            content: [
                {
                    id: 1,
                    transactionDate: '2023-01-01',
                    description: 'Sponsorship Payment',
                    category: 'SPONSORSHIP',
                    type: 'INCOME',
                    amount: 50000
                }
            ],
            totalPages: 1
        };

        api.getFinancialDashboard.mockResolvedValue({ data: mockDashboardData });
        api.getTransactions.mockResolvedValue({ data: mockTransactionsData });

        render(<FinancialDashboard clubId={mockClubId} />);

        await waitFor(() => {
            expect(screen.getByText('Financial Overview')).toBeInTheDocument();
            expect(screen.getByText('$1,500,000')).toBeInTheDocument(); // Current Balance
            expect(screen.getByText('Sponsorship Payment')).toBeInTheDocument();
        });
    });
});
