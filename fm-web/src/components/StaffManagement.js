import React, { useState, useEffect } from 'react';
import { getClubStaff, getAvailableStaff, hireStaff, fireStaff, getStaffBonuses } from '../services/api';
import { useToast } from '../context/ToastContext';

const StaffManagement = ({ clubId }) => {
    const [clubStaff, setClubStaff] = useState([]);
    const [availableStaff, setAvailableStaff] = useState([]);
    const [staffBonuses, setStaffBonuses] = useState(null);
    const [selectedTab, setSelectedTab] = useState('current');
    const [selectedRole, setSelectedRole] = useState('');
    const [loading, setLoading] = useState(true);
    const { showToast } = useToast();

    const staffRoles = [
        'HEAD_COACH', 'ASSISTANT_COACH', 'FITNESS_COACH', 'GOALKEEPING_COACH',
        'YOUTH_COACH', 'HEAD_PHYSIO', 'PHYSIO', 'DOCTOR', 'HEAD_SCOUT', 'SCOUT', 'ANALYST'
    ];

    useEffect(() => {
        if (clubId) {
            loadStaffData();
        }
    }, [clubId]);

    const loadStaffData = async () => {
        try {
            const [staffResponse, bonusesResponse] = await Promise.all([
                getClubStaff(clubId),
                getStaffBonuses(clubId)
            ]);
            setClubStaff(staffResponse.data);
            setStaffBonuses(bonusesResponse.data);
        } catch (error) {
            console.error('Error loading staff data:', error);
        } finally {
            setLoading(false);
        }
    };

    const loadAvailableStaff = async (role = '') => {
        try {
            const response = await getAvailableStaff(role);
            setAvailableStaff(response.data);
        } catch (error) {
            console.error('Error loading available staff:', error);
        }
    };

    const handleHireStaff = async (staffId, contractDetails) => {
        try {
            await hireStaff({
                clubId,
                staffId,
                ...contractDetails
            });
            loadStaffData();
            loadAvailableStaff(selectedRole);
            showToast('Staff hired successfully!', 'success');
        } catch (error) {
            console.error('Error hiring staff:', error);
            showToast('Failed to hire staff. Check funds or if staff is already hired.', 'error');
        }
    };

    const handleFireStaff = async (staffId, reason) => {
        if (!window.confirm("Are you sure you want to fire this staff member? You will have to pay a termination fee.")) return;

        try {
            await fireStaff(staffId, { reason });
            loadStaffData();
            showToast('Staff fired successfully.', 'success');
        } catch (error) {
            console.error('Error firing staff:', error);
            showToast('Failed to fire staff.', 'error');
        }
    };

    const getRoleColor = (role) => {
        const colors = {
            HEAD_COACH: '#ff6b35',
            ASSISTANT_COACH: '#f7931e',
            FITNESS_COACH: '#4caf50',
            GOALKEEPING_COACH: '#2196f3',
            YOUTH_COACH: '#9c27b0',
            HEAD_PHYSIO: '#e91e63',
            PHYSIO: '#f06292',
            DOCTOR: '#d32f2f',
            HEAD_SCOUT: '#795548',
            SCOUT: '#8d6e63',
            ANALYST: '#607d8b'
        };
        return colors[role] || '#666';
    };

    const getAbilityStars = (ability) => {
        const stars = Math.ceil(ability / 4); // Convert 1-20 to 1-5 stars
        return '⭐'.repeat(stars) + '☆'.repeat(5 - stars);
    };

    if (loading) return <div>Loading staff...</div>;

    return (
        <div className="staff-management container mt-3">
            <div className="staff-header mb-4">
                <h2>Staff Management</h2>

                {staffBonuses && (
                    <div className="card p-3 mb-3">
                        <h4>Current Staff Bonuses</h4>
                        <div className="row">
                            <div className="col-md-2">
                                <strong>Training:</strong> <br/>
                                <span className="text-success">+{Math.round(staffBonuses.trainingBonus * 100)}%</span>
                            </div>
                            <div className="col-md-2">
                                <strong>Motivation:</strong> <br/>
                                <span className="text-success">+{Math.round(staffBonuses.motivationBonus * 100)}%</span>
                            </div>
                            <div className="col-md-2">
                                <strong>Injury Prevention:</strong> <br/>
                                <span className="text-success">-{Math.round(staffBonuses.injuryPreventionBonus * 100)}%</span>
                            </div>
                            <div className="col-md-2">
                                <strong>Recovery:</strong> <br/>
                                <span className="text-success">+{Math.round(staffBonuses.recoveryBonus * 100)}%</span>
                            </div>
                            <div className="col-md-2">
                                <strong>Scouting:</strong> <br/>
                                <span className="text-success">+{Math.round(staffBonuses.scoutingBonus * 100)}%</span>
                            </div>
                        </div>
                    </div>
                )}
            </div>

            <ul className="nav nav-tabs mb-3">
                <li className="nav-item">
                    <button
                        className={`nav-link ${selectedTab === 'current' ? 'active' : ''}`}
                        onClick={() => setSelectedTab('current')}
                    >
                        Current Staff ({clubStaff.length})
                    </button>
                </li>
                <li className="nav-item">
                    <button
                        className={`nav-link ${selectedTab === 'available' ? 'active' : ''}`}
                        onClick={() => {
                            setSelectedTab('available');
                            if (availableStaff.length === 0) loadAvailableStaff();
                        }}
                    >
                        Available Staff
                    </button>
                </li>
            </ul>

            {selectedTab === 'current' && (
                <div className="current-staff row">
                    {clubStaff.map(staff => (
                        <div key={staff.id} className="col-md-6 col-lg-4 mb-3">
                            <div className="card h-100">
                                <div className="card-header d-flex justify-content-between align-items-center">
                                    <h5 className="mb-0">{staff.name} {staff.surname}</h5>
                                    <span
                                        className="badge"
                                        style={{ backgroundColor: getRoleColor(staff.role), color: 'white' }}
                                    >
                                        {staff.role.replace('_', ' ')}
                                    </span>
                                </div>
                                <div className="card-body">
                                    <p><strong>Age:</strong> {staff.age}</p>
                                    <p><strong>Ability:</strong> {getAbilityStars(staff.ability)}</p>
                                    <p><strong>Reputation:</strong> {getAbilityStars(staff.reputation)}</p>
                                    <p><strong>Experience:</strong> {staff.experience} years</p>
                                    <p><strong>Salary:</strong> ${staff.monthlySalary.toLocaleString()}/month</p>
                                    <p><strong>Specialization:</strong> {staff.specialization?.replace('_', ' ')}</p>

                                    <div className="small mt-2">
                                        <strong>Bonuses:</strong>
                                        {staff.trainingBonus > 0 && <div>Training: +{Math.round(staff.trainingBonus * 100)}%</div>}
                                        {staff.motivationBonus > 0 && <div>Motivation: +{Math.round(staff.motivationBonus * 100)}%</div>}
                                        {staff.injuryPreventionBonus > 0 && <div>Injury Prev: -{Math.round(staff.injuryPreventionBonus * 100)}%</div>}
                                        {staff.recoveryBonus > 0 && <div>Recovery: +{Math.round(staff.recoveryBonus * 100)}%</div>}
                                        {staff.scoutingBonus > 0 && <div>Scouting: +{Math.round(staff.scoutingBonus * 100)}%</div>}
                                    </div>

                                    <div className="mt-3 text-muted small">
                                        Contract until: {new Date(staff.contractEnd).toLocaleDateString()}
                                    </div>
                                </div>
                                <div className="card-footer d-flex justify-content-between">
                                    <button
                                        className="btn btn-sm btn-outline-primary"
                                        onClick={() => showToast("Renew contract logic to be implemented (Modal)", 'info')}
                                    >
                                        Renew
                                    </button>
                                    <button
                                        className="btn btn-sm btn-outline-danger"
                                        onClick={() => handleFireStaff(staff.id, 'Performance issues')}
                                    >
                                        Fire
                                    </button>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {selectedTab === 'available' && (
                <div className="available-staff">
                    <div className="mb-3">
                        <select
                            className="form-select"
                            value={selectedRole}
                            onChange={(e) => {
                                setSelectedRole(e.target.value);
                                loadAvailableStaff(e.target.value);
                            }}
                        >
                            <option value="">All Roles</option>
                            {staffRoles.map(role => (
                                <option key={role} value={role}>
                                    {role.replace('_', ' ')}
                                </option>
                            ))}
                        </select>
                    </div>

                    <div className="row">
                        {availableStaff.map(staff => (
                            <div key={staff.id} className="col-md-6 col-lg-4 mb-3">
                                <div className="card h-100">
                                    <div className="card-header d-flex justify-content-between align-items-center">
                                        <h5 className="mb-0">{staff.name} {staff.surname}</h5>
                                        <span
                                            className="badge"
                                            style={{ backgroundColor: getRoleColor(staff.role), color: 'white' }}
                                        >
                                            {staff.role.replace('_', ' ')}
                                        </span>
                                    </div>
                                    <div className="card-body">
                                        <p><strong>Age:</strong> {staff.age}</p>
                                        <p><strong>Ability:</strong> {getAbilityStars(staff.ability)}</p>
                                        <p><strong>Reputation:</strong> {getAbilityStars(staff.reputation)}</p>
                                        <p><strong>Experience:</strong> {staff.experience} years</p>
                                        <p><strong>Asking Salary:</strong> ${staff.monthlySalary.toLocaleString()}/month</p>
                                        <p className="card-text small"><em>{staff.description}</em></p>
                                    </div>
                                    <div className="card-footer">
                                        <button
                                            className="btn btn-primary w-100"
                                            onClick={() => handleHireStaff(staff.id, {
                                                contractYears: 2,
                                                signingBonus: 0,
                                                performanceBonus: 0
                                            })}
                                        >
                                            Hire (2 Years)
                                        </button>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
};

export default StaffManagement;
