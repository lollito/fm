import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import ConfirmationModal from './ConfirmationModal';
import '@testing-library/jest-dom';

describe('ConfirmationModal', () => {
    const mockOnClose = jest.fn();
    const mockOnConfirm = jest.fn();
    const defaultProps = {
        isOpen: true,
        onClose: mockOnClose,
        onConfirm: mockOnConfirm,
        title: 'Test Title',
        message: 'Test Message'
    };

    beforeEach(() => {
        jest.clearAllMocks();
    });

    test('renders nothing when isOpen is false', () => {
        render(<ConfirmationModal {...defaultProps} isOpen={false} />);
        expect(screen.queryByText('Test Title')).not.toBeInTheDocument();
    });

    test('renders correctly when isOpen is true', () => {
        render(<ConfirmationModal {...defaultProps} />);
        expect(screen.getByText('Test Title')).toBeInTheDocument();
        expect(screen.getByText('Test Message')).toBeInTheDocument();
        expect(screen.getByRole('button', { name: /cancel/i })).toBeInTheDocument();
        expect(screen.getByRole('button', { name: /confirm/i })).toBeInTheDocument();
    });

    test('has correct accessibility attributes', () => {
        render(<ConfirmationModal {...defaultProps} />);
        // This will fail until role="alertdialog" is added
        const modal = screen.getByRole('alertdialog');
        expect(modal).toBeInTheDocument();
        expect(modal).toHaveAttribute('aria-modal', 'true');
        // We expect ids to be added for these
        expect(modal).toHaveAttribute('aria-labelledby', 'modal-title');
        expect(modal).toHaveAttribute('aria-describedby', 'modal-message');
    });

    test('calls onClose when Cancel is clicked', () => {
        render(<ConfirmationModal {...defaultProps} />);
        fireEvent.click(screen.getByRole('button', { name: /cancel/i }));
        expect(mockOnClose).toHaveBeenCalledTimes(1);
    });

    test('calls onConfirm when Confirm is clicked', () => {
        render(<ConfirmationModal {...defaultProps} />);
        fireEvent.click(screen.getByRole('button', { name: /confirm/i }));
        expect(mockOnConfirm).toHaveBeenCalledTimes(1);
    });

    test('closes on Escape key', () => {
        render(<ConfirmationModal {...defaultProps} />);
        fireEvent.keyDown(document, { key: 'Escape' });
        expect(mockOnClose).toHaveBeenCalledTimes(1);
    });

    test('focuses Cancel button on open', () => {
        render(<ConfirmationModal {...defaultProps} />);
        expect(screen.getByRole('button', { name: /cancel/i })).toHaveFocus();
    });

    test('shows loading state during async confirmation', async () => {
        let resolvePromise;
        const asyncConfirm = jest.fn(() => new Promise((resolve) => {
            resolvePromise = resolve;
        }));

        render(<ConfirmationModal {...defaultProps} onConfirm={asyncConfirm} />);

        const confirmBtn = screen.getByRole('button', { name: /confirm/i });
        fireEvent.click(confirmBtn);

        // Should show loading spinner and be disabled
        expect(asyncConfirm).toHaveBeenCalled();
        expect(confirmBtn).toBeDisabled();
        expect(screen.getByRole('button', { name: /cancel/i })).toBeDisabled();
        // Check for spinner class
        expect(confirmBtn.querySelector('.spinner-border-sm')).toBeInTheDocument();

        // Finish promise
        resolvePromise();

        await waitFor(() => expect(confirmBtn).not.toBeDisabled());
    });
});
