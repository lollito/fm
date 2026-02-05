import React, { useState, useEffect } from 'react';
import { getInfrastructureOverview, getAvailableUpgrades, startUpgrade } from '../../services/api';
import '../../styles/InfrastructureDashboard.css';

const InfrastructureDashboard = ({ clubId }) => {
    const [overview, setOverview] = useState(null);
    const [selectedFacility, setSelectedFacility] = useState(null);
    const [availableUpgrades, setAvailableUpgrades] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (clubId) {
            loadInfrastructureData();
        }
    }, [clubId]);

    const loadInfrastructureData = async () => {
        try {
            const response = await getInfrastructureOverview(clubId);
            setOverview(response.data);
        } catch (error) {
            console.error('Error loading infrastructure data:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleFacilitySelect = async (facilityType) => {
        setSelectedFacility(facilityType);
        try {
            const response = await getAvailableUpgrades(clubId, facilityType);
            setAvailableUpgrades(response.data);
        } catch (error) {
            console.error('Error loading upgrades:', error);
        }
    };

    const handleStartUpgrade = async (upgrade) => {
        try {
            let facilityId = null;
            let facilityType = null;
            if (selectedFacility === 'STADIUM') {
                facilityId = overview.stadium.id;
                facilityType = 'STADIUM';
            } else if (selectedFacility === 'TRAINING_FACILITY') {
                 facilityId = overview.trainingFacility.id;
                 facilityType = 'TRAINING_FACILITY';
            } else if (selectedFacility === 'MEDICAL_CENTER') {
                 facilityId = overview.medicalCenter.id;
                 facilityType = 'MEDICAL_CENTER';
            } else if (selectedFacility === 'YOUTH_ACADEMY') {
                 facilityId = overview.youthAcademy.id;
                 facilityType = 'YOUTH_ACADEMY';
            }

            const request = {
                facilityType: facilityType,
                facilityId: facilityId,
                upgradeType: upgrade.upgradeType,
                upgradeName: upgrade.name,
                description: upgrade.description,
                cost: upgrade.cost,
                durationDays: upgrade.durationDays,
                qualityImprovement: 0,
                bonusEffects: upgrade.effects,
            };

            await startUpgrade(clubId, request);
            loadInfrastructureData();
            setAvailableUpgrades([]);
            setSelectedFacility(null);
        } catch (error) {
            console.error('Error starting upgrade:', error);
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

    const getQualityColor = (quality) => {
        if (quality >= 8) return '#4caf50'; // Green
        if (quality >= 6) return '#ff9800'; // Orange
        if (quality >= 4) return '#ffc107'; // Yellow
        return '#f44336'; // Red
    };

    if (loading) return <div>Loading infrastructure overview...</div>;
    if (!overview) return <div>No data available</div>;

    return (
        <div className="infrastructure-dashboard">
            <div className="dashboard-header">
                <h2>Infrastructure Management</h2>
                <div className="infrastructure-summary">
                    <div className="summary-item">
                        <span className="label">Total Value:</span>
                        <span className="value">{formatCurrency(overview.totalInfrastructureValue)}</span>
                    </div>
                    <div className="summary-item">
                        <span className="label">Monthly Maintenance:</span>
                        <span className="value negative">{formatCurrency(overview.totalMonthlyMaintenanceCost)}</span>
                    </div>
                </div>
            </div>

            <div className="facilities-grid">
                {/* Stadium */}
                <div className="facility-card" onClick={() => handleFacilitySelect('STADIUM')}>
                    <div className="facility-header">
                        <h3>Stadium</h3>
                        {overview.stadium && (
                            <span className="facility-name">{overview.stadium.name}</span>
                        )}
                    </div>

                    {overview.stadium ? (
                        <div className="facility-details">
                            <div className="capacity">
                                <span className="label">Capacity:</span>
                                <span className="value">{overview.stadium.capacity.toLocaleString()}</span>
                            </div>
                            <div className="quality-indicators">
                                <div className="quality-item">
                                    <span>Pitch Quality:</span>
                                    <div className="quality-bar">
                                        <div
                                            className="quality-fill"
                                            style={{
                                                width: `${overview.stadium.pitchQuality * 10}%`,
                                                backgroundColor: getQualityColor(overview.stadium.pitchQuality)
                                            }}
                                        ></div>
                                    </div>
                                    <span>{overview.stadium.pitchQuality}/10</span>
                                </div>
                                <div className="quality-item">
                                    <span>Facilities:</span>
                                    <div className="quality-bar">
                                        <div
                                            className="quality-fill"
                                            style={{
                                                width: `${overview.stadium.facilitiesQuality * 10}%`,
                                                backgroundColor: getQualityColor(overview.stadium.facilitiesQuality)
                                            }}
                                        ></div>
                                    </div>
                                    <span>{overview.stadium.facilitiesQuality}/10</span>
                                </div>
                            </div>
                            <div className="facility-features">
                                {overview.stadium.hasRoof && <span className="feature">Roof</span>}
                                {overview.stadium.hasUndersoilHeating && <span className="feature">Heating</span>}
                                {overview.stadium.hasVipBoxes && <span className="feature">VIP Boxes</span>}
                                {overview.stadium.hasMegastore && <span className="feature">Megastore</span>}
                            </div>
                            <div className="maintenance-cost">
                                Monthly: {formatCurrency(overview.stadium.maintenanceCost)}
                            </div>
                        </div>
                    ) : (
                        <div className="facility-placeholder">
                            <p>No stadium built</p>
                            <button className="btn-primary">Build Stadium</button>
                        </div>
                    )}
                </div>

                {/* Training Facility */}
                <div className="facility-card" onClick={() => handleFacilitySelect('TRAINING_FACILITY')}>
                    <div className="facility-header">
                        <h3>Training Facility</h3>
                        {overview.trainingFacility && (
                            <span className="facility-name">{overview.trainingFacility.name}</span>
                        )}
                    </div>

                    {overview.trainingFacility ? (
                        <div className="facility-details">
                            <div className="quality-indicators">
                                <div className="quality-item">
                                    <span>Overall Quality:</span>
                                    <div className="quality-bar">
                                        <div
                                            className="quality-fill"
                                            style={{
                                                width: `${overview.trainingFacility.overallQuality * 10}%`,
                                                backgroundColor: getQualityColor(overview.trainingFacility.overallQuality)
                                            }}
                                        ></div>
                                    </div>
                                    <span>{overview.trainingFacility.overallQuality}/10</span>
                                </div>
                            </div>
                            <div className="training-bonuses">
                                {overview.trainingFacility.physicalTrainingBonus > 0 &&
                                    <div className="bonus">
                                        Physical: +{(overview.trainingFacility.physicalTrainingBonus * 100).toFixed(1)}%
                                    </div>
                                }
                            </div>
                            <div className="facility-features">
                                <span className="feature">{overview.trainingFacility.numberOfPitches} Pitches</span>
                                {overview.trainingFacility.hasIndoorFacilities && <span className="feature">Indoor</span>}
                                {overview.trainingFacility.hasHydrotherapy && <span className="feature">Hydrotherapy</span>}
                                {overview.trainingFacility.hasVideoAnalysis && <span className="feature">Video Analysis</span>}
                            </div>
                            <div className="maintenance-cost">
                                Monthly: {formatCurrency(overview.trainingFacility.maintenanceCost)}
                            </div>
                        </div>
                    ) : (
                        <div className="facility-placeholder">
                            <p>No training facility built</p>
                            <button className="btn-primary">Build Training Facility</button>
                        </div>
                    )}
                </div>

                {/* Medical Center */}
                <div className="facility-card" onClick={() => handleFacilitySelect('MEDICAL_CENTER')}>
                    <div className="facility-header">
                        <h3>Medical Center</h3>
                        {overview.medicalCenter && (
                            <span className="facility-name">{overview.medicalCenter.name}</span>
                        )}
                    </div>

                    {overview.medicalCenter ? (
                        <div className="facility-details">
                            <div className="quality-indicators">
                                <div className="quality-item">
                                    <span>Overall Quality:</span>
                                    <div className="quality-bar">
                                        <div
                                            className="quality-fill"
                                            style={{
                                                width: `${overview.medicalCenter.overallQuality * 10}%`,
                                                backgroundColor: getQualityColor(overview.medicalCenter.overallQuality)
                                            }}
                                        ></div>
                                    </div>
                                    <span>{overview.medicalCenter.overallQuality}/10</span>
                                </div>
                            </div>
                            <div className="medical-bonuses">
                                {overview.medicalCenter.injuryPreventionBonus > 0 &&
                                    <div className="bonus">
                                        Injury Prevention: +{(overview.medicalCenter.injuryPreventionBonus * 100).toFixed(1)}%
                                    </div>
                                }
                            </div>
                            <div className="facility-features">
                                <span className="feature">{overview.medicalCenter.numberOfDoctors} Doctors</span>
                                {overview.medicalCenter.hasMriScanner && <span className="feature">MRI</span>}
                                {overview.medicalCenter.hasCryotherapy && <span className="feature">Cryotherapy</span>}
                                {overview.medicalCenter.hasHyperbaricChamber && <span className="feature">Hyperbaric</span>}
                            </div>
                            <div className="maintenance-cost">
                                Monthly: {formatCurrency(overview.medicalCenter.maintenanceCost)}
                            </div>
                        </div>
                    ) : (
                        <div className="facility-placeholder">
                            <p>No medical center built</p>
                            <button className="btn-primary">Build Medical Center</button>
                        </div>
                    )}
                </div>

                {/* Youth Academy */}
                <div className="facility-card" onClick={() => handleFacilitySelect('YOUTH_ACADEMY')}>
                    <div className="facility-header">
                        <h3>Youth Academy</h3>
                        {overview.youthAcademy && (
                            <span className="facility-name">{overview.youthAcademy.name}</span>
                        )}
                    </div>

                    {overview.youthAcademy ? (
                        <div className="facility-details">
                            <div className="quality-indicators">
                                <div className="quality-item">
                                    <span>Overall Quality:</span>
                                    <div className="quality-bar">
                                        <div
                                            className="quality-fill"
                                            style={{
                                                width: `${overview.youthAcademy.overallQuality * 10}%`,
                                                backgroundColor: getQualityColor(overview.youthAcademy.overallQuality)
                                            }}
                                        ></div>
                                    </div>
                                    <span>{overview.youthAcademy.overallQuality}/10</span>
                                </div>
                            </div>
                            <div className="youth-bonuses">
                                {overview.youthAcademy.talentGenerationBonus > 0 &&
                                    <div className="bonus">
                                        Talent Generation: +{(overview.youthAcademy.talentGenerationBonus * 100).toFixed(1)}%
                                    </div>
                                }
                            </div>
                            <div className="facility-features">
                                <span className="feature">Capacity: {overview.youthAcademy.maxYouthPlayers}</span>
                                <span className="feature">{overview.youthAcademy.numberOfCoaches} Coaches</span>
                                {overview.youthAcademy.hasEducationCenter && <span className="feature">Education</span>}
                                {overview.youthAcademy.hasScoutingNetwork && <span className="feature">Scouting</span>}
                            </div>
                            <div className="maintenance-cost">
                                Monthly: {formatCurrency(overview.youthAcademy.maintenanceCost)}
                            </div>
                        </div>
                    ) : (
                        <div className="facility-placeholder">
                            <p>No youth academy built</p>
                            <button className="btn-primary">Build Youth Academy</button>
                        </div>
                    )}
                </div>
            </div>

            {/* Ongoing Upgrades */}
            {overview.ongoingUpgrades && overview.ongoingUpgrades.length > 0 && (
                <div className="ongoing-upgrades">
                    <h3>Ongoing Upgrades ({overview.ongoingUpgrades.length})</h3>
                    <div className="upgrades-list">
                        {overview.ongoingUpgrades.map(upgrade => (
                            <div key={upgrade.id} className="upgrade-card">
                                <div className="upgrade-info">
                                    <h4>{upgrade.upgradeName}</h4>
                                    <p>{upgrade.description}</p>
                                    <span className="facility-type">{upgrade.facilityType}</span>
                                </div>
                                <div className="upgrade-progress">
                                    <div className="progress-bar">
                                        <div
                                            className="progress-fill"
                                            style={{ width: `${calculateUpgradeProgress(upgrade)}%` }}
                                        ></div>
                                    </div>
                                    <span className="progress-text">
                                        {calculateRemainingDays(upgrade.plannedCompletionDate)} days remaining
                                    </span>
                                </div>
                                <div className="upgrade-cost">
                                    {formatCurrency(upgrade.cost)}
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            )}

            {/* Available Upgrades Modal */}
            {selectedFacility && availableUpgrades.length > 0 && (
                <div className="upgrades-modal">
                    <div className="modal-content">
                        <div className="modal-header">
                            <h3>Available Upgrades - {selectedFacility.replace('_', ' ')}</h3>
                            <button className="close-btn" onClick={() => setSelectedFacility(null)}>×</button>
                        </div>
                        <div className="upgrades-list">
                            {availableUpgrades.map((upgrade, index) => (
                                <div key={index} className="upgrade-option">
                                    <div className="upgrade-details">
                                        <h4>{upgrade.name}</h4>
                                        <p>{upgrade.description}</p>
                                        <div className="upgrade-effects">
                                            <strong>Effects:</strong> {upgrade.effects}
                                        </div>
                                        {upgrade.requirements && (
                                            <div className="upgrade-requirements">
                                                <strong>Requirements:</strong> {upgrade.requirements}
                                            </div>
                                        )}
                                    </div>
                                    <div className="upgrade-specs">
                                        <div className="spec">
                                            <span className="label">Cost:</span>
                                            <span className="value">{formatCurrency(upgrade.cost)}</span>
                                        </div>
                                        <div className="spec">
                                            <span className="label">Duration:</span>
                                            <span className="value">{upgrade.durationDays} days</span>
                                        </div>
                                        <button
                                            onClick={() => handleStartUpgrade(upgrade)}
                                            className="btn-primary"
                                        >
                                            Start Upgrade
                                        </button>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>
            )}

            {selectedFacility && availableUpgrades.length === 0 && (
                 <div className="upgrades-modal">
                    <div className="modal-content">
                        <div className="modal-header">
                            <h3>Available Upgrades - {selectedFacility.replace('_', ' ')}</h3>
                            <button className="close-btn" onClick={() => setSelectedFacility(null)}>×</button>
                        </div>
                        <div className="upgrades-list">
                             <p>No upgrades available for this facility at the moment.</p>
                        </div>
                    </div>
                 </div>
            )}
        </div>
    );
};

const calculateUpgradeProgress = (upgrade) => {
    const start = new Date(upgrade.startDate);
    const planned = new Date(upgrade.plannedCompletionDate);
    const now = new Date();

    const total = planned - start;
    const elapsed = now - start;

    if (total <= 0) return 100;

    return Math.min(Math.max((elapsed / total) * 100, 0), 100);
};

const calculateRemainingDays = (completionDate) => {
    const completion = new Date(completionDate);
    const now = new Date();
    const diff = completion - now;

    return Math.max(Math.ceil(diff / (1000 * 60 * 60 * 24)), 0);
};

export default InfrastructureDashboard;
