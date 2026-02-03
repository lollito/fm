import React, { useState, useEffect, useContext, useCallback } from 'react';
import { Link } from 'react-router-dom';
import api from '../services/api';
import Layout from '../components/Layout';
import { AuthContext } from '../context/AuthContext';
import useSortableData from '../hooks/useSortableData';

const UpcomingMatches = () => {
  const [matches, setMatches] = useState([]);
  const [filteredMatches, setFilteredMatches] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState({ type: 'all', location: 'all' });
  const [sortBy, setSortBy] = useState('dateAsc');
  const { items: sortedMatches, requestSort, sortConfig } = useSortableData(filteredMatches);
  const { user } = useContext(AuthContext);
  const [countdown, setCountdown] = useState('');

  const fetchUpcomingMatches = useCallback(async () => {
    try {
      const res = await api.get('/match/upcoming');
      setMatches(res.data);
      setFilteredMatches(res.data);
    } catch (error) {
      console.error('Error fetching upcoming matches', error);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchUpcomingMatches();
  }, [fetchUpcomingMatches]);

  const parseDate = (dateStr) => {
    if (!dateStr) return new Date();
    // pattern = "dd-MM-yyyy HH:mm"
    const [datePart, timePart] = dateStr.split(' ');
    const [day, month, year] = datePart.split('-');
    const [hours, minutes] = timePart.split(':');
    return new Date(year, month - 1, day, hours, minutes);
  };

  useEffect(() => {
    if (matches.length > 0) {
      const nextMatch = matches[0];
      const matchDate = parseDate(nextMatch.date);

      const interval = setInterval(() => {
        const now = new Date();
        const diff = matchDate - now;

        if (diff <= 0) {
          setCountdown('Started');
          clearInterval(interval);
        } else {
          const days = Math.floor(diff / (1000 * 60 * 60 * 24));
          const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
          const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
          const seconds = Math.floor((diff % (1000 * 60)) / 1000);

          let countdownStr = '';
          if (days > 0) countdownStr += days + 'd ';
          if (hours > 0 || days > 0) countdownStr += hours + 'h ';
          countdownStr += minutes + 'm ' + seconds + 's';
          setCountdown(countdownStr);
        }
      }, 1000);

      return () => clearInterval(interval);
    }
  }, [matches]);

  useEffect(() => {
    let result = [...matches];

    // Filter by competition type (if we had multiple)
    if (filter.type !== 'all') {
      result = result.filter(m => m.competitionName === filter.type);
    }

    // Filter by location
    if (filter.location === 'home') {
      result = result.filter(m => m.home.id === user?.club?.id);
    } else if (filter.location === 'away') {
      result = result.filter(m => m.away.id === user?.club?.id);
    }

    // Sort
    if (sortBy === 'dateAsc') {
      result.sort((a, b) => parseDate(a.date) - parseDate(b.date));
    } else if (sortBy === 'dateDesc') {
      result.sort((a, b) => parseDate(b.date) - parseDate(a.date));
    }

    setFilteredMatches(result);
  }, [matches, filter, sortBy, user]);

  const isHome = (match) => match.home.id === user?.club?.id;
  const getOpponent = (match) => isHome(match) ? match.away : match.home;

  const getSortIcon = (key) => {
    if (!sortConfig || sortConfig.key !== key) return <i className="fas fa-sort" style={{ marginLeft: '5px', opacity: 0.3 }}></i>;
    return sortConfig.direction === 'ascending' ?
        <i className="fas fa-sort-up" style={{ marginLeft: '5px' }}></i> :
        <i className="fas fa-sort-down" style={{ marginLeft: '5px' }}></i>;
  };

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

  const nextMatch = matches.length > 0 ? matches[0] : null;

  return (
    <Layout>
      <div className="container-fluid">
        <h1 className="mt-4">Prossime Partite</h1>

        {nextMatch && (
          <div className="row mt-4">
            <div className="col-12">
              <div className="card shadow-sm border-warning">
                <div className="card-header bg-warning text-dark d-flex justify-content-between align-items-center">
                  <h5 className="mb-0">⚡ PROSSIMA PARTITA</h5>
                  <span className="badge badge-dark">{nextMatch.competitionName}</span>
                </div>
                <div className="card-body">
                  <div className="row align-items-center text-center">
                    <div className="col-md-4">
                      <div className="d-flex flex-column align-items-center">
                        {nextMatch.home.logoURL ? (
                          <img src={nextMatch.home.logoURL} alt={nextMatch.home.name} style={{ width: '80px', height: '80px', objectFit: 'contain' }} className="mb-2" />
                        ) : (
                          <div style={{ width: '80px', height: '80px', display: 'flex', alignItems: 'center', justifyContent: 'center', backgroundColor: '#eee', borderRadius: '50%' }} className="mb-2">
                            <i className="fas fa-shield-alt fa-3x text-muted"></i>
                          </div>
                        )}
                        <h4>{nextMatch.home.name}</h4>
                      </div>
                    </div>
                    <div className="col-md-4">
                      <div className="display-4 mb-2">VS</div>
                      <div className="h5 text-muted">{nextMatch.date}</div>
                      <div className="mt-3">
                        <span className="h4 text-danger font-weight-bold">⏱️ {countdown}</span>
                      </div>
                    </div>
                    <div className="col-md-4">
                      <div className="d-flex flex-column align-items-center">
                        {nextMatch.away.logoURL ? (
                          <img src={nextMatch.away.logoURL} alt={nextMatch.away.name} style={{ width: '80px', height: '80px', objectFit: 'contain' }} className="mb-2" />
                        ) : (
                          <div style={{ width: '80px', height: '80px', display: 'flex', alignItems: 'center', justifyContent: 'center', backgroundColor: '#eee', borderRadius: '50%' }} className="mb-2">
                            <i className="fas fa-shield-alt fa-3x text-muted"></i>
                          </div>
                        )}
                        <h4>{nextMatch.away.name}</h4>
                      </div>
                    </div>
                  </div>
                  <hr />
                  <div className="d-flex justify-content-between align-items-center">
                    <div>
                      <i className="fas fa-map-marker-alt mr-2"></i> {nextMatch.stadiumName}
                    </div>
                    <div>
                      <Link to="/formation" className="btn btn-primary mr-2">Vedi Formazione</Link>
                      <button className="btn btn-outline-secondary">Dettagli</button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        <div className="row mt-4 mb-2">
          <div className="col-md-3">
            <label>Competizione</label>
            <select className="form-control" value={filter.type} onChange={(e) => setFilter({ ...filter, type: e.target.value })}>
              <option value="all">Tutte</option>
              {Array.from(new Set(matches.map(m => m.competitionName))).map(c => (
                <option key={c} value={c}>{c}</option>
              ))}
            </select>
          </div>
          <div className="col-md-3">
            <label>Casa/Trasferta</label>
            <select className="form-control" value={filter.location} onChange={(e) => setFilter({ ...filter, location: e.target.value })}>
              <option value="all">Tutte</option>
              <option value="home">Casa</option>
              <option value="away">Trasferta</option>
            </select>
          </div>
          <div className="col-md-3">
            <label>Ordina per</label>
            <select className="form-control" value={sortBy} onChange={(e) => setSortBy(e.target.value)}>
              <option value="dateAsc">Data (Crescente)</option>
              <option value="dateDesc">Data (Decrescente)</option>
            </select>
          </div>
        </div>

        <div className="row mt-2">
          <div className="col-12">
            <div className="card shadow-sm">
              <div className="card-body p-0">
                <div className="table-responsive">
                  <table className="table table-hover mb-0">
                    <thead className="thead-light">
                      <tr>
                        <th onClick={() => requestSort('date')} style={{ cursor: 'pointer' }}>Data {getSortIcon('date')}</th>
                        <th onClick={() => requestSort('away.name')} style={{ cursor: 'pointer' }}>Opponente {getSortIcon('away.name')}</th>
                        <th onClick={() => requestSort('competitionName')} style={{ cursor: 'pointer' }} className="d-none d-md-table-cell">Competizione {getSortIcon('competitionName')}</th>
                        <th onClick={() => requestSort('stadiumName')} style={{ cursor: 'pointer' }} className="d-none d-md-table-cell">Sito {getSortIcon('stadiumName')}</th>
                        <th>Azioni</th>
                      </tr>
                    </thead>
                    <tbody>
                      {sortedMatches.length === 0 ? (
                        <tr><td colSpan="5" className="text-center py-4">Nessuna partita trovata con questi filtri.</td></tr>
                      ) : (
                        sortedMatches.map((m) => {
                          const opponent = getOpponent(m);
                          const home = isHome(m);
                          return (
                            <tr key={m.id}>
                              <td className="align-middle">{m.date}</td>
                              <td className="align-middle">
                                <div className="d-flex align-items-center">
                                  {opponent.logoURL ? (
                                    <img src={opponent.logoURL} alt={opponent.name} style={{ width: '30px', height: '30px', objectFit: 'contain' }} className="mr-2" />
                                  ) : (
                                    <i className="fas fa-shield-alt text-muted mr-2"></i>
                                  )}
                                  <span>
                                    {home ? 'vs ' : '@ '}
                                    <strong>{opponent.name}</strong>
                                    <span className="badge badge-light ml-2">{home ? 'Casa' : 'Trasferta'}</span>
                                  </span>
                                </div>
                              </td>
                              <td className="align-middle d-none d-md-table-cell">{m.competitionName}</td>
                              <td className="align-middle d-none d-md-table-cell">{m.stadiumName}</td>
                              <td className="align-middle">
                                <Link to="/formation" className="btn btn-sm btn-outline-primary mr-2">Formazione</Link>
                                <button className="btn btn-sm btn-outline-secondary d-none d-sm-inline-block">Dettagli</button>
                              </td>
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

export default UpcomingMatches;
