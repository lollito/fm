/**
 * @jest-environment jsdom
 */
import React from 'react';
import { render, screen, waitFor, fireEvent, act } from '@testing-library/react';
import '@testing-library/jest-dom';
import FinancialDashboard from './FinancialDashboard';
import * as api from '../services/api';

// Mock the API service
jest.mock('../services/api');

// Mock Recharts
jest.mock('recharts', () => ({
    ResponsiveContainer: ({ children }) => <div className="recharts-responsive-container">{children}</div>,
    PieChart: ({ children }) => <div className="recharts-pie-chart">{children}</div>,
    Pie: ({ children }) => <div className="recharts-pie">{children}</div>,
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
    });
    const mockDashboardData = {
        currentBalance: 1500000,
        monthlyIncome: 200000,
        monthlyExpenses: 150000,
        netProfit: 50000,
        matchdayRevenue: 50000,
        sponsorshipRevenue: 50000,
        merchandiseRevenue: 50000,
        transferRevenue: 50000,
        prizeMoneyRevenue: 0,
        tvRightsRevenue: 0,
        playerSalaries: 100000,
        staffSalaries: 20000,
        transferExpenses: 20000,
        facilityMaintenance: 5000,
        operationalCosts: 5000,
        netWorth: 2000000,
        debt: 0,
        profitMargin: 0.25
    };

    const mockTransactionsData = {
        content: [
            {
                id: 1,
                transactionDate: '2023-10-01',
                description: 'Ticket Sales',
                category: 'MATCHDAY',
                type: 'INCOME',
                amount: 50000
            },
            {
                id: 2,
                transactionDate: '2023-10-02',
                description: 'Player Wages',
                category: 'SALARIES',
                type: 'EXPENSE',
                amount: 100000
            }
        ],
        totalPages: 2,
        totalElements: 25
    };

    beforeEach(() => {
        jest.clearAllMocks();
        // Set default mocks to prevent crashes in unrelated tests
        api.getFinancialDashboard.mockResolvedValue({ data: mockDashboardData });
        api.getTransactions.mockResolvedValue({ data: mockTransactionsData });
    });

    test('renders loading state initially', async () => {
        // Override mocks to return pending promises
        api.getFinancialDashboard.mockReturnValue(new Promise(() => {}));
        api.getTransactions.mockReturnValue(new Promise(() => {}));

        render(<FinancialDashboard clubId={1} />);
        expect(screen.getByText('Loading financial dashboard...')).toBeInTheDocument();
    });

    test('renders error state when API fails', async () => {
        api.getFinancialDashboard.mockRejectedValue(new Error('Failed to fetch'));
        // Maintain successful transaction mock to isolate dashboard failure

        render(<FinancialDashboard clubId={1} />);

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

    test('renders financial data correctly', async () => {
        // Mocks are already set in beforeEach

        render(<FinancialDashboard clubId={1} />);

        await waitFor(() => {
            expect(screen.getByText('Financial Overview')).toBeInTheDocument();
        });

        // Check summary cards
        expect(screen.getByText('Current Balance')).toBeInTheDocument();
        expect(screen.getByText('$1,500,000')).toBeInTheDocument();
        expect(screen.getByText('$200,000')).toBeInTheDocument(); // Income
        expect(screen.getByText('$150,000')).toBeInTheDocument(); // Expenses
        expect(screen.getByText('$50,000')).toBeInTheDocument();  // Profit

        // Check Net Worth and Debt
        expect(screen.getByText('$2,000,000')).toBeInTheDocument(); // Net Worth
        expect(screen.getByText('$0')).toBeInTheDocument();         // Debt
        expect(screen.getByText('25.0%')).toBeInTheDocument();      // Profit Margin
    });

    test('renders transactions table correctly', async () => {
        // Mocks are already set in beforeEach

        render(<FinancialDashboard clubId={1} />);

        await waitFor(() => {
            expect(screen.getByText('Transactions')).toBeInTheDocument();
        });

        // Check table headers
        expect(screen.getByText('Date')).toBeInTheDocument();
        expect(screen.getByText('Description')).toBeInTheDocument();
        expect(screen.getByText('Category')).toBeInTheDocument();
        expect(screen.getByText('Type')).toBeInTheDocument();
        expect(screen.getByText('Amount')).toBeInTheDocument();

        // Check transaction rows
        expect(screen.getByText('Ticket Sales')).toBeInTheDocument();
        expect(screen.getByText('MATCHDAY')).toBeInTheDocument();
        expect(screen.getByText('INCOME')).toBeInTheDocument();
        expect(screen.getByText('+$50,000')).toBeInTheDocument();

        expect(screen.getByText('Player Wages')).toBeInTheDocument();
        expect(screen.getByText('SALARIES')).toBeInTheDocument();
        expect(screen.getByText('EXPENSE')).toBeInTheDocument();
        expect(screen.getByText('-$100,000')).toBeInTheDocument();
    });

    test('handles pagination correctly', async () => {
        // Initial render mocks are set in beforeEach

        render(<FinancialDashboard clubId={1} />);

        await waitFor(() => {
            expect(screen.getByText('Transactions')).toBeInTheDocument();
        });

        const nextButton = screen.getByText('Next');
        expect(nextButton).toBeInTheDocument();
        expect(nextButton).not.toBeDisabled();

        // Mock next page response
        const mockNextPageData = {
            content: [],
            totalPages: 2,
            totalElements: 25
        };
        api.getTransactions.mockResolvedValueOnce({ data: mockNextPageData });

        await act(async () => {
            fireEvent.click(nextButton);
        });

        await waitFor(() => {
            expect(api.getTransactions).toHaveBeenCalledWith(1, 1, 10);
        });

        // Wait for re-render with new page info
        await waitFor(() => {
             expect(screen.getByText('Page 2 of 2')).toBeInTheDocument();
        });
    });
});
