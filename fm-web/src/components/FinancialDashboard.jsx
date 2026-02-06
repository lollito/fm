import React, { useState, useEffect } from 'react';
import { getFinancialDashboard, getTransactions } from '../services/api';
import { PieChart, Pie, Cell, Tooltip, Legend, ResponsiveContainer, LineChart, Line, XAxis, YAxis, CartesianGrid } from 'recharts';
import '../styles/FinancialDashboard.css';

const FinancialDashboard = ({ clubId }) => {
    const [dashboard, setDashboard] = useState(null);
    const [transactions, setTransactions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [transactionsLoading, setTransactionsLoading] = useState(false);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const SIZE = 10;

    useEffect(() => {
        if (clubId) {
            loadFinancialData();
            loadTransactions(0);
        }
    }, [clubId]);

    const loadFinancialData = async () => {
        try {
            const dashboardResponse = await getFinancialDashboard(clubId);
            setDashboard(dashboardResponse.data);
        } catch (error) {
            console.error('Error loading financial data:', error);
        } finally {
            setLoading(false);
        }
    };

    const loadTransactions = async (pageNumber) => {
        try {
            setTransactionsLoading(true);
            const response = await getTransactions(clubId, pageNumber, SIZE);
            setTransactions(response.data.content);
            setTotalPages(response.data.totalPages);
            setPage(pageNumber);
        } catch (error) {
            console.error('Error loading transactions:', error);
        } finally {
            setTransactionsLoading(false);
        }
    };

    const formatCurrency = (amount) => {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'USD',
            minimumFractionDigits: 0,
            maximumFractionDigits: 0
        }).format(amount);
    };

    const getBalanceColor = (balance) => {
        if (balance >= 1000000) return '#4caf50'; // Green for healthy
        if (balance >= 100000) return '#ff9800';  // Orange for warning
        return '#f44336'; // Red for critical
    };

    if (loading) return <div>Loading financial dashboard...</div>;
    if (!dashboard) return <div>No data available</div>;

    const revenueData = [
        { name: 'Matchday', value: dashboard.matchdayRevenue || 0 },
        { name: 'Sponsorship', value: dashboard.sponsorshipRevenue || 0 },
        { name: 'Merchandise', value: dashboard.merchandiseRevenue || 0 },
        { name: 'Transfers', value: dashboard.transferRevenue || 0 },
        { name: 'Prize Money', value: dashboard.prizeMoneyRevenue || 0 },
        { name: 'TV Rights', value: dashboard.tvRightsRevenue || 0 }
    ].filter(item => item.value > 0);

    const expenseData = [
        { name: 'Player Salaries', value: dashboard.playerSalaries || 0 },
        { name: 'Staff Salaries', value: dashboard.staffSalaries || 0 },
        { name: 'Transfers', value: dashboard.transferExpenses || 0 },
        { name: 'Maintenance', value: dashboard.facilityMaintenance || 0 },
        { name: 'Operations', value: dashboard.operationalCosts || 0 }
    ].filter(item => item.value > 0);

    const COLORS_REVENUE = ['#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF', '#FF9F40'];
    const COLORS_EXPENSE = ['#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF'];

    // Transform transactions for cash flow chart (simplified)
    const cashFlowData = transactions
        .slice()
        .reverse()
        .map(t => ({
            date: new Date(t.transactionDate).toLocaleDateString(),
            amount: t.type === 'INCOME' ? t.amount : -t.amount
        }));

    return (
        <div className="financial-dashboard">
            <div className="dashboard-header">
                <h2>Financial Overview</h2>
            </div>

            <div className="financial-summary">
                <div className="summary-card balance">
                    <h3>Current Balance</h3>
                    <span
                        className="amount"
                        style={{ color: getBalanceColor(dashboard.currentBalance) }}
                    >
                        {formatCurrency(dashboard.currentBalance)}
                    </span>
                </div>

                <div className="summary-card income">
                    <h3>Monthly Income</h3>
                    <span className="amount positive">
                        {formatCurrency(dashboard.monthlyIncome)}
                    </span>
                </div>

                <div className="summary-card expenses">
                    <h3>Monthly Expenses</h3>
                    <span className="amount negative">
                        {formatCurrency(dashboard.monthlyExpenses)}
                    </span>
                </div>

                <div className="summary-card profit">
                    <h3>Net Profit</h3>
                    <span
                        className={'amount ' + (dashboard.netProfit >= 0 ? 'positive' : 'negative')}
                    >
                        {formatCurrency(dashboard.netProfit)}
                    </span>
                </div>
            </div>

            <div className="financial-charts">
                <div className="chart-container">
                    <h3>Revenue Breakdown</h3>
                    <ResponsiveContainer width="100%" height={300}>
                        <PieChart>
                            <Pie
                                data={revenueData}
                                cx="50%"
                                cy="50%"
                                labelLine={false}
                                outerRadius={80}
                                fill="#8884d8"
                                dataKey="value"
                            >
                                {revenueData.map((entry, index) => (
                                    <Cell key={'cell-' + index} fill={COLORS_REVENUE[index % COLORS_REVENUE.length]} />
                                ))}
                            </Pie>
                            <Tooltip
                                formatter={(value) => formatCurrency(value)}
                                contentStyle={{ backgroundColor: '#1e1136', borderColor: 'rgba(161, 85, 255, 0.2)', color: '#f0f0f5' }}
                            />
                            <Legend wrapperStyle={{ color: '#a0a0b0' }} />
                        </PieChart>
                    </ResponsiveContainer>
                </div>

                <div className="chart-container">
                    <h3>Expense Breakdown</h3>
                    <ResponsiveContainer width="100%" height={300}>
                         <PieChart>
                            <Pie
                                data={expenseData}
                                cx="50%"
                                cy="50%"
                                labelLine={false}
                                outerRadius={80}
                                fill="#8884d8"
                                dataKey="value"
                            >
                                {expenseData.map((entry, index) => (
                                    <Cell key={'cell-' + index} fill={COLORS_EXPENSE[index % COLORS_EXPENSE.length]} />
                                ))}
                            </Pie>
                            <Tooltip
                                formatter={(value) => formatCurrency(value)}
                                contentStyle={{ backgroundColor: '#1e1136', borderColor: 'rgba(161, 85, 255, 0.2)', color: '#f0f0f5' }}
                            />
                            <Legend wrapperStyle={{ color: '#a0a0b0' }} />
                        </PieChart>
                    </ResponsiveContainer>
                </div>

                <div className="chart-container full-width">
                    <h3>Cash Flow Trend (Recent Transactions)</h3>
                    <ResponsiveContainer width="100%" height={300}>
                        <LineChart data={cashFlowData}>
                            <CartesianGrid strokeDasharray="3 3" stroke="rgba(161, 85, 255, 0.1)" />
                            <XAxis dataKey="date" stroke="#a0a0b0" />
                            <YAxis stroke="#a0a0b0" />
                            <Tooltip
                                formatter={(value) => formatCurrency(value)}
                                contentStyle={{ backgroundColor: '#1e1136', borderColor: 'rgba(161, 85, 255, 0.2)', color: '#f0f0f5' }}
                            />
                            <Legend wrapperStyle={{ color: '#a0a0b0' }} />
                            <Line type="monotone" dataKey="amount" stroke="#8884d8" activeDot={{ r: 8 }} />
                        </LineChart>
                    </ResponsiveContainer>
                </div>
            </div>

            <div className="recent-transactions">
                <h3>Transactions</h3>
                {transactionsLoading ? (
                    <div>Loading transactions...</div>
                ) : (
                    <>
                        <table className="transaction-table">
                            <thead>
                                <tr>
                                    <th>Date</th>
                                    <th>Description</th>
                                    <th>Category</th>
                                    <th>Type</th>
                                    <th className="text-right">Amount</th>
                                </tr>
                            </thead>
                            <tbody>
                                {transactions.map(transaction => (
                                    <tr key={transaction.id}>
                                        <td>{new Date(transaction.transactionDate).toLocaleDateString()}</td>
                                        <td>{transaction.description}</td>
                                        <td>{transaction.category}</td>
                                        <td>
                                            <span className={`badge ${transaction.type === 'INCOME' ? 'badge-success' : 'badge-danger'}`}>
                                                {transaction.type}
                                            </span>
                                        </td>
                                        <td className={`text-right ${transaction.type === 'INCOME' ? 'positive' : 'negative'}`}>
                                            {transaction.type === 'INCOME' ? '+' : '-'}
                                            {formatCurrency(transaction.amount)}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                        <div className="pagination">
                            <button
                                disabled={page === 0}
                                onClick={() => loadTransactions(page - 1)}
                                className="btn btn-secondary btn-sm"
                            >
                                Previous
                            </button>
                            <span className="page-info">
                                Page {page + 1} of {totalPages}
                            </span>
                            <button
                                disabled={page === totalPages - 1}
                                onClick={() => loadTransactions(page + 1)}
                                className="btn btn-secondary btn-sm"
                            >
                                Next
                            </button>
                        </div>
                    </>
                )}
            </div>

            <div className="financial-health">
                <h3>Financial Health Indicators</h3>
                <div className="health-indicators">
                    <div className="indicator">
                        <span>Net Worth:</span>
                        <span className="value">{formatCurrency(dashboard.netWorth)}</span>
                    </div>
                    <div className="indicator">
                        <span>Total Debt:</span>
                        <span className="value negative">{formatCurrency(dashboard.debt)}</span>
                    </div>
                    <div className="indicator">
                        <span>Profit Margin:</span>
                        <span className={'value ' + (dashboard.profitMargin >= 0 ? 'positive' : 'negative')}>
                            {(dashboard.profitMargin * 100).toFixed(1)}%
                        </span>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default FinancialDashboard;
