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
          <div key={round} className="card mb-4">
            <div className="card-header">
              Matchday {round}
            </div>
            <div className="card-body p-0">
              <table className="table table-hover mb-0">
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
                        <td
                          className={homeBold ? 'text-success' : ''}
                          style={{
                            textAlign: 'right',
                            fontWeight: homeBold ? 'bold' : 'normal'
                          }}
                        >
                          {match.home.name}
                        </td>
                        <td style={{ textAlign: 'center' }}>
                          {isPlayed ? `${match.homeScore} - ${match.awayScore}` : '-'}
                        </td>
                        <td
                          className={awayBold ? 'text-success' : ''}
                          style={{
                            textAlign: 'left',
                            fontWeight: awayBold ? 'bold' : 'normal'
                          }}
                        >
                          {match.away.name}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          </div>
        ))}
      </div>
    </Layout>
  );
};

export default Schedule;
