/**
 * @jest-environment jsdom
 */
import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import ConfirmationModal from './ConfirmationModal';

describe('ConfirmationModal Component', () => {
    const defaultProps = {
        isOpen: true,
        onClose: jest.fn(),
        onConfirm: jest.fn(),
        title: 'Delete Item',
        message: 'Are you sure?'
    };

    beforeEach(() => {
        jest.clearAllMocks();
    });

    test('renders correctly when open', () => {
        render(<ConfirmationModal {...defaultProps} />);

        expect(screen.getByRole('alertdialog')).toBeInTheDocument();
        expect(screen.getByText('Delete Item')).toBeInTheDocument();
        expect(screen.getByText('Are you sure?')).toBeInTheDocument();
        expect(screen.getByRole('button', { name: /cancel/i })).toBeInTheDocument();
        expect(screen.getByRole('button', { name: /confirm/i })).toBeInTheDocument();
    });

    test('does not render when closed', () => {
        render(<ConfirmationModal {...defaultProps} isOpen={false} />);

        expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument();
    });

    test('calls onClose when Cancel is clicked', () => {
        render(<ConfirmationModal {...defaultProps} />);

        fireEvent.click(screen.getByRole('button', { name: /cancel/i }));
        expect(defaultProps.onClose).toHaveBeenCalledTimes(1);
    });

    test('calls onConfirm and shows loading state when Confirm is clicked', async () => {
        let resolveConfirm;
        const onConfirmPromise = new Promise(resolve => {
            resolveConfirm = resolve;
        });
        const onConfirmMock = jest.fn(() => onConfirmPromise);

        render(<ConfirmationModal {...defaultProps} onConfirm={onConfirmMock} />);

        fireEvent.click(screen.getByRole('button', { name: /confirm/i }));

        expect(onConfirmMock).toHaveBeenCalledTimes(1);
        expect(screen.getByRole('button', { name: /processing/i })).toBeInTheDocument();
        expect(screen.getByRole('button', { name: /processing/i })).toBeDisabled();

        // Resolve the promise
        resolveConfirm();

        await waitFor(() => {
            expect(defaultProps.onClose).toHaveBeenCalledTimes(1);
        });
    });

    test('focuses Cancel button on mount', () => {
        render(<ConfirmationModal {...defaultProps} />);

        expect(screen.getByRole('button', { name: /cancel/i })).toHaveFocus();
    });
});
