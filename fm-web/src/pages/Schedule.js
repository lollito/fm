import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import Layout from '../components/Layout';

const Schedule = () => {
  const [schedule, setSchedule] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    api.get('/match/').then(res => setSchedule(res.data));
  }, []);

  // Group matches by round
  const groupedMatches = schedule.reduce((acc, match) => {
    const round = match.roundNumber || 0;
    if (!acc[round]) {
      acc[round] = [];
    }
    acc[round].push(match);
    return acc;
  }, {});

  // Sort rounds keys numerically
  const rounds = Object.keys(groupedMatches).sort((a, b) => Number(a) - Number(b));

  const formatDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return new Intl.DateTimeFormat('it-IT', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    }).format(date);
  };

  return (
    <Layout>
      <div className="mt-2">
        <h1>Schedule</h1>
        {rounds.map(round => (
          <div key={round} className="mb-4">
            <h3 className="p-2 border-bottom" style={{ backgroundColor: '#f8f9fa' }}>
              Matchday {round}
            </h3>
            <table className="table table-hover">
              <thead>
                <tr>
                  <th style={{ width: '25%' }}>Date</th>
                  <th style={{ width: '30%', textAlign: 'right' }}>Home</th>
                  <th style={{ width: '15%', textAlign: 'center' }}>Score</th>
                  <th style={{ width: '30%', textAlign: 'left' }}>Away</th>
                </tr>
              </thead>
              <tbody>
                {groupedMatches[round].map((match) => {
                   const isPlayed = match.finish || match.status === 'COMPLETED';
                   const homeBold = isPlayed && match.homeScore > match.awayScore;
                   const awayBold = isPlayed && match.awayScore > match.homeScore;

                   return (
                    <tr
                      key={match.id}
                      onClick={() => navigate(`/match/${match.id}`)}
                      style={{ cursor: 'pointer' }}
                    >
                      <td>{formatDate(match.date)}</td>
                      <td style={{
                        textAlign: 'right',
                        fontWeight: homeBold ? 'bold' : 'normal',
                        color: homeBold ? '#28a745' : 'inherit'
                      }}>
                        {match.home.name}
                      </td>
                      <td style={{ textAlign: 'center' }}>
                        {isPlayed ? `${match.homeScore} - ${match.awayScore}` : '-'}
                      </td>
                      <td style={{
                        textAlign: 'left',
                        fontWeight: awayBold ? 'bold' : 'normal',
                        color: awayBold ? '#28a745' : 'inherit'
                      }}>
                        {match.away.name}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        ))}
      </div>
    </Layout>
  );
};

export default Schedule;
