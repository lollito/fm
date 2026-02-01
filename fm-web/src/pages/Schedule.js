import React, { useState, useEffect } from 'react';
import api from '../services/api';
import Layout from '../components/Layout';

const Schedule = () => {
  const [schedule, setSchedule] = useState([]);

  useEffect(() => {
    // Assuming there's an endpoint or I should use match/next/previous
    // The original schedule.html used /api/match/
    api.get('/match/').then(res => setSchedule(res.data));
  }, []);

  return (
    <Layout>
      <h1 className="mt-2">Schedule</h1>
      <table className="table table-striped">
        <thead>
          <tr>
            <th>Home</th>
            <th>Away</th>
            <th>Score</th>
            <th>Date</th>
          </tr>
        </thead>
        <tbody>
          {schedule.map((m, i) => (
            <tr key={i}>
              <td>{m.home.name}</td>
              <td>{m.away.name}</td>
              <td>{m.homeScore} - {m.awayScore}</td>
              <td>{m.date}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </Layout>
  );
};

export default Schedule;
