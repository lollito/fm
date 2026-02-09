import React, { useEffect, useRef, useState } from 'react';

const ConfirmationModal = ({ isOpen, onClose, onConfirm, title, message }) => {
    const cancelButtonRef = useRef(null);
    const [isConfirming, setIsConfirming] = useState(false);
    const isMounted = useRef(false);

    useEffect(() => {
        isMounted.current = true;
        return () => { isMounted.current = false; };
    }, []);

    useEffect(() => {
        if (isOpen) {
            // Focus cancel button for safety when modal opens
            if (cancelButtonRef.current) {
                cancelButtonRef.current.focus();
            }

            const handleEscape = (e) => {
                if (e.key === 'Escape' && !isConfirming) {
                    onClose();
                }
            };

            document.addEventListener('keydown', handleEscape);
            return () => {
                document.removeEventListener('keydown', handleEscape);
            };
        }
    }, [isOpen, onClose, isConfirming]);

    const handleConfirm = async () => {
        if (isConfirming) return;

        setIsConfirming(true);
        try {
            await onConfirm();
        } catch (error) {
            console.error('Confirmation action failed', error);
        } finally {
            if (isMounted.current) {
                setIsConfirming(false);
            }
        }
    };

    if (!isOpen) return null;

    return (
        <div
            className="modal-overlay"
            role="alertdialog"
            aria-modal="true"
            aria-labelledby="modal-title"
            aria-describedby="modal-message"
        >
            <div className="modal-content">
                <h3 id="modal-title" className="mb-3">{title}</h3>
                <p id="modal-message" className="mb-3">{message}</p>
                <div className="d-flex justify-content-end gap-2 mt-4">
                    <button
                        ref={cancelButtonRef}
                        className="btn btn-outline"
                        onClick={onClose}
                        disabled={isConfirming}
                    >
                        Cancel
                    </button>
                    <button
                        className="btn btn-primary"
                        onClick={handleConfirm}
                        disabled={isConfirming}
                    >
                        {isConfirming ? (
                            <>
                                <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                                <span className="visually-hidden">Loading...</span>
                                Confirming...
                            </>
                        ) : (
                            'Confirm'
                        )}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default ConfirmationModal;
