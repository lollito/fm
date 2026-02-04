import React, { useContext } from 'react';
import Layout from '../components/Layout';
import FinancialDashboard from '../components/FinancialDashboard';
import { AuthContext } from '../context/AuthContext';

const Finance = () => {
  const { user } = useContext(AuthContext);

  if (!user || !user.clubId) {
    return (
      <Layout>
        <div className="alert alert-warning">
          You are not associated with any club.
        </div>
      </Layout>
    );
  }

  return (
    <Layout>
      <h1 className="mt-2">Financial Management</h1>
      <FinancialDashboard clubId={user.clubId} />
    </Layout>
  );
};

export default Finance;
