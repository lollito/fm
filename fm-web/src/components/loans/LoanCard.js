import React from 'react';
import { recallPlayerFromLoan, activatePurchaseOption, getLoanReviews } from '../../services/api';

const LoanCard = ({ loan, type, onUpdate }) => {
    const isOutgoing = type === 'outgoing';
    const otherClubName = isOutgoing ? loan.loanClubName : loan.parentClubName;

    const handleRecall = async () => {
        if (window.confirm("Are you sure you want to recall this player?")) {
            try {
                await recallPlayerFromLoan(loan.id, { reason: "Tactical decision" });
                onUpdate();
            } catch (error) {
                console.error("Error recalling player", error);
                alert("Failed to recall player");
            }
        }
    };

    const handlePurchase = async () => {
        if (window.confirm(`Are you sure you want to purchase this player for $${loan.optionToBuyPrice?.toLocaleString()}?`)) {
            try {
                await activatePurchaseOption(loan.id);
                onUpdate();
            } catch (error) {
                console.error("Error purchasing player", error);
                alert("Failed to purchase player");
            }
        }
    };

    return (
        <div className="loan-card" style={{ border: '1px solid #ccc', padding: '15px', margin: '10px 0', borderRadius: '8px', backgroundColor: '#fff', color: '#333', boxShadow: '0 2px 4px rgba(0,0,0,0.1)' }}>
            <div className="loan-player-info" style={{ marginBottom: '10px', borderBottom: '1px solid #eee', paddingBottom: '5px' }}>
                <h4 style={{ margin: 0 }}>{loan.playerName} {loan.playerSurname}</h4>
            </div>

            <div className="loan-details" style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px', fontSize: '0.9em' }}>
                <div className="detail-row">
                    <span style={{ color: '#666' }}>{isOutgoing ? 'Loaned to:' : 'Loaned from:'}</span>
                    <div style={{ fontWeight: 'bold' }}>{otherClubName}</div>
                </div>
                <div className="detail-row">
                    <span style={{ color: '#666' }}>Period:</span>
                    <div>
                        {new Date(loan.startDate).toLocaleDateString()} -
                        {new Date(loan.endDate).toLocaleDateString()}
                    </div>
                </div>
                <div className="detail-row">
                    <span style={{ color: '#666' }}>Salary Share:</span>
                    <div>
                        {isOutgoing ?
                            `${Math.round(loan.parentClubSalaryShare * 100)}% (You)` :
                            `${Math.round(loan.loanClubSalaryShare * 100)}% (You)`
                        }
                    </div>
                </div>
                <div className="detail-row">
                    <span style={{ color: '#666' }}>Appearances:</span>
                    <div>{loan.actualAppearances} / {loan.minimumAppearances || '-'}</div>
                </div>
            </div>

            <div className="loan-options" style={{ marginTop: '10px', display: 'flex', gap: '5px' }}>
                {loan.hasRecallClause && isOutgoing && (
                    <span className="option-badge recall" style={{ padding: '2px 8px', backgroundColor: '#e0f7fa', color: '#006064', borderRadius: '12px', fontSize: '0.8em' }}>Recall Available</span>
                )}
                {loan.hasOptionToBuy && !isOutgoing && (
                    <span className="option-badge purchase" style={{ padding: '2px 8px', backgroundColor: '#e8f5e9', color: '#1b5e20', borderRadius: '12px', fontSize: '0.8em' }}>Purchase Option</span>
                )}
            </div>

            <div className="loan-actions" style={{ marginTop: '15px', display: 'flex', gap: '10px' }}>
                {isOutgoing && loan.hasRecallClause && (
                    <button className="recall-btn" onClick={handleRecall} style={{ padding: '5px 10px', cursor: 'pointer', backgroundColor: '#d32f2f', color: 'white', border: 'none', borderRadius: '4px' }}>
                        Recall
                    </button>
                )}
                {!isOutgoing && loan.hasOptionToBuy && (
                    <button className="purchase-btn" onClick={handlePurchase} style={{ padding: '5px 10px', cursor: 'pointer', backgroundColor: '#2e7d32', color: 'white', border: 'none', borderRadius: '4px' }}>
                        Buy (${loan.optionToBuyPrice?.toLocaleString()})
                    </button>
                )}
                <button className="review-btn" onClick={() => alert("Reviews not implemented yet")} style={{ padding: '5px 10px', cursor: 'pointer', backgroundColor: '#1976d2', color: 'white', border: 'none', borderRadius: '4px' }}>
                    View Reviews
                </button>
            </div>
        </div>
    );
};

export default LoanCard;
