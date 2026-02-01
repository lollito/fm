import React, { useState, useEffect, useContext } from 'react';
import api from '../services/api';
import { AuthContext } from '../context/AuthContext';
import Layout from '../components/Layout';

const Home = () => {
  const [news, setNews] = useState([]);
  const [nextMatches, setNextMatches] = useState([]);
  const [previousMatches, setPreviousMatches] = useState([]);
  const [ranking, setRanking] = useState([]);
  const { user } = useContext(AuthContext);

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
    return user?.club?.name === rowUserClubName ? 'bg-info' : '';
  };

  return (
    <Layout>
      <h1 className="mt-2">Home</h1>
      <div className="row">
        <div className="col-sm-12">
          <div className="card text-white bg-warning mb-3">
            <div className="card-header">News</div>
            <div className="card-body">
              <table className="table table-sm text-white">
                <thead>
                  <tr>
                    <th>Date</th>
                    <th>News</th>
                  </tr>
                </thead>
                <tbody>
                  {news.map((n, i) => (
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
        <div className="col-sm-6">
          <div className="card text-white bg-primary mb-3">
            <div className="card-header">Next Round</div>
            <div className="card-body">
              <table className="table table-sm text-white">
                <thead>
                  <tr>
                    <th>Home</th>
                    <th>Away</th>
                    <th>Date</th>
                  </tr>
                </thead>
                <tbody>
                  {nextMatches.map((m, i) => (
                    <tr key={i} className={getRowClass(m.home.name) || getRowClass(m.away.name)}>
                      <td>{m.home.name}</td>
                      <td>{m.away.name}</td>
                      <td>{m.date}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
          <div className="card text-white bg-primary mb-3">
            <div className="card-header">Last Match Results</div>
            <div className="card-body">
              <table className="table table-sm text-white">
                <thead>
                  <tr>
                    <th>Home</th>
                    <th>Away</th>
                    <th>Score</th>
                  </tr>
                </thead>
                <tbody>
                  {previousMatches.map((m, i) => (
                    <tr key={i} className={getRowClass(m.home.name) || getRowClass(m.away.name)}>
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
        <div className="col-sm-6">
          <div className="card text-white bg-primary mb-3">
            <div className="card-header">Ranking</div>
            <div className="card-body">
              <table className="table table-sm text-white">
                <thead>
                  <tr>
                    <th>Club</th>
                    <th>Played</th>
                    <th>Points</th>
                  </tr>
                </thead>
                <tbody>
                  {ranking.map((r, i) => (
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
