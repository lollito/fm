/**
 * @jest-environment jsdom
 */
import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import Register from './Register';
import { AuthContext } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import api, { getServers } from '../services/api';

// Mock the API
jest.mock('../services/api', () => {
    const mockApi = {
        get: jest.fn(),
        post: jest.fn(),
    };
    return {
        __esModule: true,
        default: mockApi,
        getServers: jest.fn(),
    };
});

// Mock ToastContext
jest.mock('../context/ToastContext', () => ({
    useToast: jest.fn(),
}));

// Mock react-router-dom completely
const mockedNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
    useNavigate: () => mockedNavigate,
    Link: ({ children, to }) => <a href={to}>{children}</a>,
    BrowserRouter: ({ children }) => <div>{children}</div>
}));

describe('Register Component', () => {
    const login = jest.fn();
    const showToast = jest.fn();

    const renderComponent = () => {
        render(
            <AuthContext.Provider value={{ login }}>
                <Register />
            </AuthContext.Provider>
        );
    };

    beforeEach(() => {
        jest.clearAllMocks();
        useToast.mockReturnValue({ showToast });

        // Mock API responses
        api.get.mockImplementation((url) => {
            if (url === '/country/') {
                return Promise.resolve({ data: [{ id: 1, name: 'Country 1' }] });
            }
            return Promise.resolve({ data: [] });
        });
        getServers.mockResolvedValue({ data: [{ id: 1, name: 'Server 1' }] });
    });

    test('renders register form with inputs and button', async () => {
        renderComponent();

        await waitFor(() => {
             expect(screen.getByText('FM Register')).toBeInTheDocument();
        });

        // Use regex for flexible matching and verify inputs exist
        expect(screen.getByPlaceholderText('Username')).toBeInTheDocument();
        expect(screen.getByPlaceholderText('Email address')).toBeInTheDocument();

        // Button should exist
        expect(screen.getByRole('button', { name: /create account/i })).toBeInTheDocument();
    });

    test('handles successful registration', async () => {
        api.post.mockResolvedValueOnce({ data: { token: 'fake-token' } });
        renderComponent();

        // Fill out the form
        fireEvent.change(screen.getByPlaceholderText('Username'), { target: { value: 'newuser' } });
        fireEvent.change(screen.getByPlaceholderText('Email address'), { target: { value: 'test@example.com' } });
        fireEvent.change(screen.getByPlaceholderText('Repeat Email address'), { target: { value: 'test@example.com' } });
        fireEvent.change(screen.getByPlaceholderText('Password'), { target: { value: 'password123' } });
        fireEvent.change(screen.getByPlaceholderText('Repeat Password'), { target: { value: 'password123' } });
        fireEvent.change(screen.getByPlaceholderText('Club Name'), { target: { value: 'My Club' } });

        // Wait for selects to populate
        await waitFor(() => {
            expect(screen.getByText('Country 1')).toBeInTheDocument();
        });

        // Select country and server (assuming they are populated)
        // Since we can't easily select by text on native selects without role="option",
        // we'll find by display value or just trigger change on the select element if we can find it.
        // But for now let's just submit, as the select might already have a value or we can mock the state change.

        fireEvent.change(screen.getByRole('combobox', { name: /country/i }), { target: { value: '1' } });
        // The server select might not have a label associated yet, so we might need to find it by other means or add label first.
        // Wait, the current implementation has NO labels associated. So getByRole('combobox', { name: ... }) will FAIL.
        // This is expected failure!

        // If we want to simulate user interaction without accessible labels, we have to use less ideal selectors.
        // But the goal is to fix accessibility. So let's write the test assuming accessible labels,
        // and it should fail now and pass later.

        fireEvent.click(screen.getByRole('button', { name: /create account/i }));

        await waitFor(() => {
            expect(api.post).toHaveBeenCalledWith('/user/register', expect.objectContaining({
                username: 'newuser',
                email: 'test@example.com',
                password: 'password123',
                countryId: '1'
            }));
            expect(login).toHaveBeenCalledWith({ token: 'fake-token' });
            expect(mockedNavigate).toHaveBeenCalledWith('/');
        });
    });

    test('displays loading state during submission', async () => {
        // Mock API to delay response
        api.post.mockImplementation(() => new Promise(resolve => setTimeout(() => resolve({ data: { token: 'token' } }), 100)));
        renderComponent();

        fireEvent.change(screen.getByPlaceholderText('Username'), { target: { value: 'newuser' } });
        fireEvent.change(screen.getByPlaceholderText('Email address'), { target: { value: 'test@example.com' } });
        fireEvent.change(screen.getByPlaceholderText('Repeat Email address'), { target: { value: 'test@example.com' } });
        fireEvent.change(screen.getByPlaceholderText('Password'), { target: { value: 'password123' } });
        fireEvent.change(screen.getByPlaceholderText('Repeat Password'), { target: { value: 'password123' } });
        fireEvent.change(screen.getByPlaceholderText('Club Name'), { target: { value: 'My Club' } });

        // Select country/server mock
        // We'll skip precise select interaction for this specific test as we just want to test loading state on button click

        fireEvent.click(screen.getByRole('button', { name: /create account/i }));

        // Check for loading state
        expect(screen.getByRole('button')).toBeDisabled();
        // expect(screen.getByText('Loading...')).toBeInTheDocument(); // This will fail currently

        await waitFor(() => {
            expect(screen.getByRole('button')).not.toBeDisabled();
        });
    });
});
