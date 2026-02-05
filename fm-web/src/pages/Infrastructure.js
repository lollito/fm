import React, { useContext } from 'react';
import Layout from '../components/Layout';
import InfrastructureDashboard from '../components/infrastructure/InfrastructureDashboard';
import { AuthContext } from '../context/AuthContext';

const Infrastructure = () => {
  const { user } = useContext(AuthContext);

  if (!user || !user.clubId) return <div>Loading...</div>;

  return (
    <Layout>
      <InfrastructureDashboard clubId={user.clubId} />
    </Layout>
  );
};

export default Infrastructure;
