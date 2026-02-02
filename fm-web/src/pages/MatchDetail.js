import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import api from '../services/api';
import Layout from '../components/Layout';
import { Radar, RadarChart, PolarGrid, PolarAngleAxis, ResponsiveContainer, Legend } from 'recharts';

const MatchDetail = () => {
    const { id } = useParams();
    const [match, setMatch] = useState(null);
    const [activeTab, setActiveTab] = useState('summary');

    useEffect(() => {
        api.get(`/match/${id}`).then(res => setMatch(res.data));
    }, [id]);

    if (!match) return <Layout><div className="text-center mt-5">Loading match details...</div></Layout>;

    const statsData = [
        { name: 'Possession', home: match.stats.homePossession, away: match.stats.awayPossession },
        { name: 'Shots', home: match.stats.homeShots, away: match.stats.awayShots },
        { name: 'On Target', home: match.stats.homeOnTarget, away: match.stats.awayOnTarget },
        { name: 'Passes', home: match.stats.homePasses || 0, away: match.stats.awayPasses || 0 },
        { name: 'Tackles', home: match.stats.homeTackles || 0, away: match.stats.awayTackles || 0 },
        { name: 'Fouls', home: match.stats.homeFouls, away: match.stats.awayFouls },
    ];

    const radarData = statsData.map(s => ({
        subject: s.name,
        A: s.home,
        B: s.away,
        fullMark: Math.max(s.home, s.away, 100)
    }));

    const renderSlot = (player, top, left, label) => {
        return (
            <div
                key={`${label}-${top}-${left}`}
                className="pos"
                style={{ position: 'absolute', top, left, transform: 'translate(-50%, -50%)', width: '40px', height: '40px', fontSize: '0.6rem' }}
            >
                <span className="player-name" style={{ fontSize: '0.6rem', bottom: '-20px' }}>
                    {player ? `${player.surname} ${player.name ? player.name.charAt(0) + '.' : ''}` : label}
                </span>
            </div>
        );
    };

    const getPositions = (formation) => {
        if (!formation || !formation.module) return [];
        const positions = [];
        const mod = formation.module;
        const players = formation.players;

        // GK
        positions.push({ player: players[0], top: '85%', left: '50%', label: 'GK' });
        // DEF
        for (let i = 0; i < (mod.cd || 0); i++)
            positions.push({ player: players[positions.length], top: '70%', left: (20 + (60 / (mod.cd + 1)) * (i + 1)) + '%', label: 'DEF' });
        // WB
        for (let i = 0; i < (mod.wb || 0); i++)
            positions.push({ player: players[positions.length], top: '60%', left: (i % 2 === 0 ? '20%' : '80%'), label: 'WB' });
        // MF
        for (let i = 0; i < (mod.mf || 0); i++)
            positions.push({ player: players[positions.length], top: '45%', left: (20 + (60 / (mod.mf + 1)) * (i + 1)) + '%', label: 'MID' });
        // WNG
        for (let i = 0; i < (mod.wng || 0); i++)
            positions.push({ player: players[positions.length], top: '35%', left: (i % 2 === 0 ? '15%' : '85%'), label: 'WNG' });
        // FW
        for (let i = 0; i < (mod.fw || 0); i++)
            positions.push({ player: players[positions.length], top: '15%', left: (20 + (60 / (mod.fw + 1)) * (i + 1)) + '%', label: 'FWD' });

        return positions;
    };

    const getEventIcon = (type) => {
        switch(type) {
            case 'HAVE_SCORED':
            case 'HAVE_SCORED_FREE_KICK': return <i className="fas fa-futbol text-success"></i>;
            case 'YELLOW_CARD': return <i className="fas fa-square text-warning"></i>;
            case 'RED_CARD': return <i className="fas fa-square text-danger"></i>;
            case 'SUBSTITUTION': return <i className="fas fa-exchange-alt text-info"></i>;
            default: return <i className="fas fa-info-circle text-dim"></i>;
        }
    };

    return (
        <Layout>
            <div className="card text-center mb-4" style={{ background: 'linear-gradient(135deg, var(--bg-card) 0%, var(--bg-darker) 100%)', borderBottom: '4px solid var(--primary)' }}>
                <div className="card-body">
                    <div className="row align-items-center">
                        <div className="col">
                            <h3>{match.home.name}</h3>
                        </div>
                        <div className="col-auto">
                            <h1 style={{ fontSize: '3.5rem', margin: '0 20px' }}>{match.homeScore} - {match.awayScore}</h1>
                        </div>
                        <div className="col">
                            <h3>{match.away.name}</h3>
                        </div>
                    </div>
                    <div className="text-muted mt-2">
                        <i className="far fa-calendar-alt mr-2"></i> {match.date} | <i className="fas fa-users mr-2"></i> {match.spectators} spectators
                    </div>
                </div>
            </div>

            <div className="row mb-3" style={{ padding: '0 12px', gap: '10px' }}>
                <button className={`btn ${activeTab === 'summary' ? 'btn-primary' : 'btn-outline'}`} onClick={() => setActiveTab('summary')}>Summary</button>
                <button className={`btn ${activeTab === 'stats' ? 'btn-primary' : 'btn-outline'}`} onClick={() => setActiveTab('stats')}>Stats</button>
                <button className={`btn ${activeTab === 'lineups' ? 'btn-primary' : 'btn-outline'}`} onClick={() => setActiveTab('lineups')}>Lineups</button>
                <button className={`btn ${activeTab === 'ratings' ? 'btn-primary' : 'btn-outline'}`} onClick={() => setActiveTab('ratings')}>Player Ratings</button>
            </div>

            {activeTab === 'summary' && (
                <div className="card">
                    <div className="card-header">Match Timeline</div>
                    <div className="card-body">
                        <div className="timeline">
                            {match.events.map((e, i) => (
                                <div key={i} className="d-flex align-items-center mb-3 p-2" style={{ borderBottom: '1px solid rgba(255,255,255,0.05)' }}>
                                    <div style={{ width: '50px', fontWeight: 'bold', color: 'var(--primary)' }}>{e.minute}'</div>
                                    <div style={{ width: '30px', textAlign: 'center' }}>{getEventIcon(e.type)}</div>
                                    <div className="ml-3" style={{ color: 'white' }}>{e.event}</div>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>
            )}

            {activeTab === 'stats' && (
                <div className="row">
                    <div className="col-6">
                        <div className="card">
                            <div className="card-header">Performance Comparison</div>
                            <div className="card-body" style={{ height: '400px' }}>
                                <ResponsiveContainer width="100%" height="100%">
                                    <RadarChart cx="50%" cy="50%" outerRadius="80%" data={radarData}>
                                        <PolarGrid stroke="#444" />
                                        <PolarAngleAxis dataKey="subject" tick={{ fill: 'var(--text-dim)', fontSize: 12 }} />
                                        <Radar name={match.home.name} dataKey="A" stroke="var(--primary)" fill="var(--primary)" fillOpacity={0.6} />
                                        <Radar name={match.away.name} dataKey="B" stroke="var(--accent)" fill="var(--accent)" fillOpacity={0.6} />
                                        <Legend />
                                    </RadarChart>
                                </ResponsiveContainer>
                            </div>
                        </div>
                    </div>
                    <div className="col-6">
                        <div className="card">
                            <div className="card-header">Match Statistics</div>
                            <div className="card-body">
                                {statsData.map(s => (
                                    <div key={s.name} className="mb-4">
                                        <div className="d-flex justify-content-between mb-1">
                                            <span>{s.home}</span>
                                            <span className="text-muted">{s.name}</span>
                                            <span>{s.away}</span>
                                        </div>
                                        <div className="attr-bar" style={{ display: 'flex' }}>
                                            <div style={{
                                                width: `${(s.home / (s.home + s.away || 1)) * 100}%`,
                                                backgroundColor: 'var(--primary)',
                                                height: '100%',
                                                borderRadius: '4px 0 0 4px'
                                            }}></div>
                                            <div style={{
                                                width: `${(s.away / (s.home + s.away || 1)) * 100}%`,
                                                backgroundColor: 'var(--accent)',
                                                height: '100%',
                                                borderRadius: '0 4px 4px 0'
                                            }}></div>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>
                    </div>
                </div>
            )}

            {activeTab === 'lineups' && (
                <div className="row">
                    <div className="col-6">
                        <div className="card">
                            <div className="card-header">{match.home.name} Lineup</div>
                            <div className="card-body">
                                <div className="pitch" style={{ height: '400px !important' }}>
                                    {getPositions(match.homeFormation).map(p => renderSlot(p.player, p.top, p.left, p.label))}
                                </div>
                            </div>
                        </div>
                    </div>
                    <div className="col-6">
                        <div className="card">
                            <div className="card-header">{match.away.name} Lineup</div>
                            <div className="card-body">
                                <div className="pitch" style={{ height: '400px !important' }}>
                                    {getPositions(match.awayFormation).map(p => renderSlot(p.player, p.top, p.left, p.label))}
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            )}

            {activeTab === 'ratings' && (
                <div className="card">
                    <div className="card-header">Player Performance Ratings</div>
                    <div className="card-body">
                        <table className="table">
                            <thead>
                                <tr>
                                    <th>Pos</th>
                                    <th>Player</th>
                                    <th>Team</th>
                                    <th>Shots (OT)</th>
                                    <th>Passes</th>
                                    <th>Tackles</th>
                                    <th>Rating</th>
                                </tr>
                            </thead>
                            <tbody>
                                {match.playerStats.sort((a,b) => b.rating - a.rating).map((ps, i) => {
                                    const isHome = match.homeFormation.players.some(p => p.id === ps.player.id);
                                    return (
                                    <tr key={i} style={{ backgroundColor: ps.mvp ? 'rgba(255, 214, 0, 0.1)' : 'transparent' }}>
                                        <td>{ps.position}</td>
                                        <td style={{ color: 'white', fontWeight: 'bold' }}>
                                            {ps.player.surname} {ps.player.name ? ps.player.name.charAt(0) + '.' : ''}
                                            {ps.mvp && <i className="fas fa-star text-warning ml-2" title="MVP"></i>}
                                            {ps.goals > 0 && <span className="ml-2 text-success">⚽ x{ps.goals}</span>}
                                        </td>
                                        <td>{isHome ? 'Home' : 'Away'}</td>
                                        <td>{ps.shots} ({ps.shotsOnTarget})</td>
                                        <td>{ps.completedPasses}/{ps.passes}</td>
                                        <td>{ps.tackles}</td>
                                        <td>
                                            <span style={{
                                                padding: '5px 10px',
                                                borderRadius: '4px',
                                                backgroundColor: ps.rating >= 8 ? 'var(--success)' : ps.rating >= 6 ? 'var(--info)' : 'var(--danger)',
                                                color: 'white'
                                            }}>
                                                {ps.rating.toFixed(1)}
                                            </span>
                                        </td>
                                    </tr>
                                    );
                                })}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}
        </Layout>
    );
};

export default MatchDetail;
