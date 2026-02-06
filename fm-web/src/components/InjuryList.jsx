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

    if (injuries.length === 0) return null;

    return (
        <div className="card mt-4 border-danger">
            <div className="card-header bg-danger text-white">
                <h5 className="mb-0">Team Injuries</h5>
            </div>
            <div className="card-body p-0">
                <table className="table mb-0">
                    <thead>
                        <tr>
                            <th>Player</th>
                            <th>Type</th>
                            <th>Severity</th>
                            <th>Recovery</th>
                            <th>Status</th>
                        </tr>
                    </thead>
                    <tbody>
                        {injuries.map(injury => (
                            <tr key={injury.id}>
                                <td>
                                    {getInjuryIcon(injury.severity)} {injury.playerName} {injury.playerSurname}
                                </td>
                                <td>{injury.type}</td>
                                <td>{injury.severity}</td>
                                <td>{formatRecoveryTime(injury.expectedRecoveryDate)}</td>
                                <td><span className="badge bg-danger">{injury.status}</span></td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default InjuryList;
