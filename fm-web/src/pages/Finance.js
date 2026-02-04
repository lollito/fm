import React, { useContext, useState } from 'react';
import Layout from '../components/Layout';
import FinancialDashboard from '../components/FinancialDashboard';
import SponsorshipDashboard from '../components/SponsorshipDashboard';
import { AuthContext } from '../context/AuthContext';

const Finance = () => {
  const { user } = useContext(AuthContext);
  const [activeTab, setActiveTab] = useState('overview');

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

      <ul className="nav nav-tabs mb-3">
        <li className="nav-item">
          <button
            className={'nav-link ' + (activeTab === 'overview' ? 'active' : '')}
            onClick={() => setActiveTab('overview')}
          >
            Overview
          </button>
        </li>
        <li className="nav-item">
          <button
            className={'nav-link ' + (activeTab === 'sponsorship' ? 'active' : '')}
            onClick={() => setActiveTab('sponsorship')}
          >
            Sponsorship
          </button>
        </li>
      </ul>

      {activeTab === 'overview' && <FinancialDashboard clubId={user.clubId} />}
      {activeTab === 'sponsorship' && <SponsorshipDashboard clubId={user.clubId} />}
    </Layout>
  );
};

export default Finance;
