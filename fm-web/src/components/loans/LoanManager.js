import React, { useState, useEffect } from 'react';
import { getActiveLoans } from '../../services/api';
import LoanCard from './LoanCard';

const LoanManager = ({ clubId }) => {
    const [activeLoans, setActiveLoans] = useState([]);
    const [selectedTab, setSelectedTab] = useState('outgoing');
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (clubId) {
            loadLoanData();
        }
    }, [clubId]);

    const loadLoanData = async () => {
        try {
            setLoading(true);
            const response = await getActiveLoans(clubId);
            setActiveLoans(response.data);
        } catch (error) {
            console.error('Error loading loan data:', error);
        } finally {
            setLoading(false);
        }
    };

    const outgoingLoans = activeLoans.filter(loan => loan.parentClubId === parseInt(clubId));
    const incomingLoans = activeLoans.filter(loan => loan.loanClubId === parseInt(clubId));

    if (loading) return <div>Loading loan data...</div>;

    return (
        <div className="loan-manager" style={{ padding: '20px' }}>
            <div className="loan-header" style={{ marginBottom: '20px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <h2 style={{ margin: 0 }}>Loan Management</h2>
                <div className="loan-stats" style={{ display: 'flex', gap: '20px' }}>
                    <div className="stat">
                        <span style={{ color: '#666', marginRight: '5px' }}>Players Out:</span>
                        <span style={{ fontWeight: 'bold' }}>{outgoingLoans.length}</span>
                    </div>
                    <div className="stat">
                        <span style={{ color: '#666', marginRight: '5px' }}>Players In:</span>
                        <span style={{ fontWeight: 'bold' }}>{incomingLoans.length}</span>
                    </div>
                </div>
            </div>

            <div className="loan-tabs" style={{ marginBottom: '20px', borderBottom: '1px solid #ccc' }}>
                <button
                    style={{
                        padding: '10px 20px',
                        cursor: 'pointer',
                        border: 'none',
                        background: 'none',
                        borderBottom: selectedTab === 'outgoing' ? '2px solid #1976d2' : 'none',
                        color: selectedTab === 'outgoing' ? '#1976d2' : '#666',
                        fontWeight: selectedTab === 'outgoing' ? 'bold' : 'normal'
                    }}
                    onClick={() => setSelectedTab('outgoing')}
                >
                    Players Out ({outgoingLoans.length})
                </button>
                <button
                    style={{
                        padding: '10px 20px',
                        cursor: 'pointer',
                        border: 'none',
                        background: 'none',
                        borderBottom: selectedTab === 'incoming' ? '2px solid #1976d2' : 'none',
                        color: selectedTab === 'incoming' ? '#1976d2' : '#666',
                        fontWeight: selectedTab === 'incoming' ? 'bold' : 'normal'
                    }}
                    onClick={() => setSelectedTab('incoming')}
                >
                    Players In ({incomingLoans.length})
                </button>
            </div>

            {selectedTab === 'outgoing' && (
                <div className="outgoing-loans">
                    {outgoingLoans.length === 0 ? <p>No outgoing loans.</p> : outgoingLoans.map(loan => (
                        <LoanCard key={loan.id} loan={loan} type="outgoing" onUpdate={loadLoanData} />
                    ))}
                </div>
            )}

            {selectedTab === 'incoming' && (
                <div className="incoming-loans">
                    {incomingLoans.length === 0 ? <p>No incoming loans.</p> : incomingLoans.map(loan => (
                        <LoanCard key={loan.id} loan={loan} type="incoming" onUpdate={loadLoanData} />
                    ))}
                </div>
            )}
        </div>
    );
};

export default LoanManager;
