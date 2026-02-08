import React, { useState, useEffect, useRef } from 'react';
import './ConfirmationModal.css';

const ConfirmationModal = ({ isOpen, onClose, onConfirm, title, message }) => {
    const [loading, setLoading] = useState(false);
    const cancelRef = useRef(null);

    // Focus management: focus the cancel button when modal opens
    useEffect(() => {
        if (isOpen && cancelRef.current) {
            cancelRef.current.focus();
        }
    }, [isOpen]);

    if (!isOpen) return null;

    const handleConfirm = async () => {
        setLoading(true);
        try {
            await onConfirm();
        } finally {
            setLoading(false);
            onClose();
        }
    };

    return (
        <div
            className="modal-overlay"
            onClick={loading ? undefined : onClose}
            role="alertdialog"
            aria-modal="true"
            aria-labelledby="modal-title"
            aria-describedby="modal-message"
        >
            <div className="modal-content" onClick={e => e.stopPropagation()}>
                <h3
                    id="modal-title"
                    className="mb-3"
                    style={{ color: 'var(--text-main)' }}
                >
                    {title}
                </h3>
                <p
                    id="modal-message"
                    className="mb-3"
                    style={{ color: 'var(--text-muted)' }}
                >
                    {message}
                </p>
                <div className="modal-actions">
                    <button
                        ref={cancelRef}
                        className="btn btn-secondary"
                        onClick={onClose}
                        disabled={loading}
                        style={{ backgroundColor: 'transparent', border: '1px solid var(--border-color)', color: 'var(--text-main)', opacity: loading ? 0.6 : 1 }}
                    >
                        Cancel
                    </button>
                    <button
                        className="btn btn-primary"
                        onClick={handleConfirm}
                        disabled={loading}
                        aria-busy={loading}
                        style={{ opacity: loading ? 0.8 : 1 }}
                    >
                        {loading && <span className="modal-spinner" aria-hidden="true"></span>}
                        {loading ? 'Processing...' : 'Confirm'}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default ConfirmationModal;
