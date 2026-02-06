import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import Layout from '../components/Layout';
import TeamLink from '../components/TeamLink';

const LiveMatches = () => {
  const [liveMatches, setLiveMatches] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  const fetchLiveMatches = useCallback(async () => {
    try {
      const res = await api.get('/live-match/current');
      setLiveMatches(res.data);
    } catch (error) {
      console.error('Error fetching live matches', error);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchLiveMatches();
    const interval = setInterval(fetchLiveMatches, 10000); // Refresh every 10 seconds
    return () => clearInterval(interval);
  }, [fetchLiveMatches]);

  if (loading) {
    return (
      <Layout>
        <div className="text-center mt-5">
          <div className="spinner-border" role="status">
            <span className="sr-only">Loading...</span>
          </div>
        </div>
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="container-fluid">
        <h1 className="mt-4">Partite in Corso</h1>

        <div className="row mt-4">
          <div className="col-12">
            <div className="card shadow-sm">
              <div className="card-body p-0">
                <div className="table-responsive">
                  <table className="table table-hover mb-0">
                    <thead className="thead-light">
                      <tr>
                        <th>Minuto</th>
                        <th>Casa</th>
                        <th>Risultato</th>
                        <th>Trasferta</th>
                        <th>Stadio</th>
                      </tr>
                    </thead>
                    <tbody>
                      {liveMatches.length === 0 ? (
                        <tr><td colSpan="5" className="text-center py-4">Nessuna partita live al momento.</td></tr>
                      ) : (
                        liveMatches.map((lm) => {
                          const { match, homeScore, awayScore, currentMinute } = lm;
                          return (
                            <tr
                              key={match.id}
                              onClick={() => navigate(`/match/live/${match.id}`)}
                              style={{ cursor: 'pointer' }}
                            >
                              <td className="align-middle text-danger font-weight-bold">
                                {currentMinute}' <span className="spinner-grow spinner-grow-sm text-danger ml-1" role="status" aria-hidden="true"></span>
                              </td>
                              <td className="align-middle">
                                <div className="d-flex align-items-center">
                                  {match.home.logoURL ? (
                                    <img src={match.home.logoURL} alt={match.home.name} style={{ width: '30px', height: '30px', objectFit: 'contain' }} className="mr-2" />
                                  ) : (
                                    <i className="fas fa-shield-alt text-muted mr-2"></i>
                                  )}
                                  <strong><TeamLink club={match.home} /></strong>
                                </div>
                              </td>
                              <td className="align-middle font-weight-bold h5">
                                {homeScore} - {awayScore}
                              </td>
                              <td className="align-middle">
                                <div className="d-flex align-items-center">
                                  {match.away.logoURL ? (
                                    <img src={match.away.logoURL} alt={match.away.name} style={{ width: '30px', height: '30px', objectFit: 'contain' }} className="mr-2" />
                                  ) : (
                                    <i className="fas fa-shield-alt text-muted mr-2"></i>
                                  )}
                                  <strong><TeamLink club={match.away} /></strong>
                                </div>
                              </td>
                              <td className="align-middle text-muted">{match.stadiumName}</td>
                            </tr>
                          );
                        })
                      )}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Layout>
  );
};

export default LiveMatches;
