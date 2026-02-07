/**
 * @jest-environment jsdom
 */
import React from 'react';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom';
import StaffManagement from './StaffManagement';
import * as api from '../services/api';

// Mock the API service
jest.mock('../services/api');

// Mock Toast Context
jest.mock('../context/ToastContext', () => ({
    useToast: () => ({ showToast: jest.fn() })
}));

describe('StaffManagement Component', () => {
    const mockClubId = 1;

    const mockStaffList = [
        {
            id: 1,
            name: 'John',
            surname: 'Doe',
            role: 'HEAD_COACH',
            age: 45,
            ability: 18,
            reputation: 15,
            experience: 10,
            monthlySalary: 50000,
            contractEnd: '2025-06-30',
            nationalityName: 'England',
            trainingBonus: 0.1,
            motivationBonus: 0.05
        }
    ];

    const mockBonuses = {
        trainingBonus: 0.15,
        motivationBonus: 0.10,
        injuryPreventionBonus: 0.05,
        recoveryBonus: 0.08,
        scoutingBonus: 0.12
    };

    beforeEach(() => {
        jest.clearAllMocks();
        api.getClubStaff.mockResolvedValue({ data: mockStaffList });
        api.getStaffBonuses.mockResolvedValue({ data: mockBonuses });
        api.getAvailableStaff.mockResolvedValue({ data: [] });
    });

    test('renders loading state initially', async () => {
        api.getClubStaff.mockReturnValue(new Promise(() => {}));
        render(<StaffManagement clubId={mockClubId} />);
        expect(screen.getByRole('status')).toBeInTheDocument();
    });

    test('renders staff list and bonuses after loading', async () => {
        render(<StaffManagement clubId={mockClubId} />);

        await waitFor(() => {
            expect(screen.getByText('John Doe')).toBeInTheDocument();
        });

        // Check bonuses
        expect(screen.getByText('Training')).toBeInTheDocument();
        expect(screen.getByText('+15%')).toBeInTheDocument();

        // Check staff details
        expect(screen.getByText('HEAD COACH')).toBeInTheDocument();
        expect(screen.getByText('England')).toBeInTheDocument();
        expect(screen.getByText('$50,000/mo')).toBeInTheDocument();
    });

    test('opens renew modal when Renew button is clicked', async () => {
        render(<StaffManagement clubId={mockClubId} />);

        await waitFor(() => {
            expect(screen.getByText('John Doe')).toBeInTheDocument();
        });

        const renewBtn = screen.getByText('Renew');
        fireEvent.click(renewBtn);

        expect(screen.getByText('Renew Contract: John Doe')).toBeInTheDocument();
    });

    test('opens fire modal when Fire button is clicked', async () => {
        render(<StaffManagement clubId={mockClubId} />);

        await waitFor(() => {
            expect(screen.getByText('John Doe')).toBeInTheDocument();
        });

        const fireBtn = screen.getByText('Fire');
        fireEvent.click(fireBtn);

        expect(screen.getByText('Terminate Contract')).toBeInTheDocument();
        expect(screen.getByText(/Are you sure you want to fire John Doe/)).toBeInTheDocument();
    });

    test('switches to Available Staff tab', async () => {
        render(<StaffManagement clubId={mockClubId} />);

        await waitFor(() => {
            expect(screen.getByText('Current Staff')).toBeInTheDocument();
        });

        const availableTab = screen.getByText('Available Market');
        fireEvent.click(availableTab);

        expect(api.getAvailableStaff).toHaveBeenCalled();
        expect(screen.getByText('No available staff found.')).toBeInTheDocument();
    });
});
