import React, { useState, useEffect } from 'react';
import api from '../services/api';
import Layout from '../components/Layout';
import useSortableData from '../hooks/useSortableData';

const Schedule = () => {
  const [schedule, setSchedule] = useState([]);
  const { items: sortedSchedule, requestSort, sortConfig } = useSortableData(schedule);

  useEffect(() => {
    api.get('/match/').then(res => setSchedule(res.data));
  }, []);

  const getSortIcon = (key) => {
    if (!sortConfig || sortConfig.key !== key) return <i className="fas fa-sort" style={{ marginLeft: '5px', opacity: 0.3 }}></i>;
    return sortConfig.direction === 'ascending' ?
        <i className="fas fa-sort-up" style={{ marginLeft: '5px' }}></i> :
        <i className="fas fa-sort-down" style={{ marginLeft: '5px' }}></i>;
  };

  return (
    <Layout>
      <h1 className="mt-2">Schedule</h1>
      <table className="table table-striped">
        <thead>
          <tr>
            <th onClick={() => requestSort('home.name')} style={{ cursor: 'pointer' }}>Home {getSortIcon('home.name')}</th>
            <th onClick={() => requestSort('away.name')} style={{ cursor: 'pointer' }}>Away {getSortIcon('away.name')}</th>
            <th onClick={() => requestSort('homeScore')} style={{ cursor: 'pointer' }}>Score {getSortIcon('homeScore')}</th>
            <th onClick={() => requestSort('date')} style={{ cursor: 'pointer' }}>Date {getSortIcon('date')}</th>
          </tr>
        </thead>
        <tbody>
          {sortedSchedule.map((m, i) => (
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
