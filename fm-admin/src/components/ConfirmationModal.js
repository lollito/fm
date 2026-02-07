import React from 'react';
import './ConfirmationModal.css';

const ConfirmationModal = ({ isOpen, onClose, onConfirm, title, message }) => {
    if (!isOpen) return null;

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal-content" onClick={e => e.stopPropagation()}>
                <h3 className="mb-3" style={{ color: 'var(--text-main)' }}>{title}</h3>
                <p className="mb-3" style={{ color: 'var(--text-muted)' }}>{message}</p>
                <div className="modal-actions">
                    <button
                        className="btn btn-secondary"
                        onClick={onClose}
                        style={{ backgroundColor: 'transparent', border: '1px solid var(--border-color)', color: 'var(--text-main)' }}
                    >
                        Cancel
                    </button>
                    <button className="btn btn-primary" onClick={() => { onConfirm(); onClose(); }}>
                        Confirm
                    </button>
                </div>
            </div>
        </div>
    );
};

export default ConfirmationModal;
