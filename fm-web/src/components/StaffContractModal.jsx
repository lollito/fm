import React, { useState, useEffect } from 'react';

const StaffContractModal = ({ isOpen, onClose, onSubmit, staff, mode }) => {
    const [years, setYears] = useState(2);
    const [salary, setSalary] = useState(0);
    const [signingBonus, setSigningBonus] = useState(0);
    const [performanceBonus, setPerformanceBonus] = useState(0);
    const [error, setError] = useState('');

    useEffect(() => {
        if (staff) {
            setSalary(staff.monthlySalary || 0);
            setYears(2);
            setSigningBonus(0);
            setPerformanceBonus(0);
            setError('');
        }
    }, [staff, isOpen]);

    if (!isOpen || !staff) return null;

    const handleSubmit = () => {
        if (years < 1) {
            setError('Contract length must be at least 1 year.');
            return;
        }
        if (mode === 'RENEW' && salary < 0) {
            setError('Salary cannot be negative.');
            return;
        }

        const payload = {
            contractYears: parseInt(years),
            signingBonus: parseFloat(signingBonus),
            performanceBonus: parseFloat(performanceBonus)
        };

        if (mode === 'RENEW') {
            payload.newSalary = parseFloat(salary);
        }

        onSubmit(staff.id, payload);
        onClose();
    };

    const isHire = mode === 'HIRE';
    const title = isHire ? `Hire ${staff.name} ${staff.surname}` : `Renew Contract: ${staff.name} ${staff.surname}`;

    return (
        <div className="modal-overlay" style={{
            position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
            backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1050
        }}>
            <div className="modal-content bg-white p-4 rounded shadow-lg" style={{ maxWidth: '500px', width: '100%' }}>
                <div className="d-flex justify-content-between align-items-center mb-3">
                    <h4 className="mb-0">{title}</h4>
                    <button type="button" className="btn-close" onClick={onClose}></button>
                </div>

                {error && <div className="alert alert-danger">{error}</div>}

                <div className="mb-3">
                    <label className="form-label">Contract Length (Years)</label>
                    <input
                        type="number"
                        className="form-control"
                        value={years}
                        onChange={(e) => setYears(e.target.value)}
                        min="1"
                        max="5"
                    />
                </div>

                <div className="mb-3">
                    <label className="form-label">Monthly Salary ($)</label>
                    <input
                        type="number"
                        className="form-control"
                        value={salary}
                        onChange={(e) => setSalary(e.target.value)}
                        disabled={isHire}
                        readOnly={isHire}
                    />
                    {isHire && <small className="text-muted">Asking salary is fixed for initial hiring.</small>}
                </div>

                <div className="mb-3">
                    <label className="form-label">Signing Bonus ($)</label>
                    <input
                        type="number"
                        className="form-control"
                        value={signingBonus}
                        onChange={(e) => setSigningBonus(e.target.value)}
                        min="0"
                    />
                </div>

                <div className="mb-3">
                    <label className="form-label">Performance Bonus ($)</label>
                    <input
                        type="number"
                        className="form-control"
                        value={performanceBonus}
                        onChange={(e) => setPerformanceBonus(e.target.value)}
                        min="0"
                    />
                </div>

                <div className="d-flex justify-content-end gap-2 mt-4">
                    <button className="btn btn-outline-secondary" onClick={onClose}>Cancel</button>
                    <button className="btn btn-primary" onClick={handleSubmit}>
                        {isHire ? 'Offer Contract' : 'Renew Contract'}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default StaffContractModal;
