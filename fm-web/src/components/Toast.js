import React, { useEffect, useState } from 'react';

const Toast = ({ id, message, type, duration, onRemove }) => {
    const [isExiting, setIsExiting] = useState(false);

    useEffect(() => {
        if (duration > 0) {
            const timer = setTimeout(() => {
                setIsExiting(true);
            }, duration - 300); // Start exit animation slightly before removal

            const removeTimer = setTimeout(() => {
                onRemove(id);
            }, duration);

            return () => {
                clearTimeout(timer);
                clearTimeout(removeTimer);
            };
        }
    }, [duration, id, onRemove]);

    const handleRemove = () => {
        setIsExiting(true);
        setTimeout(() => {
            onRemove(id);
        }, 300);
    };

    let backgroundColor = 'var(--bg-card)';
    let borderLeftColor = 'var(--primary)';
    let iconClass = 'fa-info-circle';
    let textColor = 'var(--text-main)';

    switch (type) {
        case 'success':
            borderLeftColor = 'var(--success)';
            iconClass = 'fa-check-circle';
            textColor = 'var(--success)';
            break;
        case 'error':
            borderLeftColor = 'var(--danger)';
            iconClass = 'fa-exclamation-circle';
            textColor = 'var(--danger)';
            break;
        case 'warning':
            borderLeftColor = 'var(--warning)';
            iconClass = 'fa-exclamation-triangle';
            textColor = 'var(--warning)';
            break;
        default:
            borderLeftColor = 'var(--info)';
            break;
    }

    return (
        <div
            className={`toast-item ${isExiting ? 'exit' : 'enter'}`}
            style={{
                backgroundColor: 'var(--bg-card)',
                color: 'var(--text-main)',
                padding: '16px 24px',
                borderRadius: 'var(--border-radius)',
                boxShadow: '0 4px 12px rgba(0,0,0,0.5)',
                marginBottom: '10px',
                display: 'flex',
                alignItems: 'center',
                minWidth: '300px',
                maxWidth: '400px',
                position: 'relative',
                overflow: 'hidden',
                cursor: 'pointer',
                border: '1px solid rgba(161, 85, 255, 0.1)',
                borderLeft: `4px solid ${borderLeftColor}`
            }}
            onClick={handleRemove}
        >
            <i className={`fas ${iconClass}`} style={{ color: textColor, marginRight: '12px', fontSize: '1.2rem' }}></i>
            <span style={{ flexGrow: 1, fontSize: '0.95rem' }}>{message}</span>
            <button
                onClick={(e) => { e.stopPropagation(); handleRemove(); }}
                style={{
                    background: 'transparent',
                    border: 'none',
                    color: 'var(--text-dim)',
                    cursor: 'pointer',
                    fontSize: '1rem',
                    marginLeft: '12px'
                }}
            >
                &times;
            </button>
        </div>
    );
};

export default Toast;
