import React, { useState, useEffect, useContext } from 'react';
import api from '../services/api';
import Layout from '../components/Layout';
import { AuthContext } from '../context/AuthContext';
import { Link } from 'react-router-dom';

const History = () => {
  const [matches, setMatches] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const { user } = useContext(AuthContext);

  useEffect(() => {
    api.get(`/match/history?page=${page}&size=10`).then(res => {
      setMatches(res.data.content || []);
      setTotalPages(res.data.totalPages || 0);
    });
  }, [page]);

  const getResultInfo = (m) => {
    const isHome = m.home.name === user?.club?.name;
    const userScore = isHome ? m.homeScore : m.awayScore;
    const oppScore = isHome ? m.awayScore : m.homeScore;

    if (userScore > oppScore) return { label: 'VITTORIA', color: '#28a745' };
    if (userScore < oppScore) return { label: 'SCONFITTA', color: '#dc3545' };
    return { label: 'PAREGGIO', color: '#ffc107' };
  };

  return (
    <Layout>
      <h1 className="mt-2">Match History</h1>
      <div className="mt-4">
        {matches.map(m => {
          const res = getResultInfo(m);
          return (
            <div key={m.id} className="card mb-3" style={{ borderLeft: `8px solid ${res.color}` }}>
              <div className="card-body d-flex justify-content-between align-items-center">
                <div>
                  <h5 className="mb-1" style={{ color: res.color }}>{res.label}</h5>
                  <div className="h4 mb-0">
                    {m.home.name} <span style={{ fontWeight: 'bold' }}>{m.homeScore} - {m.awayScore}</span> {m.away.name}
                  </div>
                  <div className="mt-1">
                    <small className="text-muted">
                        <i className="far fa-calendar-alt mr-1"></i> {m.date}
                        {m.round && <span className="ml-3"><i className="fas fa-trophy mr-1"></i> Round {m.round.number}</span>}
                    </small>
                  </div>
                </div>
                <Link to={`/match/${m.id}`} className="btn btn-outline btn-sm">Vedi Dettagli</Link>
              </div>
            </div>
          );
        })}
      </div>
      {totalPages > 1 && (
        <div className="d-flex justify-content-center mt-4 mb-4">
          <nav>
            <ul className="pagination" style={{ display: 'flex', listStyle: 'none', gap: '5px' }}>
              <li className={`page-item ${page === 0 ? 'disabled' : ''}`}>
                <button className="btn btn-outline btn-sm" onClick={() => setPage(page - 1)} disabled={page === 0}>Previous</button>
              </li>
              {[...Array(totalPages).keys()].map(p => (
                <li key={p} className={`page-item ${page === p ? 'active' : ''}`}>
                  <button className={`btn btn-sm ${page === p ? 'btn-primary' : 'btn-outline'}`} onClick={() => setPage(p)}>{p + 1}</button>
                </li>
              ))}
              <li className={`page-item ${page >= totalPages - 1 ? 'disabled' : ''}`}>
                <button className="btn btn-outline btn-sm" onClick={() => setPage(page + 1)} disabled={page >= totalPages - 1}>Next</button>
              </li>
            </ul>
          </nav>
        </div>
      )}
      {matches.length === 0 && (
          <div className="text-center mt-5">
              <h3>No matches played yet.</h3>
          </div>
      )}
    </Layout>
  );
};

export default History;
