import React, { useEffect, useState } from 'react';
import Layout from '../components/Layout';
import { getManagerProfile, unlockPerk } from '../services/managerService';
import { useToast } from '../context/ToastContext';

const PERKS = {
    TACTICAL: [
        { id: 'VIDEO_ANALYST', name: 'Video Analyst', description: 'Increases tactical learning speed by 5%.', requiredLevel: 1 },
        { id: 'MOTIVATOR', name: 'Motivator', description: 'Increases morale recovery after defeat by 10%.', requiredLevel: 5 },
        { id: 'FORTRESS', name: 'Fortress', description: 'Increases home advantage bonus by 2%.', requiredLevel: 10 }
    ],
    FINANCIAL: [
        { id: 'NEGOTIATOR', name: 'Negotiator', description: 'Reduces wage demands by 5%.', requiredLevel: 1 },
        { id: 'MARKETING_GURU', name: 'Marketing Guru', description: 'Increases sponsor revenue by 5%.', requiredLevel: 5 },
        { id: 'INVESTOR', name: 'Investor', description: 'Earns interest on bank deposits.', requiredLevel: 10 }
    ],
    SCOUTING: [
        { id: 'HAWK_EYE', name: 'Hawk Eye', description: 'Reduces scouting time by 10%.', requiredLevel: 1 },
        { id: 'GLOBAL_NETWORK', name: 'Global Network', description: 'Increases probability of finding Wonderkids.', requiredLevel: 5 },
        { id: 'PERSUADER', name: 'Persuader', description: 'Increases probability of negotiation acceptance.', requiredLevel: 10 }
    ]
};

const ManagerProfile = () => {
    const [profile, setProfile] = useState(null);
    const [loading, setLoading] = useState(true);
    const [activeTab, setActiveTab] = useState('overview');
    const { showToast } = useToast();

    useEffect(() => {
        fetchProfile();
    }, []);

    const fetchProfile = async () => {
        try {
            const response = await getManagerProfile();
            setProfile(response.data);
        } catch (error) {
            console.error('Error fetching profile:', error);
            showToast('Error fetching profile', 'error');
        } finally {
            setLoading(false);
        }
    };

    const handleUnlockPerk = async (perkId) => {
        try {
            const response = await unlockPerk(perkId);
            setProfile(response.data);
            showToast('Perk unlocked successfully!', 'success');
        } catch (error) {
            console.error('Error unlocking perk:', error);
            showToast('Error unlocking perk', 'error');
        }
    };

    if (loading) {
        return (
            <Layout>
                <div className="d-flex justify-content-center align-items-center" style={{ height: '80vh' }}>
                    <div style={{ color: 'var(--primary)', fontSize: '1.5rem' }}>Loading...</div>
                </div>
            </Layout>
        );
    }

    const calculateProgress = () => {
        if (!profile) return 0;
        return (profile.currentXp / profile.xpForNextLevel) * 100;
    };

    const tabStyle = (isActive) => ({
        padding: '0.75rem 1.5rem',
        cursor: 'pointer',
        borderBottom: isActive ? '3px solid var(--primary)' : '3px solid transparent',
        color: isActive ? 'var(--primary)' : 'var(--text-muted)',
        fontWeight: 'bold',
        background: 'transparent',
        borderTop: 'none',
        borderLeft: 'none',
        borderRight: 'none',
    });

    return (
        <Layout>
            <div className="container-fluid">
                <h1 className="h3 mb-4">Manager Profile</h1>

                {/* Header Stats */}
                <div className="row mb-4">
                    <div className="col-xl-4 col-md-6 mb-4" style={{ minWidth: '300px' }}>
                        <div className="card shadow h-100 py-2" style={{ borderLeft: '4px solid var(--primary)' }}>
                            <div className="card-body">
                                <div className="row no-gutters align-items-center">
                                    <div className="col mr-2">
                                        <div style={{ fontSize: '0.75rem', fontWeight: 'bold', color: 'var(--primary)', textTransform: 'uppercase', marginBottom: '0.25rem' }}>
                                            Level {profile?.level}
                                        </div>
                                        <div className="h5 mb-0 font-weight-bold" style={{ color: 'var(--text-main)' }}>
                                            {profile?.currentXp} / {profile?.xpForNextLevel} XP
                                        </div>
                                        <div style={{ height: '10px', background: 'var(--bg-darker)', borderRadius: '5px', marginTop: '0.5rem', overflow: 'hidden' }}>
                                            <div style={{
                                                width: `${calculateProgress()}%`,
                                                height: '100%',
                                                background: 'var(--primary)',
                                                transition: 'width 0.5s ease-in-out'
                                            }}></div>
                                        </div>
                                    </div>
                                    <div className="col-auto">
                                        <i className="fas fa-user-tie fa-2x" style={{ color: 'var(--text-muted)' }}></i>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div className="col-xl-4 col-md-6 mb-4" style={{ minWidth: '300px' }}>
                        <div className="card shadow h-100 py-2" style={{ borderLeft: '4px solid var(--success)' }}>
                            <div className="card-body">
                                <div className="row no-gutters align-items-center">
                                    <div className="col mr-2">
                                        <div style={{ fontSize: '0.75rem', fontWeight: 'bold', color: 'var(--success)', textTransform: 'uppercase', marginBottom: '0.25rem' }}>
                                            Talent Points
                                        </div>
                                        <div className="h5 mb-0 font-weight-bold" style={{ color: 'var(--text-main)' }}>{profile?.talentPoints}</div>
                                    </div>
                                    <div className="col-auto">
                                        <i className="fas fa-star fa-2x" style={{ color: 'var(--text-muted)' }}></i>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                {/* Tabs */}
                <div style={{ display: 'flex', borderBottom: '1px solid var(--border-color)', marginBottom: '1.5rem' }}>
                    <button style={tabStyle(activeTab === 'overview')} onClick={() => setActiveTab('overview')}>Overview</button>
                    <button style={tabStyle(activeTab === 'talents')} onClick={() => setActiveTab('talents')}>Talents</button>
                    <button style={tabStyle(activeTab === 'achievements')} onClick={() => setActiveTab('achievements')}>Achievements</button>
                </div>

                {/* Tab Content */}
                <div>
                    {activeTab === 'overview' && (
                        <div className="card shadow mb-4">
                            <div className="card-header py-3">
                                <h6 className="m-0 font-weight-bold">Overview</h6>
                            </div>
                            <div className="card-body">
                                <p>Manager Overview content coming soon...</p>
                            </div>
                        </div>
                    )}

                    {activeTab === 'talents' && (
                        <div className="row">
                            {Object.entries(PERKS).map(([category, perks]) => (
                                <div key={category} className="col-lg-4" style={{ minWidth: '300px', flex: '1' }}>
                                    <div className="card shadow mb-4">
                                        <div className="card-header py-3">
                                            <h6 className="m-0 font-weight-bold">{category}</h6>
                                        </div>
                                        <div className="card-body">
                                            {perks.map(perk => {
                                                const isUnlocked = profile?.unlockedPerks?.includes(perk.id);
                                                const canUnlock = !isUnlocked &&
                                                                  profile?.talentPoints > 0 &&
                                                                  profile?.level >= perk.requiredLevel;

                                                let borderColor = 'var(--border-color)';
                                                let iconColor = 'var(--text-muted)';
                                                let opacity = 0.5;
                                                let cursor = 'default';

                                                if (isUnlocked) {
                                                    borderColor = 'var(--success)';
                                                    iconColor = 'var(--success)';
                                                    opacity = 1;
                                                } else if (canUnlock) {
                                                    borderColor = 'var(--warning)';
                                                    iconColor = 'var(--warning)';
                                                    opacity = 1;
                                                    cursor = 'pointer';
                                                }

                                                return (
                                                    <div key={perk.id} className="card mb-3"
                                                        style={{
                                                            border: `1px solid ${borderColor}`,
                                                            opacity: opacity,
                                                            cursor: cursor,
                                                            backgroundColor: 'var(--bg-card)'
                                                        }}
                                                        onClick={() => canUnlock && handleUnlockPerk(perk.id)}>
                                                        <div className="card-body">
                                                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
                                                                <h6 className="font-weight-bold mb-0" style={{ color: isUnlocked ? 'var(--success)' : canUnlock ? 'var(--warning)' : 'var(--text-muted)' }}>
                                                                    {perk.name}
                                                                </h6>
                                                                {isUnlocked && <i className="fas fa-check" style={{ color: 'var(--success)' }}></i>}
                                                                {canUnlock && <i className="fas fa-unlock" style={{ color: 'var(--warning)' }}></i>}
                                                                {!isUnlocked && !canUnlock && <i className="fas fa-lock" style={{ color: 'var(--text-muted)' }}></i>}
                                                            </div>

                                                            <p className="small mb-2" style={{ color: 'var(--text-main)' }}>{perk.description}</p>

                                                            <div className="small" style={{ color: 'var(--text-muted)' }}>
                                                                Requires Level {perk.requiredLevel}
                                                            </div>

                                                            {canUnlock && (
                                                                <button className="btn btn-sm mt-3 w-100"
                                                                    style={{
                                                                        backgroundColor: 'var(--warning)',
                                                                        borderColor: 'var(--warning)',
                                                                        color: '#212529'
                                                                    }}
                                                                    onClick={(e) => { e.stopPropagation(); handleUnlockPerk(perk.id); }}>
                                                                    Unlock (1 Point)
                                                                </button>
                                                            )}
                                                        </div>
                                                    </div>
                                                );
                                            })}
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}

                    {activeTab === 'achievements' && (
                         <div className="card shadow mb-4">
                            <div className="card-header py-3">
                                <h6 className="m-0 font-weight-bold">Achievements</h6>
                            </div>
                            <div className="card-body">
                                <p>Achievements content coming soon...</p>
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </Layout>
    );
};

export default ManagerProfile;
