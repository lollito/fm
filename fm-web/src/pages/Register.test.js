/** @jest-environment jsdom */
import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import Register from './Register';
import { AuthContext } from '../context/AuthContext';
import api, { getServers } from '../services/api';

// Mock API
jest.mock('../services/api', () => ({
    __esModule: true,
    default: {
        get: jest.fn(),
        post: jest.fn(),
    },
    getServers: jest.fn()
}));

// Mock ToastContext
const mockShowToast = jest.fn();
jest.mock('../context/ToastContext', () => ({
    useToast: () => ({
        showToast: mockShowToast,
    }),
}));

// Mock react-router-dom
const mockedNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  useNavigate: () => mockedNavigate,
  Link: ({ children, to }) => <a href={to}>{children}</a>,
  BrowserRouter: ({ children }) => <div>{children}</div>
}));

describe('Register Component', () => {
    const login = jest.fn();

    const renderComponent = () => {
        render(
            <AuthContext.Provider value={{ login }}>
                <Register />
            </AuthContext.Provider>
        );
    };

    beforeEach(() => {
        jest.clearAllMocks();
        // Mock initial data fetch
        api.get.mockResolvedValue({ data: [{ id: 1, name: 'Test Country' }] });
        getServers.mockResolvedValue({ data: [{ id: 1, name: 'Test Server' }] });
    });

    test('renders register form with inputs and button', async () => {
        renderComponent();

        // Wait for countries and servers to load
        await waitFor(() => {
            expect(api.get).toHaveBeenCalledWith('/country/');
            expect(getServers).toHaveBeenCalled();
        });

        expect(screen.getByText('FM Register')).toBeInTheDocument();
        expect(screen.getByLabelText('Username')).toBeInTheDocument(); // Changed to getByLabelText to verify htmlFor
        expect(screen.getByLabelText('Email address')).toBeInTheDocument();
        expect(screen.getByRole('button', { name: /create account/i })).toBeInTheDocument();
    });

    test('handles successful registration', async () => {
        renderComponent();

        await waitFor(() => expect(api.get).toHaveBeenCalled());

        // Fill form
        fireEvent.change(screen.getByLabelText('Username'), { target: { value: 'user' } });
        fireEvent.change(screen.getByLabelText('Email address'), { target: { value: 'test@example.com' } });
        fireEvent.change(screen.getByLabelText('Repeat Email address'), { target: { value: 'test@example.com' } });
        fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'password' } });
        fireEvent.change(screen.getByLabelText('Repeat Password'), { target: { value: 'password' } });
        fireEvent.change(screen.getByLabelText('Club Name'), { target: { value: 'My Club' } });
        fireEvent.change(screen.getByLabelText('Country'), { target: { value: '1' } });
        fireEvent.change(screen.getByLabelText('Server'), { target: { value: '1' } });


        // Mock API response
        api.post.mockResolvedValue({ data: { token: 'fake-token' } });

        fireEvent.click(screen.getByRole('button', { name: /create account/i }));

        await waitFor(() => {
            expect(api.post).toHaveBeenCalled();
            expect(login).toHaveBeenCalledWith({ token: 'fake-token' });
            expect(mockedNavigate).toHaveBeenCalledWith('/');
        });
    });

    test('shows loading state during submission', async () => {
        // Mock API to delay response
        api.post.mockImplementation(() => new Promise(resolve => setTimeout(() => resolve({ data: { token: 'token' } }), 100)));
        renderComponent();

        await waitFor(() => expect(api.get).toHaveBeenCalled());

        // Fill form
        fireEvent.change(screen.getByLabelText('Username'), { target: { value: 'user' } });
        fireEvent.change(screen.getByLabelText('Email address'), { target: { value: 'test@example.com' } });
        fireEvent.change(screen.getByLabelText('Repeat Email address'), { target: { value: 'test@example.com' } });
        fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'password' } });
        fireEvent.change(screen.getByLabelText('Repeat Password'), { target: { value: 'password' } });
        fireEvent.change(screen.getByLabelText('Club Name'), { target: { value: 'My Club' } });
        fireEvent.change(screen.getByLabelText('Country'), { target: { value: '1' } });
        fireEvent.change(screen.getByLabelText('Server'), { target: { value: '1' } });

        fireEvent.click(screen.getByRole('button', { name: /create account/i }));

        expect(screen.getByRole('button')).toBeDisabled();
        expect(screen.getByText('Creating Account...')).toBeInTheDocument();
        expect(screen.getByLabelText('Username')).toBeDisabled(); // Check input disabled

        await waitFor(() => {
            expect(screen.getByRole('button')).not.toBeDisabled();
            expect(screen.getByText('Create Account')).toBeInTheDocument();
        });
    });
});
