import React, { useState, useEffect } from 'react';
import { getClubStaff, getAvailableStaff, hireStaff, fireStaff, getStaffBonuses, renewContract } from '../services/api';
import { useToast } from '../context/ToastContext';
import StaffContractModal from './StaffContractModal';
import ConfirmationModal from './ConfirmationModal';

const BonusCard = ({ title, value, icon, isNegativeGood = false }) => {
    const percentage = Math.round(value * 100);
    const isGood = isNegativeGood ? percentage < 0 : percentage > 0;

    return (
        <div className="col-6 col-md-4 col-lg-2">
            <div className="p-3 border rounded bg-light h-100 d-flex flex-column align-items-center justify-content-center">
                <div className="text-muted mb-1"><i className={`fas ${icon} fa-lg`}></i></div>
                <small className="text-uppercase fw-bold text-muted mb-1" style={{ fontSize: '0.75rem' }}>{title}</small>
                <h5 className={`mb-0 ${isGood || percentage > 0 ? 'text-success' : 'text-secondary'}`}>
                    {isNegativeGood ? '-' : '+'}{percentage}%
                </h5>
            </div>
        </div>
    );
};

const StaffManagement = ({ clubId }) => {
    const [clubStaff, setClubStaff] = useState([]);
    const [availableStaff, setAvailableStaff] = useState([]);
    const [staffBonuses, setStaffBonuses] = useState(null);
    const [selectedTab, setSelectedTab] = useState('current');
    const [selectedRole, setSelectedRole] = useState('');
    const [loading, setLoading] = useState(true);

    // Modal states
    const [showHireModal, setShowHireModal] = useState(false);
    const [showRenewModal, setShowRenewModal] = useState(false);
    const [showFireModal, setShowFireModal] = useState(false);
    const [selectedStaff, setSelectedStaff] = useState(null);

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
            showToast('Error loading staff data', 'error');
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
            showToast('Error loading available staff', 'error');
        }
    };

    const handleHireClick = (staff) => {
        setSelectedStaff(staff);
        setShowHireModal(true);
    };

    const handleRenewClick = (staff) => {
        setSelectedStaff(staff);
        setShowRenewModal(true);
    };

    const handleFireClick = (staff) => {
        setSelectedStaff(staff);
        setShowFireModal(true);
    };

    const handleHireSubmit = async (staffId, contractDetails) => {
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

    const handleRenewSubmit = async (staffId, contractDetails) => {
        try {
            await renewContract(staffId, contractDetails);
            loadStaffData();
            showToast('Contract renewed successfully!', 'success');
        } catch (error) {
            console.error('Error renewing contract:', error);
            showToast('Failed to renew contract.', 'error');
        }
    };

    const handleFireConfirm = async () => {
        if (!selectedStaff) return;
        try {
            await fireStaff(selectedStaff.id, { reason: 'Contract Termination' });
            loadStaffData();
            showToast('Staff fired successfully.', 'success');
            setShowFireModal(false);
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
        return colors[role] || '#6c757d';
    };

    const getAbilityStars = (ability) => {
        const stars = Math.ceil(ability / 4);
        return <span className="text-warning">{'⭐'.repeat(stars)}<span className="text-muted">{'☆'.repeat(5 - stars)}</span></span>;
    };

    if (loading) return (
        <div className="d-flex justify-content-center align-items-center p-5">
            <div className="spinner-border text-primary" role="status">
                <span className="visually-hidden">Loading...</span>
            </div>
        </div>
    );

    return (
        <div className="staff-management container-fluid mt-3">
            <div className="staff-header mb-4">
                <h3 className="mb-3">Staff Management</h3>

                {staffBonuses && (
                    <div className="card border-0 shadow-sm mb-4">
                        <div className="card-body">
                             <div className="row g-3 justify-content-center">
                                <BonusCard title="Training" value={staffBonuses.trainingBonus} icon="fa-dumbbell" />
                                <BonusCard title="Motivation" value={staffBonuses.motivationBonus} icon="fa-fire" />
                                <BonusCard title="Injury Prev." value={staffBonuses.injuryPreventionBonus} icon="fa-user-shield" isNegativeGood={true} />
                                <BonusCard title="Recovery" value={staffBonuses.recoveryBonus} icon="fa-first-aid" />
                                <BonusCard title="Scouting" value={staffBonuses.scoutingBonus} icon="fa-binoculars" />
                            </div>
                        </div>
                    </div>
                )}
            </div>

            <ul className="nav nav-pills mb-4">
                <li className="nav-item">
                    <button
                        className={`nav-link ${selectedTab === 'current' ? 'active' : ''}`}
                        onClick={() => setSelectedTab('current')}
                    >
                        Current Staff <span className="badge bg-white text-primary ms-1">{clubStaff.length}</span>
                    </button>
                </li>
                <li className="nav-item ms-2">
                    <button
                        className={`nav-link ${selectedTab === 'available' ? 'active' : ''}`}
                        onClick={() => {
                            setSelectedTab('available');
                            if (availableStaff.length === 0) loadAvailableStaff();
                        }}
                    >
                        Available Market
                    </button>
                </li>
            </ul>

            {selectedTab === 'current' && (
                <div className="current-staff row g-4">
                    {clubStaff.length === 0 && (
                        <div className="col-12 text-center text-muted py-5">
                            <h4>No staff members found.</h4>
                            <p>Go to the market to hire new staff.</p>
                        </div>
                    )}
                    {clubStaff.map(staff => (
                        <div key={staff.id} className="col-md-6 col-lg-4 col-xl-3">
                            <div className="card h-100 shadow-sm border-0 staff-card">
                                <div className="card-header bg-white border-bottom-0 pt-3 px-3 d-flex justify-content-between align-items-start">
                                    <div>
                                        <h5 className="card-title mb-0 fw-bold">{staff.name} {staff.surname}</h5>
                                        <small className="text-muted">{staff.nationalityName}</small>
                                    </div>
                                    <span
                                        className="badge rounded-pill"
                                        style={{ backgroundColor: getRoleColor(staff.role), color: 'white' }}
                                    >
                                        {staff.role.replace(/_/g, ' ')}
                                    </span>
                                </div>
                                <div className="card-body px-3 py-2">
                                    <div className="d-flex justify-content-between mb-2">
                                        <span className="text-muted">Age</span>
                                        <span className="fw-bold">{staff.age}</span>
                                    </div>
                                    <div className="d-flex justify-content-between mb-2">
                                        <span className="text-muted">Ability</span>
                                        <span>{getAbilityStars(staff.ability)}</span>
                                    </div>
                                    <div className="d-flex justify-content-between mb-2">
                                        <span className="text-muted">Reputation</span>
                                        <span>{getAbilityStars(staff.reputation)}</span>
                                    </div>
                                    <div className="d-flex justify-content-between mb-2">
                                        <span className="text-muted">Experience</span>
                                        <span className="fw-bold">{staff.experience} yrs</span>
                                    </div>
                                    <div className="d-flex justify-content-between mb-2">
                                        <span className="text-muted">Salary</span>
                                        <span className="fw-bold text-success">${staff.monthlySalary.toLocaleString()}/mo</span>
                                    </div>
                                     <div className="mt-3 pt-2 border-top">
                                        <small className="d-block text-muted mb-1">Active Contract</small>
                                        <div className="d-flex align-items-center text-dark">
                                            <i className="far fa-calendar-alt me-2 text-primary"></i>
                                            {new Date(staff.contractEnd).toLocaleDateString()}
                                        </div>
                                    </div>
                                </div>
                                <div className="card-footer bg-white border-top-0 pb-3 px-3 d-flex gap-2">
                                    <button
                                        className="btn btn-outline-primary flex-grow-1 btn-sm"
                                        onClick={() => handleRenewClick(staff)}
                                    >
                                        <i className="fas fa-file-signature me-1"></i> Renew
                                    </button>
                                    <button
                                        className="btn btn-outline-danger flex-grow-1 btn-sm"
                                        onClick={() => handleFireClick(staff)}
                                    >
                                        <i className="fas fa-user-times me-1"></i> Fire
                                    </button>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {selectedTab === 'available' && (
                <div className="available-staff">
                    <div className="row mb-4">
                        <div className="col-md-4">
                            <select
                                className="form-select shadow-sm"
                                value={selectedRole}
                                onChange={(e) => {
                                    setSelectedRole(e.target.value);
                                    loadAvailableStaff(e.target.value);
                                }}
                            >
                                <option value="">Filter by Role</option>
                                {staffRoles.map(role => (
                                    <option key={role} value={role}>
                                        {role.replace(/_/g, ' ')}
                                    </option>
                                ))}
                            </select>
                        </div>
                    </div>

                    <div className="row g-4">
                        {availableStaff.length === 0 && (
                            <div className="col-12 text-center text-muted py-5">
                                <h4>No available staff found.</h4>
                                <p>Try changing the filter or come back later.</p>
                            </div>
                        )}
                        {availableStaff.map(staff => (
                            <div key={staff.id} className="col-md-6 col-lg-4 col-xl-3">
                                <div className="card h-100 shadow-sm border-0 staff-card">
                                    <div className="card-header bg-white border-bottom-0 pt-3 px-3 d-flex justify-content-between align-items-start">
                                        <div>
                                            <h5 className="card-title mb-0 fw-bold">{staff.name} {staff.surname}</h5>
                                            <small className="text-muted">{staff.nationalityName}</small>
                                        </div>
                                        <span
                                            className="badge rounded-pill"
                                            style={{ backgroundColor: getRoleColor(staff.role), color: 'white' }}
                                        >
                                            {staff.role.replace(/_/g, ' ')}
                                        </span>
                                    </div>
                                    <div className="card-body px-3 py-2">
                                        <div className="d-flex justify-content-between mb-2">
                                            <span className="text-muted">Age</span>
                                            <span className="fw-bold">{staff.age}</span>
                                        </div>
                                        <div className="d-flex justify-content-between mb-2">
                                            <span className="text-muted">Ability</span>
                                            <span>{getAbilityStars(staff.ability)}</span>
                                        </div>
                                        <div className="d-flex justify-content-between mb-2">
                                            <span className="text-muted">Reputation</span>
                                            <span>{getAbilityStars(staff.reputation)}</span>
                                        </div>
                                        <div className="d-flex justify-content-between mb-2">
                                            <span className="text-muted">Experience</span>
                                            <span className="fw-bold">{staff.experience} yrs</span>
                                        </div>
                                        <div className="d-flex justify-content-between mb-2">
                                            <span className="text-muted">Asking</span>
                                            <span className="fw-bold text-success">${staff.monthlySalary.toLocaleString()}/mo</span>
                                        </div>
                                        <p className="card-text small text-muted mt-3 fst-italic border-top pt-2">
                                            "{staff.description}"
                                        </p>
                                    </div>
                                    <div className="card-footer bg-white border-top-0 pb-3 px-3">
                                        <button
                                            className="btn btn-primary w-100"
                                            onClick={() => handleHireClick(staff)}
                                        >
                                            <i className="fas fa-handshake me-2"></i>Offer Contract
                                        </button>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            )}

            <StaffContractModal
                isOpen={showHireModal}
                onClose={() => setShowHireModal(false)}
                onSubmit={handleHireSubmit}
                staff={selectedStaff}
                mode="HIRE"
            />

            <StaffContractModal
                isOpen={showRenewModal}
                onClose={() => setShowRenewModal(false)}
                onSubmit={handleRenewSubmit}
                staff={selectedStaff}
                mode="RENEW"
            />

            <ConfirmationModal
                isOpen={showFireModal}
                onClose={() => setShowFireModal(false)}
                onConfirm={handleFireConfirm}
                title="Terminate Contract"
                message={`Are you sure you want to fire ${selectedStaff?.name} ${selectedStaff?.surname}? You will have to pay a termination fee.`}
            />
        </div>
    );
};

export default StaffManagement;
