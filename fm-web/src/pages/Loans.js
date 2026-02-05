import React, { useContext } from 'react';
import Layout from '../components/Layout';
import LoanManager from '../components/loans/LoanManager';
import { AuthContext } from '../context/AuthContext';

const Loans = () => {
    const { user } = useContext(AuthContext);

    return (
        <Layout>
            <h1>Loans</h1>
            {user && user.clubId ? (
                <LoanManager clubId={user.clubId} />
            ) : (
                <div>Please log in to manage loans.</div>
            )}
        </Layout>
    );
};

export default Loans;
