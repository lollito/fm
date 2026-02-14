import React from 'react';
import { render, screen, act, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import Toast from './Toast';

jest.useFakeTimers();

describe('Toast Component', () => {
    const defaultProps = {
        id: 'test-toast',
        message: 'Test Message',
        type: 'info',
        duration: 3000,
        onRemove: jest.fn(),
    };

    afterEach(() => {
        jest.clearAllMocks();
        jest.clearAllTimers();
    });

    test('renders with correct message', () => {
        render(<Toast {...defaultProps} />);
        expect(screen.getByText('Test Message')).toBeInTheDocument();
    });

    test('applies success style based on type', () => {
        const { container } = render(<Toast {...defaultProps} type="success" />);
        // Check for success color (green) or icon class
        // Current implementation uses fa-check-circle for success
        expect(container.querySelector('.fa-check-circle')).toBeInTheDocument();
    });

    test('calls onRemove after duration (auto-dismissal)', () => {
        render(<Toast {...defaultProps} />);

        // Fast-forward time to duration
        act(() => {
            jest.advanceTimersByTime(defaultProps.duration);
        });

        expect(defaultProps.onRemove).toHaveBeenCalledWith(defaultProps.id);
    });

    test('sets isExiting state before removal', () => {
        const { container } = render(<Toast {...defaultProps} />);

        // Before duration - 300ms, should have 'enter' class (or at least not 'exit')
        // Note: The component uses `isExiting ? 'exit' : 'enter'`
        expect(container.firstChild).toHaveClass('enter');

        // Advance to just before removal animation starts (e.g. duration - 300)
        act(() => {
            jest.advanceTimersByTime(defaultProps.duration - 300);
        });

        // Should now have 'exit' class
        expect(container.firstChild).toHaveClass('exit');
    });

    test('calls onRemove when clicked', () => {
        const { container } = render(<Toast {...defaultProps} />);

        fireEvent.click(container.firstChild);

        // Wait for removal animation (300ms)
        act(() => {
            jest.advanceTimersByTime(300);
        });

        expect(defaultProps.onRemove).toHaveBeenCalledWith(defaultProps.id);
    });

    test('calls onRemove when close button is clicked', () => {
        render(<Toast {...defaultProps} />);

        const closeButton = screen.getByRole('button');
        fireEvent.click(closeButton);

        // Wait for removal animation (300ms)
        act(() => {
            jest.advanceTimersByTime(300);
        });

        expect(defaultProps.onRemove).toHaveBeenCalledWith(defaultProps.id);
    });
});
