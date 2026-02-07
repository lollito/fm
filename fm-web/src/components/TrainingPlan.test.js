import React from 'react';
import '@testing-library/jest-dom';
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import TrainingPlan from './TrainingPlan';
import { getTrainingPlan, updateTrainingPlan } from '../services/api';
import { useToast } from '../context/ToastContext';

// Mock dependencies
jest.mock('../services/api');
jest.mock('../context/ToastContext');

describe('TrainingPlan Component', () => {
    const mockShowToast = jest.fn();
    const mockUpdateTrainingPlan = jest.fn();
    const mockGetTrainingPlan = jest.fn();

    beforeEach(() => {
        useToast.mockReturnValue({ showToast: mockShowToast });
        updateTrainingPlan.mockImplementation(mockUpdateTrainingPlan);
        getTrainingPlan.mockImplementation(mockGetTrainingPlan);
        jest.clearAllMocks();
    });

    test('sanitizes payload on save by converting empty strings to null', async () => {
        const initialPlan = {
            id: 1,
            mondayFocus: 'ATTACKING',
            tuesdayFocus: 'DEFENDING',
            wednesdayFocus: 'PHYSICAL',
            thursdayFocus: 'TECHNICAL',
            fridayFocus: 'GOALKEEPING',
            saturdayFocus: 'BALANCED',
            sundayFocus: 'ATTACKING', // Will change this to Rest Day
            intensity: 'MODERATE',
            restOnWeekends: false,
            lastUpdated: '2023-01-01'
        };

        mockGetTrainingPlan.mockResolvedValue({ data: initialPlan });
        // updateTrainingPlan should return the updated plan (or at least valid data)
        mockUpdateTrainingPlan.mockResolvedValue({ data: { ...initialPlan, sundayFocus: null } });

        await act(async () => {
            render(<TrainingPlan teamId={1} />);
        });

        // Wait for loading to finish
        expect(await screen.findByText('Training Plan')).toBeInTheDocument();

        // Find the Sunday dropdown
        // The label is "Sunday"
        // The select is next to it.
        // We can find by combobox role? Or by label text if associated.
        // The current implementation:
        // <label ...>Sunday</label> <select ...>
        // They are siblings, not nested. So getByLabelText won't work automatically unless 'for' attribute is used.
        // I'll use getAllByRole('combobox')[6] (Sunday is last).

        const selects = screen.getAllByRole('combobox');
        const sundaySelect = selects[6]; // Sunday is the 7th day

        // Verify initial value
        expect(sundaySelect.value).toBe('ATTACKING');

        // Change to Rest Day (value "")
        fireEvent.change(sundaySelect, { target: { value: '' } });

        // Click Save
        const saveButton = screen.getByText('Save Training Plan');
        fireEvent.click(saveButton);

        // Verify updateTrainingPlan call
        await waitFor(() => {
            expect(mockUpdateTrainingPlan).toHaveBeenCalledTimes(1);
        });

        const expectedPayload = {
            mondayFocus: 'ATTACKING',
            tuesdayFocus: 'DEFENDING',
            wednesdayFocus: 'PHYSICAL',
            thursdayFocus: 'TECHNICAL',
            fridayFocus: 'GOALKEEPING',
            saturdayFocus: 'BALANCED',
            sundayFocus: null, // This is the crucial check!
            intensity: 'MODERATE',
            restOnWeekends: false
        };

        expect(mockUpdateTrainingPlan).toHaveBeenCalledWith(1, expectedPayload);
    });
});
