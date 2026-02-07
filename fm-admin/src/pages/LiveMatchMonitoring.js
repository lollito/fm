import React, { useState, useEffect } from 'react';
import { getAllLiveMatches, forceFinishMatch, resetMatch } from '../services/api';
import Layout from '../components/Layout';

const LiveMatchMonitoring = () => {
    const [matches, setMatches] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const fetchMatches = async () => {
        try {
            // Don't set loading to true on refresh to avoid flickering
            // setLoading(true);
            const response = await getAllLiveMatches();
            setMatches(response.data);
            setError(null);
        } catch (err) {
            console.error("Error fetching live matches", err);
            setError("Failed to load live matches.");
        } finally {
            setLoading(false);
        }
    };

    const handleForceFinish = async (matchId) => {
        if (window.confirm('Are you sure you want to force finish this match?')) {
            try {
                await forceFinishMatch(matchId);
                fetchMatches();
            } catch (err) {
                console.error("Error finishing match", err);
                alert("Failed to force finish match.");
            }
        }
    };

    const handleReset = async (matchId) => {
        if (window.confirm('Are you sure you want to reset this match?')) {
            try {
                await resetMatch(matchId);
                fetchMatches();
            } catch (err) {
                console.error("Error resetting match", err);
                alert("Failed to reset match.");
            }
        }
    };

    useEffect(() => {
        // Initial load
        setLoading(true);
        fetchMatches();

        const interval = setInterval(fetchMatches, 10000); // Auto refresh every 10s
        return () => clearInterval(interval);
    }, []);

    const liveMatches = matches.filter(m => !m.finished);
    const finishedMatches = matches.filter(m => m.finished);

    if (loading && matches.length === 0) return (
        <Layout>
            <div className="p-4">Loading...</div>
        </Layout>
    );

    return (
        <Layout>
            <div className="container-fluid p-4">
                <h1 className="h3 mb-4 text-gray-800">Live Match Monitoring</h1>

                {error && <div className="alert alert-danger">{error}</div>}

                <div className="card shadow mb-4">
                    <div className="card-header py-3">
                        <h6 className="m-0 font-weight-bold text-primary">Live Matches ({liveMatches.length})</h6>
                    </div>
                    <div className="card-body">
                        <MatchTable
                            matches={liveMatches}
                            live={true}
                            onForceFinish={handleForceFinish}
                            onReset={handleReset}
                        />
                    </div>
                </div>

                <div className="card shadow mb-4">
                    <div className="card-header py-3">
                        <h6 className="m-0 font-weight-bold text-success">Finished Matches ({finishedMatches.length})</h6>
                    </div>
                    <div className="card-body">
                        <MatchTable
                            matches={finishedMatches}
                            live={false}
                            onForceFinish={handleForceFinish}
                            onReset={handleReset}
                        />
                    </div>
                </div>
            </div>
        </Layout>
    );
};

const MatchTable = ({ matches, live, onForceFinish, onReset }) => {
    if (matches.length === 0) return <p>No matches found.</p>;

    return (
        <div className="table-responsive">
            <table className="table table-bordered" width="100%" cellSpacing="0">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Start Time</th>
                        <th>Home</th>
                        <th>Score</th>
                        <th>Away</th>
                        <th>Minute</th>
                        <th>Status</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    {matches.map(match => (
                        <tr key={match.sessionId}>
                            <td>{match.matchId}</td>
                            <td>{new Date(match.startTime).toLocaleString()}</td>
                            <td className="font-weight-bold">{match.homeTeamName}</td>
                            <td className="text-center">{match.homeScore} - {match.awayScore}</td>
                            <td className="font-weight-bold">{match.awayTeamName}</td>
                            <td>{match.currentMinute}'</td>
                            <td>
                                {live ? (
                                    <span className="badge badge-danger">LIVE</span>
                                ) : (
                                    <span className="badge badge-secondary">Finished</span>
                                )}
                            </td>
                            <td>
                                {live && (
                                    <button
                                        className="btn btn-sm btn-warning mr-2"
                                        onClick={() => onForceFinish(match.matchId)}
                                        style={{ marginRight: '5px' }}
                                    >
                                        Finish
                                    </button>
                                )}
                                <button
                                    className="btn btn-sm btn-danger"
                                    onClick={() => onReset(match.matchId)}
                                >
                                    Reset
                                </button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
};

export default LiveMatchMonitoring;
