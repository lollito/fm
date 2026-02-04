import React, { useState, useEffect } from 'react';
import { getSponsorshipDashboard, generateOffers, acceptOffer, rejectOffer } from '../services/api';
import '../styles/SponsorshipDashboard.css';

const SponsorshipDashboard = ({ clubId }) => {
    const [dashboard, setDashboard] = useState(null);
    const [loading, setLoading] = useState(true);
    // eslint-disable-next-line no-unused-vars
    const [selectedOffer, setSelectedOffer] = useState(null);

    useEffect(() => {
        if (clubId) {
            loadSponsorshipData();
        }
    }, [clubId]);

    const loadSponsorshipData = async () => {
        try {
            const response = await getSponsorshipDashboard(clubId);
            setDashboard(response.data);
        } catch (error) {
            console.error('Error loading sponsorship data:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleGenerateOffers = async () => {
        try {
            setLoading(true);
            await generateOffers(clubId);
            loadSponsorshipData(); // Refresh data
        } catch (error) {
            console.error('Error generating offers:', error);
            setLoading(false);
        }
    };

    const handleAcceptOffer = async (offerId) => {
        try {
            setLoading(true);
            await acceptOffer(offerId);
            loadSponsorshipData(); // Refresh data
        } catch (error) {
            console.error('Error accepting offer:', error);
            setLoading(false);
        }
    };

    const handleRejectOffer = async (offerId) => {
        try {
            setLoading(true);
            await rejectOffer(offerId);
            loadSponsorshipData(); // Refresh data
        } catch (error) {
            console.error('Error rejecting offer:', error);
            setLoading(false);
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

    if (loading && !dashboard) return <div>Loading sponsorship dashboard...</div>;
    if (!dashboard) return <div>No data available</div>;

    return (
        <div className="sponsorship-dashboard">
            <div className="dashboard-header">
                <h2>Sponsorship Management</h2>
                <button onClick={handleGenerateOffers} className="btn btn-primary">
                    Generate New Offers
                </button>
            </div>

            <div className="sponsorship-summary">
                <div className="summary-card">
                    <h3>Total Annual Value</h3>
                    <span className="amount">{formatCurrency(dashboard.totalAnnualValue)}</span>
                </div>
                <div className="summary-card">
                    <h3>Active Deals</h3>
                    <span className="count">{dashboard.totalActiveDeals}</span>
                </div>
                <div className="summary-card">
                    <h3>Club Attractiveness</h3>
                    <span className="score">{dashboard.clubAttractiveness.overallScore ? dashboard.clubAttractiveness.overallScore.toFixed(1) : '0.0'}/100</span>
                </div>
            </div>

            <div className="pending-offers section">
                <h3>Pending Offers ({dashboard.pendingOffers.length})</h3>
                {dashboard.pendingOffers.length === 0 ? (
                    <p>No pending offers. Generate new offers to see available sponsorship deals.</p>
                ) : (
                    <div className="offers-list">
                        {dashboard.pendingOffers.map(offer => (
                            <div key={offer.id} className="offer-card">
                                <div className="offer-header">
                                    <div className="sponsor-info">
                                        {offer.sponsor.logo && <img src={offer.sponsor.logo} alt={offer.sponsor.name} />}
                                        <div>
                                            <h4>{offer.sponsor.name}</h4>
                                            <span className="sponsor-tier">{offer.sponsor.tier}</span>
                                        </div>
                                    </div>
                                    <div className="offer-type">
                                        <span className="type-badge">{offer.type}</span>
                                    </div>
                                </div>

                                <div className="offer-details">
                                    <div className="offer-value">
                                        <span className="label">Annual Value:</span>
                                        <span className="value">{formatCurrency(offer.offeredAnnualValue)}</span>
                                    </div>
                                    <div className="contract-length">
                                        <span className="label">Contract Length:</span>
                                        <span className="value">{offer.contractYears} years</span>
                                    </div>
                                    <div className="total-value">
                                        <span className="label">Total Value:</span>
                                        <span className="value">
                                            {formatCurrency(offer.offeredAnnualValue * offer.contractYears)}
                                        </span>
                                    </div>
                                </div>

                                {(offer.leaguePositionBonus > 0 || offer.cupProgressBonus > 0 || offer.attendanceBonus > 0) && (
                                    <div className="performance-bonuses">
                                        <h5>Performance Bonuses:</h5>
                                        {offer.leaguePositionBonus > 0 && (
                                            <div className="bonus">
                                                League Position: {formatCurrency(offer.leaguePositionBonus)}
                                            </div>
                                        )}
                                        {offer.cupProgressBonus > 0 && (
                                            <div className="bonus">
                                                Cup Progress: {formatCurrency(offer.cupProgressBonus)}
                                            </div>
                                        )}
                                        {offer.attendanceBonus > 0 && (
                                            <div className="bonus">
                                                Attendance: {formatCurrency(offer.attendanceBonus)}
                                            </div>
                                        )}
                                    </div>
                                )}

                                <div className="offer-actions">
                                    <button
                                        onClick={() => handleAcceptOffer(offer.id)}
                                        className="btn btn-success"
                                    >
                                        Accept Offer
                                    </button>
                                    <button
                                        onClick={() => handleRejectOffer(offer.id)}
                                        className="btn btn-danger"
                                    >
                                        Reject
                                    </button>
                                </div>

                                <div className="offer-expiry">
                                    Expires: {new Date(offer.expiryDate).toLocaleDateString()}
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>

            <div className="active-deals section">
                <h3>Active Sponsorship Deals ({dashboard.activeDeals.length})</h3>
                <div className="deals-list">
                    {dashboard.activeDeals.map(deal => (
                        <div key={deal.id} className="deal-card">
                            <div className="deal-header">
                                <div className="sponsor-info">
                                    {deal.sponsor.logo && <img src={deal.sponsor.logo} alt={deal.sponsor.name} />}
                                    <div>
                                        <h4>{deal.sponsor.name}</h4>
                                        <span className="deal-type">{deal.type}</span>
                                    </div>
                                </div>
                                <div className="deal-status">
                                    <span className={'status-badge ' + (deal.status ? deal.status.toLowerCase() : '')}>
                                        {deal.status}
                                    </span>
                                </div>
                            </div>

                            <div className="deal-details">
                                <div className="deal-value">
                                    <span className="label">Annual Value:</span>
                                    <span className="value">{formatCurrency(deal.currentAnnualValue)}</span>
                                </div>
                                <div className="contract-period">
                                    <span className="label">Contract Period:</span>
                                    <span className="value">
                                        {new Date(deal.startDate).toLocaleDateString() + ' - ' + new Date(deal.endDate).toLocaleDateString()}
                                    </span>
                                </div>
                            </div>

                            <div className="deal-progress">
                                <div className="progress-bar">
                                    <div
                                        className="progress-fill"
                                        style={{
                                            width: calculateContractProgress(deal.startDate, deal.endDate) + '%'
                                        }}
                                    ></div>
                                </div>
                                <span className="progress-text">
                                    {calculateRemainingTime(deal.endDate) + ' remaining'}
                                </span>
                            </div>
                        </div>
                    ))}
                </div>
            </div>

            <div className="club-attractiveness section">
                <h3>Club Attractiveness Breakdown</h3>
                <div className="attractiveness-metrics">
                    <div className="metric">
                        <span className="metric-name">League Position</span>
                        <div className="metric-bar">
                            <div
                                className="metric-fill"
                                style={{ width: (dashboard.clubAttractiveness.leaguePositionScore / 20 * 100) + '%' }}
                            ></div>
                        </div>
                        <span className="metric-value">{dashboard.clubAttractiveness.leaguePositionScore ? dashboard.clubAttractiveness.leaguePositionScore.toFixed(1) : 0}/20</span>
                    </div>
                    <div className="metric">
                        <span className="metric-name">Stadium</span>
                        <div className="metric-bar">
                            <div
                                className="metric-fill"
                                style={{ width: dashboard.clubAttractiveness.stadiumScore + '%' }}
                            ></div>
                        </div>
                        <span className="metric-value">{dashboard.clubAttractiveness.stadiumScore ? dashboard.clubAttractiveness.stadiumScore.toFixed(0) : 0}/100</span>
                    </div>
                    <div className="metric">
                        <span className="metric-name">Financial Stability</span>
                        <div className="metric-bar">
                            <div
                                className="metric-fill"
                                style={{ width: dashboard.clubAttractiveness.financialScore + '%' }}
                            ></div>
                        </div>
                        <span className="metric-value">{dashboard.clubAttractiveness.financialScore ? dashboard.clubAttractiveness.financialScore.toFixed(0) : 0}/100</span>
                    </div>
                </div>
            </div>
        </div>
    );
};

const calculateContractProgress = (startDate, endDate) => {
    const start = new Date(startDate);
    const end = new Date(endDate);
    const now = new Date();

    const total = end - start;
    const elapsed = now - start;

    return Math.min(Math.max((elapsed / total) * 100, 0), 100);
};

const calculateRemainingTime = (endDate) => {
    const end = new Date(endDate);
    const now = new Date();
    const diff = end - now;

    const days = Math.floor(diff / (1000 * 60 * 60 * 24));
    const months = Math.floor(days / 30);
    const years = Math.floor(months / 12);

    if (years > 0) return years + ' year' + (years > 1 ? 's' : '');
    if (months > 0) return months + ' month' + (months > 1 ? 's' : '');
    return days + ' day' + (days > 1 ? 's' : '');
};

export default SponsorshipDashboard;
