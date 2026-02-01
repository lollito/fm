import React, { useState, useEffect } from 'react';
import api from '../services/api';
import Layout from '../components/Layout';

const Stadium = () => {
  const [stadium, setStadium] = useState(null);

  useEffect(() => {
    api.get('/stadium/').then(res => setStadium(res.data));
  }, []);

  if (!stadium) return <Layout>Loading...</Layout>;

  return (
    <Layout>
      <h1 className="mt-2">Stadium: {stadium.name}</h1>
      <div className="row">
        <div className="col-md-6">
          <div className="card mb-3">
            <div className="card-header">Capacity</div>
            <div className="card-body">
              <p>Total Capacity: {stadium.capacity}</p>
            </div>
          </div>
        </div>
      </div>
    </Layout>
  );
};

export default Stadium;
