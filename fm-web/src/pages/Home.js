import React, { useState, useEffect, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import { AuthContext } from '../context/AuthContext';
import Layout from '../components/Layout';
import useSortableData from '../hooks/useSortableData';

const Home = () => {
  const [news, setNews] = useState([]);
  const [nextMatches, setNextMatches] = useState([]);
  const [previousMatches, setPreviousMatches] = useState([]);
  const [ranking, setRanking] = useState([]);
  const { user } = useContext(AuthContext);
  const navigate = useNavigate();

  const { items: sortedNews, requestSort: requestSortNews, sortConfig: sortConfigNews } = useSortableData(news);
  const { items: sortedNextMatches, requestSort: requestSortNext, sortConfig: sortConfigNext } = useSortableData(nextMatches);
  const { items: sortedPreviousMatches, requestSort: requestSortPrev, sortConfig: sortConfigPrev } = useSortableData(previousMatches);
  const { items: sortedRanking, requestSort: requestSortRank, sortConfig: sortConfigRank } = useSortableData(ranking);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [newsRes, nextRes, prevRes, rankRes] = await Promise.all([
          api.get('/news/'),
          api.get('/match/next'),
          api.get('/match/previous'),
          api.get('/ranking/')
        ]);
        setNews(newsRes.data);
        setNextMatches(nextRes.data);
        setPreviousMatches(prevRes.data);
        setRanking(rankRes.data);
      } catch (error) {
        console.error('Error fetching dashboard data', error);
      }
    };
    fetchData();
  }, []);

  const getRowClass = (rowUserClubName) => {
    return user?.club?.name === rowUserClubName ? 'active' : '';
  };

  const getSortIcon = (sortConfig, key) => {
    if (!sortConfig || sortConfig.key !== key) return <i className="fas fa-sort" style={{ marginLeft: '5px', opacity: 0.3 }}></i>;
    return sortConfig.direction === 'ascending' ?
        <i className="fas fa-sort-up" style={{ marginLeft: '5px' }}></i> :
        <i className="fas fa-sort-down" style={{ marginLeft: '5px' }}></i>;
  };

  return (
    <Layout>
      <h1 className="mt-2">Home</h1>
      <div className="row">
        <div className="col-12">
          <div className="card">
            <div className="card-header" style={{ backgroundColor: 'var(--warning)', color: 'black' }}>News</div>
            <div className="card-body">
              <table className="table">
                <thead>
                  <tr>
                    <th onClick={() => requestSortNews('date')} style={{ cursor: 'pointer' }}>
                        Date {getSortIcon(sortConfigNews, 'date')}
                    </th>
                    <th onClick={() => requestSortNews('text')} style={{ cursor: 'pointer' }}>
                        News {getSortIcon(sortConfigNews, 'text')}
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {sortedNews.map((n, i) => (
                    <tr key={i}>
                      <td>{n.date}</td>
                      <td>{n.text}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
      <div className="row">
        <div className="col-6">
          <div className="card">
            <div className="card-header">Next Round</div>
            <div className="card-body">
              <table className="table">
                <thead>
                  <tr>
                    <th onClick={() => requestSortNext('home.name')} style={{ cursor: 'pointer' }}>
                        Home {getSortIcon(sortConfigNext, 'home.name')}
                    </th>
                    <th onClick={() => requestSortNext('away.name')} style={{ cursor: 'pointer' }}>
                        Away {getSortIcon(sortConfigNext, 'away.name')}
                    </th>
                    <th onClick={() => requestSortNext('date')} style={{ cursor: 'pointer' }}>
                        Date {getSortIcon(sortConfigNext, 'date')}
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {sortedNextMatches.map((m, i) => (
                    <tr key={i} className={getRowClass(m.home.name) || getRowClass(m.away.name)}
                        onClick={() => navigate(`/match/${m.id}`)} style={{ cursor: 'pointer' }}>
                      <td>{m.home.name}</td>
                      <td>{m.away.name}</td>
                      <td>{m.date}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
          <div className="card">
            <div className="card-header">Last Match Results</div>
            <div className="card-body">
              <table className="table">
                <thead>
                  <tr>
                    <th onClick={() => requestSortPrev('home.name')} style={{ cursor: 'pointer' }}>
                        Home {getSortIcon(sortConfigPrev, 'home.name')}
                    </th>
                    <th onClick={() => requestSortPrev('away.name')} style={{ cursor: 'pointer' }}>
                        Away {getSortIcon(sortConfigPrev, 'away.name')}
                    </th>
                    <th onClick={() => requestSortPrev('homeScore')} style={{ cursor: 'pointer' }}>
                        Score {getSortIcon(sortConfigPrev, 'homeScore')}
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {sortedPreviousMatches.map((m, i) => (
                    <tr key={i} className={getRowClass(m.home.name) || getRowClass(m.away.name)}
                        onClick={() => navigate(`/match/${m.id}`)} style={{ cursor: 'pointer' }}>
                      <td>{m.home.name}</td>
                      <td>{m.away.name}</td>
                      <td>{m.homeScore} - {m.awayScore}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
        <div className="col-6">
          <div className="card">
            <div className="card-header">Ranking</div>
            <div className="card-body">
              <table className="table">
                <thead>
                  <tr>
                    <th onClick={() => requestSortRank('club.name')} style={{ cursor: 'pointer' }}>
                        Club {getSortIcon(sortConfigRank, 'club.name')}
                    </th>
                    <th onClick={() => requestSortRank('played')} style={{ cursor: 'pointer' }}>
                        Played {getSortIcon(sortConfigRank, 'played')}
                    </th>
                    <th onClick={() => requestSortRank('points')} style={{ cursor: 'pointer' }}>
                        Points {getSortIcon(sortConfigRank, 'points')}
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {sortedRanking.map((r, i) => (
                    <tr key={i} className={getRowClass(r.club.name)}>
                      <td>{r.club.name}</td>
                      <td>{r.played}</td>
                      <td>{r.points}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </Layout>
  );
};

export default Home;
