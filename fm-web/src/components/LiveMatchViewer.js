import React, { useState, useEffect, useRef } from 'react';
import { useParams } from 'react-router-dom';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { getLiveMatch, joinLiveMatch, leaveLiveMatch, API_BASE_URL } from '../services/api';
import Layout from './Layout';
import '../styles/LiveMatchViewer.css';

const LiveMatchViewer = () => {
    const { matchId } = useParams();
    const [matchSession, setMatchSession] = useState(null);
    const [events, setEvents] = useState([]);
    const [isConnected, setIsConnected] = useState(false);
    const [loading, setLoading] = useState(true);
    const stompClient = useRef(null);
    const eventsEndRef = useRef(null);

    useEffect(() => {
        loadMatchData();
        connectWebSocket();

        return () => {
            disconnectWebSocket();
        };
    }, [matchId]);

    useEffect(() => {
        scrollToBottom();
    }, [events]);

    const loadMatchData = async () => {
        try {
            const response = await getLiveMatch(matchId);
            setMatchSession(response.data);
            setEvents(response.data.events || []);

            // Join as spectator
            await joinLiveMatch(matchId);
        } catch (error) {
            console.error('Error loading match data:', error);
        } finally {
            setLoading(false);
        }
    };

    const connectWebSocket = () => {
        const socket = new SockJS(API_BASE_URL + '/ws/live-match');
        stompClient.current = new Client({
            webSocketFactory: () => socket,
            onConnect: () => {
                setIsConnected(true);

                // Subscribe to match updates
                stompClient.current.subscribe('/topic/match/' + matchId, (message) => {
                    const update = JSON.parse(message.body);
                    setMatchSession(prev => ({
                        ...prev,
                        ...update
                    }));
                });

                // Subscribe to match events
                stompClient.current.subscribe('/topic/match/' + matchId + '/events', (message) => {
                    const event = JSON.parse(message.body);
                    setEvents(prev => [...prev, event]);
                });
            },
            onDisconnect: () => {
                setIsConnected(false);
            },
            onStompError: (frame) => {
                console.error('STOMP error:', frame);
            }
        });

        stompClient.current.activate();
    };

    const disconnectWebSocket = async () => {
        if (stompClient.current) {
            stompClient.current.deactivate();
        }

        try {
            await leaveLiveMatch(matchId);
        } catch (error) {
            console.error('Error leaving match:', error);
        }
    };

    const scrollToBottom = () => {
        eventsEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    };

    const getEventIcon = (eventType) => {
        const icons = {
            GOAL: '⚽',
            YELLOW_CARD: '🟨',
            RED_CARD: '🟥',
            SUBSTITUTION: '🔄',
            CORNER: '📐',
            FREE_KICK: '🦶',
            SAVE: '🥅',
            SHOT_ON_TARGET: '🎯',
            SHOT_OFF_TARGET: '❌',
            FOUL: '⚠️',
            OFFSIDE: '🚩',
            INJURY: '🏥',
            HALF_TIME: '⏸️',
            FULL_TIME: '⏹️',
            KICK_OFF: '⚽'
        };
        return icons[eventType] || '⚪';
    };

    const getEventSeverityClass = (severity) => {
        return 'event-' + (severity ? severity.toLowerCase() : 'normal');
    };

    const formatMatchTime = (minute, additionalTime) => {
        if (additionalTime > 0) {
            return minute + '+' + additionalTime + '\'';
        }
        return minute + '\'';
    };

    const getPhaseDisplay = (phase) => {
        const phases = {
            PRE_MATCH: 'Pre-Match',
            FIRST_HALF: '1st Half',
            HALF_TIME: 'Half Time',
            SECOND_HALF: '2nd Half',
            EXTRA_TIME_FIRST: 'Extra Time 1st',
            EXTRA_TIME_SECOND: 'Extra Time 2nd',
            PENALTIES: 'Penalties',
            FINISHED: 'Full Time'
        };
        return phases[phase] || phase;
    };

    const getIntensityColor = (intensity) => {
        const colors = {
            LOW: '#4caf50',
            MODERATE: '#ff9800',
            HIGH: '#f44336',
            EXTREME: '#9c27b0'
        };
        return colors[intensity] || '#666';
    };

    if (loading) return <Layout><div className="live-match-loading">Loading live match...</div></Layout>;
    if (!matchSession) return <Layout><div className="live-match-error">Match not found</div></Layout>;

    return (
        <Layout>
        <div className="live-match-viewer">
            <div className="match-header">
                <div className="connection-status">
                    <span className={'status-indicator ' + (isConnected ? 'connected' : 'disconnected')}>
                        {isConnected ? '🟢 LIVE' : '🔴 DISCONNECTED'}
                    </span>
                    <span className="spectator-count">👥 {matchSession.spectatorCount} watching</span>
                </div>

                <div className="match-info">
                    <div className="teams">
                        <div className="home-team">
                            <span className="team-name">{matchSession.match.home.name}</span>
                            <span className="team-score">{matchSession.homeScore}</span>
                        </div>
                        <div className="match-time">
                            <div className="time">
                                {formatMatchTime(matchSession.currentMinute, matchSession.additionalTime)}
                            </div>
                            <div className="phase">{getPhaseDisplay(matchSession.currentPhase)}</div>
                        </div>
                        <div className="away-team">
                            <span className="team-score">{matchSession.awayScore}</span>
                            <span className="team-name">{matchSession.match.away.name}</span>
                        </div>
                    </div>
                </div>

                <div className="match-conditions">
                    <div className="weather">
                        🌤️ {matchSession.weatherConditions} {matchSession.temperature}°C
                    </div>
                    <div
                        className="intensity"
                        style={{ color: getIntensityColor(matchSession.intensity) }}
                    >
                        Intensity: {matchSession.intensity}
                    </div>
                </div>
            </div>

            <div className="match-events">
                <div className="events-header">
                    <h3>Match Events</h3>
                    <button onClick={scrollToBottom} className="scroll-to-bottom">
                        ⬇️ Latest
                    </button>
                </div>

                <div className="events-timeline">
                    {events.map(event => (
                        <div
                            key={event.id}
                            className={'event-item ' + getEventSeverityClass(event.severity) + (event.isKeyEvent ? ' key-event' : '')}
                        >
                            <div className="event-time">
                                {formatMatchTime(event.minute, event.additionalTime)}
                            </div>
                            <div className="event-icon">
                                {getEventIcon(event.eventType)}
                            </div>
                            <div className="event-content">
                                <div className="event-description">
                                    {event.description}
                                </div>
                                {event.detailedDescription && (
                                    <div className="event-details">
                                        {event.detailedDescription}
                                    </div>
                                )}
                                {event.playerName && (
                                    <div className="event-player">
                                        {event.playerName} ({event.teamName})
                                    </div>
                                )}
                            </div>
                            {(event.eventType === 'GOAL' || event.homeScore !== null) && (
                                <div className="event-score">
                                    {event.homeScore} - {event.awayScore}
                                </div>
                            )}
                        </div>
                    ))}
                    <div ref={eventsEndRef} />
                </div>
            </div>

            <div className="match-stats-summary">
                <div className="stat-item">
                    <span>Goals:</span>
                    <span>{events.filter(e => e.eventType === 'GOAL').length}</span>
                </div>
                <div className="stat-item">
                    <span>Cards:</span>
                    <span>{events.filter(e => e.eventType === 'YELLOW_CARD' || e.eventType === 'RED_CARD').length}</span>
                </div>
                <div className="stat-item">
                    <span>Substitutions:</span>
                    <span>{events.filter(e => e.eventType === 'SUBSTITUTION').length}</span>
                </div>
            </div>
        </div>
        </Layout>
    );
};

export default LiveMatchViewer;
