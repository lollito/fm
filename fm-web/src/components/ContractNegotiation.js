import React, { useState, useEffect } from 'react';
import { startContractNegotiation, makeContractCounterOffer, acceptContractOffer, rejectContractOffer } from '../services/api';
import '../styles/ContractNegotiation.css';

const ContractNegotiation = ({ playerId, clubId, onClose }) => {
    const [negotiation, setNegotiation] = useState(null);
    const [currentOffer, setCurrentOffer] = useState({
        weeklySalary: 0,
        signingBonus: 0,
        loyaltyBonus: 0,
        contractYears: 3,
        releaseClause: 0
    });
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        initializeNegotiation();
    }, [playerId, clubId]);

    const initializeNegotiation = async () => {
        setLoading(true);
        try {
            const response = await startContractNegotiation({
                playerId,
                clubId,
                type: 'NEW_CONTRACT',
                initialOffer: currentOffer
            });
            setNegotiation(response.data);

            // Initialize current offer with club's offer
             setCurrentOffer({
                weeklySalary: response.data.offeredWeeklySalary || 0,
                signingBonus: response.data.offeredSigningBonus || 0,
                loyaltyBonus: response.data.offeredLoyaltyBonus || 0,
                contractYears: response.data.offeredContractYears || 1,
                releaseClause: response.data.offeredReleaseClause || 0
            });
        } catch (error) {
            console.error('Error starting negotiation:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleMakeOffer = async () => {
        setLoading(true);
        try {
            const response = await makeContractCounterOffer(negotiation.id, {
                offerSide: 'CLUB',
                offer: currentOffer
            });

            // Refresh negotiation data
            // Since API returns NegotiationOfferDTO, we might need to fetch negotiation again or update locally
            // But the component displays negotiation state.
            // The task code snippet updated negotiation state manually:
            setNegotiation(prev => ({
                ...prev,
                offeredWeeklySalary: currentOffer.weeklySalary,
                offeredSigningBonus: currentOffer.signingBonus,
                offeredLoyaltyBonus: currentOffer.loyaltyBonus,
                offeredContractYears: currentOffer.contractYears,
                offeredReleaseClause: currentOffer.releaseClause,
                roundsOfNegotiation: prev.roundsOfNegotiation + 1,
                // Status might have changed if player accepted/rejected
                status: response.data.status === 'ACCEPTED' ? 'ACCEPTED' : (response.data.status === 'REJECTED' ? 'REJECTED' : prev.status)
            }));

            // If player countered, we should update demand fields too?
            // The API logic for `makeCounterOffer` returns `NegotiationOfferDTO`.
            // But `processOfferResponse` might update negotiation status.
            // Ideally we should reload negotiation. But for now I'll stick to the snippet logic + minimal updates.
        } catch (error) {
            console.error('Error making offer:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleAcceptOffer = async () => {
        setLoading(true);
        try {
            await acceptContractOffer(negotiation.id);
            // Show success message and close
            onClose();
        } catch (error) {
            console.error('Error accepting offer:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleRejectOffer = async (reason) => {
        setLoading(true);
        try {
            await rejectContractOffer(negotiation.id, { reason });
            onClose();
        } catch (error) {
            console.error('Error rejecting offer:', error);
        } finally {
            setLoading(false);
        }
    };

    const calculateOfferDifference = (offered, demanded) => {
        if (!demanded || demanded === 0) return 0;
        const difference = ((offered - demanded) / demanded) * 100;
        return difference.toFixed(1);
    };

    const getOfferStatus = (offered, demanded) => {
        if (!demanded || demanded === 0) return 'fair';
        const difference = (offered - demanded) / demanded;
        if (difference >= 0.1) return 'generous';
        if (difference >= 0) return 'fair';
        if (difference >= -0.1) return 'close';
        return 'low';
    };

    if (loading && !negotiation) return <div>Loading negotiation...</div>;
    if (!negotiation && !loading) return <div>Failed to start negotiation</div>;

    return (
        <div className="contract-negotiation">
            <div className="negotiation-header">
                <h2>Contract Negotiation</h2>
                <div className="player-info">
                    {/* Assuming negotiation.player is redundant or populated, but DTO uses playerId.
                        We might need player details passed as props or fetched.
                        The DTO `ContractNegotiationDTO` has `playerId` not `player` object.
                        So I'll use props or placeholder.
                        Snippet used negotiation.player.name. I'll assume we pass playerName prop or fetch player.
                        For now I'll just show IDs or remove name.
                    */}
                    <span>Player ID: {negotiation.playerId}</span>
                    <span>Round {negotiation.roundsOfNegotiation + 1}</span>
                </div>
            </div>

            <div className="negotiation-content">
                <div className="offers-comparison">
                    <div className="offer-section">
                        <h4>Your Offer</h4>
                        <div className="offer-details">
                            <div className="offer-item">
                                <label>Weekly Salary:</label>
                                <input
                                    type="number"
                                    value={currentOffer.weeklySalary}
                                    onChange={(e) => setCurrentOffer(prev => ({
                                        ...prev,
                                        weeklySalary: parseInt(e.target.value)
                                    }))}
                                />
                                <span className={`difference ${getOfferStatus(currentOffer.weeklySalary, negotiation.demandedWeeklySalary)}`}>
                                    {calculateOfferDifference(currentOffer.weeklySalary, negotiation.demandedWeeklySalary)}%
                                </span>
                            </div>

                            <div className="offer-item">
                                <label>Signing Bonus:</label>
                                <input
                                    type="number"
                                    value={currentOffer.signingBonus}
                                    onChange={(e) => setCurrentOffer(prev => ({
                                        ...prev,
                                        signingBonus: parseInt(e.target.value)
                                    }))}
                                />
                                <span className={`difference ${getOfferStatus(currentOffer.signingBonus, negotiation.demandedSigningBonus)}`}>
                                    {calculateOfferDifference(currentOffer.signingBonus, negotiation.demandedSigningBonus)}%
                                </span>
                            </div>

                            <div className="offer-item">
                                <label>Contract Years:</label>
                                <select
                                    value={currentOffer.contractYears}
                                    onChange={(e) => setCurrentOffer(prev => ({
                                        ...prev,
                                        contractYears: parseInt(e.target.value)
                                    }))}
                                >
                                    <option value={1}>1 Year</option>
                                    <option value={2}>2 Years</option>
                                    <option value={3}>3 Years</option>
                                    <option value={4}>4 Years</option>
                                    <option value={5}>5 Years</option>
                                </select>
                                <span className={`difference ${getOfferStatus(currentOffer.contractYears, negotiation.demandedContractYears)}`}>
                                    {currentOffer.contractYears >= negotiation.demandedContractYears ? '✓' : '✗'}
                                </span>
                            </div>

                            <div className="offer-item">
                                <label>Release Clause:</label>
                                <input
                                    type="number"
                                    value={currentOffer.releaseClause}
                                    onChange={(e) => setCurrentOffer(prev => ({
                                        ...prev,
                                        releaseClause: parseInt(e.target.value)
                                    }))}
                                />
                                <span className={`difference ${getOfferStatus(currentOffer.releaseClause, negotiation.demandedReleaseClause)}`}>
                                    {currentOffer.releaseClause >= negotiation.demandedReleaseClause ? '✓' : '✗'}
                                </span>
                            </div>
                        </div>
                    </div>

                    <div className="demand-section">
                        <h4>Player Demands</h4>
                        <div className="demand-details">
                            <div className="demand-item">
                                <label>Weekly Salary:</label>
                                <span>${(negotiation.demandedWeeklySalary || 0).toLocaleString()}</span>
                            </div>
                            <div className="demand-item">
                                <label>Signing Bonus:</label>
                                <span>${(negotiation.demandedSigningBonus || 0).toLocaleString()}</span>
                            </div>
                            <div className="demand-item">
                                <label>Contract Years:</label>
                                <span>{negotiation.demandedContractYears} years</span>
                            </div>
                            <div className="demand-item">
                                <label>Release Clause:</label>
                                <span>${(negotiation.demandedReleaseClause || 0).toLocaleString()}</span>
                            </div>
                        </div>
                    </div>
                </div>

                <div className="negotiation-progress">
                    <div className="progress-bar">
                        <div
                            className="progress-fill"
                            style={{ width: `${Math.min(100, (negotiation.roundsOfNegotiation / 10) * 100)}%` }}
                        ></div>
                    </div>
                    <span>Negotiation Progress: {negotiation.roundsOfNegotiation}/10 rounds</span>
                </div>

                <div className="total-cost">
                    <h4>Total Contract Cost</h4>
                    <div className="cost-breakdown">
                        <div>Weekly Salary: ${currentOffer.weeklySalary.toLocaleString()}</div>
                        <div>Annual Salary: ${(currentOffer.weeklySalary * 52).toLocaleString()}</div>
                        <div>Total Contract: ${(currentOffer.weeklySalary * 52 * currentOffer.contractYears + currentOffer.signingBonus).toLocaleString()}</div>
                    </div>
                </div>
            </div>

            <div className="negotiation-actions">
                <button
                    className="make-offer-btn"
                    onClick={handleMakeOffer}
                    disabled={loading}
                >
                    Make Offer
                </button>

                <button
                    className="accept-btn"
                    onClick={handleAcceptOffer}
                    disabled={loading}
                >
                    Accept Current Terms
                </button>

                <button
                    className="reject-btn"
                    onClick={() => handleRejectOffer('Terms not acceptable')}
                    disabled={loading}
                >
                    End Negotiation
                </button>
            </div>

            <div className="negotiation-tips">
                <h4>Negotiation Tips</h4>
                <ul>
                    <li>Players value security - longer contracts may reduce salary demands</li>
                    <li>Signing bonuses can help close deals without increasing weekly wages</li>
                    <li>Release clauses protect the club but may increase other demands</li>
                    <li>Player age and performance affect their negotiating power</li>
                </ul>
            </div>
        </div>
    );
};

export default ContractNegotiation;
