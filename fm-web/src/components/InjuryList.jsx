import React, { useState, useEffect } from 'react';
import { getTeamInjuries, getClubInjuries } from '../services/api';

const InjuryList = ({ teamId, clubId }) => {
    const [injuries, setInjuries] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if(teamId || clubId) {
            loadInjuries();
        }
    }, [teamId, clubId]);

    const loadInjuries = async () => {
        try {
            let response;
            if (teamId) {
                response = await getTeamInjuries(teamId);
            } else if (clubId) {
                response = await getClubInjuries(clubId);
            }
            if (response) {
                setInjuries(response.data);
            }
        } catch (error) {
            console.error('Error loading injuries:', error);
        } finally {
            setLoading(false);
        }
    };

    const getInjuryIcon = (severity) => {
        const icons = {
            MINOR: '🟡',
            MODERATE: '🟠',
            MAJOR: '🔴',
            SEVERE: '⚫'
        };
        return icons[severity] || '❓';
    };

    const formatRecoveryTime = (expectedDate) => {
        const days = Math.ceil(
            (new Date(expectedDate) - new Date()) / (1000 * 60 * 60 * 24)
        );
        return days > 0 ? days + ' days' : 'Ready';
    };

    if (loading) return <div>Loading injuries...</div>;

    return (
        <div className="card mt-4">
            <div className="card-header bg-danger text-white">
                <h5 className="mb-0">Team Injuries</h5>
            </div>
            <div className="card-body">
                {injuries.length === 0 ? (
                    <p>No current injuries</p>
                ) : (
                    <div className="row">
                        {injuries.map(injury => (
                            <div key={injury.id} className="col-md-6 mb-3">
                                <div className="card border-danger">
                                    <div className="card-body">
                                        <div className="d-flex justify-content-between align-items-center mb-2">
                                            <h6 className="card-title mb-0">
                                                {getInjuryIcon(injury.severity)} {injury.playerName} {injury.playerSurname}
                                            </h6>
                                            <span className="badge bg-danger">{injury.status}</span>
                                        </div>
                                        <div className="small">
                                            <p className="mb-1"><strong>Type:</strong> {injury.type}</p>
                                            <p className="mb-1"><strong>Severity:</strong> {injury.severity}</p>
                                            <p className="mb-1"><strong>Recovery:</strong> {formatRecoveryTime(injury.expectedRecoveryDate)}</p>
                                            <p className="mb-0 text-muted">
                                               {injury.description}
                                            </p>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
};

export default InjuryList;
