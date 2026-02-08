import React, { useEffect, useState } from 'react';
import Layout from '../components/Layout';
import { getManagerProfile, unlockPerk } from '../services/api';
import { useToast } from '../context/ToastContext';
import QuestList from '../components/QuestList';

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
                    <div className="spinner-border text-primary" role="status">
                        <span className="sr-only">Loading...</span>
                    </div>
                </div>
            </Layout>
        );
    }

    const calculateProgress = () => {
        if (!profile) return 0;
        if (profile.xpForNextLevel === 0) return 100;
        return (profile.currentXp / profile.xpForNextLevel) * 100;
    };

    return (
        <Layout>
            <div className="container-fluid">
                <h1 className="h3 mb-4 text-gray-800">Manager Profile</h1>

                {/* Header Stats */}
                <div className="row mb-4">
                    {/* Level Card */}
                    <div className="col-xl-6 col-md-6 mb-4">
                        <div className="card border-left-primary shadow h-100 py-2">
                            <div className="card-body">
                                <div className="row no-gutters align-items-center">
                                    <div className="col mr-2">
                                        <div className="text-xs font-weight-bold text-primary text-uppercase mb-1">
                                            Level {profile?.level}
                                        </div>
                                        <div className="h5 mb-0 font-weight-bold text-gray-800">
                                            {profile?.currentXp} / {profile?.xpForNextLevel} XP
                                        </div>
                                        <div className="progress mt-2" style={{ height: '10px' }}>
                                            <div className="progress-bar bg-primary"
                                                role="progressbar"
                                                style={{ width: `${calculateProgress()}%` }}
                                                aria-valuenow={calculateProgress()}
                                                aria-valuemin="0"
                                                aria-valuemax="100">
                                            </div>
                                        </div>
                                    </div>
                                    <div className="col-auto">
                                        <i className="fas fa-user-tie fa-2x text-gray-300"></i>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* Talent Points Card */}
                    <div className="col-xl-6 col-md-6 mb-4">
                        <div className="card border-left-success shadow h-100 py-2">
                            <div className="card-body">
                                <div className="row no-gutters align-items-center">
                                    <div className="col mr-2">
                                        <div className="text-xs font-weight-bold text-success text-uppercase mb-1">
                                            Talent Points
                                        </div>
                                        <div className="h5 mb-0 font-weight-bold text-gray-800">{profile?.talentPoints}</div>
                                    </div>
                                    <div className="col-auto">
                                        <i className="fas fa-star fa-2x text-gray-300"></i>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                {/* Tabs */}
                <ul className="nav nav-tabs mb-4">
                    <li className="nav-item">
                        <button className={`nav-link ${activeTab === 'overview' ? 'active' : ''}`} onClick={() => setActiveTab('overview')}>
                            Overview
                        </button>
                    </li>
                    <li className="nav-item">
                        <button className={`nav-link ${activeTab === 'talents' ? 'active' : ''}`} onClick={() => setActiveTab('talents')}>
                            Talents & Perks
                        </button>
                    </li>
                    <li className="nav-item">
                        <button className={`nav-link ${activeTab === 'quests' ? 'active' : ''}`} onClick={() => setActiveTab('quests')}>
                            Quests
                        </button>
                    </li>
                    <li className="nav-item">
                        <button className={`nav-link ${activeTab === 'achievements' ? 'active' : ''}`} onClick={() => setActiveTab('achievements')}>
                            Achievements
                        </button>
                    </li>
                </ul>

                {/* Tab Content */}
                <div>
                    {activeTab === 'overview' && (
                        <div className="card shadow mb-4">
                            <div className="card-header py-3">
                                <h6 className="m-0 font-weight-bold text-primary">Manager Overview</h6>
                            </div>
                            <div className="card-body">
                                <p>Welcome to your manager profile. Here you can track your progress, unlock special perks, and manage your daily quests.</p>
                                <p>Earn XP by winning matches, completing quests, and unlocking achievements.</p>
                            </div>
                        </div>
                    )}

                    {activeTab === 'talents' && (
                        <div className="row">
                            {Object.entries(PERKS).map(([category, perks]) => (
                                <div key={category} className="col-lg-4 mb-4">
                                    <div className="card shadow h-100">
                                        <div className="card-header py-3">
                                            <h6 className="m-0 font-weight-bold text-primary">{category}</h6>
                                        </div>
                                        <div className="card-body">
                                            {perks.map(perk => {
                                                const isUnlocked = profile?.unlockedPerks?.includes(perk.id);
                                                const canUnlock = !isUnlocked &&
                                                                  profile?.talentPoints > 0 &&
                                                                  profile?.level >= perk.requiredLevel;

                                                let borderClass = 'border-left-secondary';
                                                let bgClass = '';
                                                let btnClass = 'btn-secondary disabled';

                                                if (isUnlocked) {
                                                    borderClass = 'border-left-success';
                                                    bgClass = 'bg-light';
                                                } else if (canUnlock) {
                                                    borderClass = 'border-left-warning';
                                                    btnClass = 'btn-warning';
                                                }

                                                return (
                                                    <div key={perk.id} className={`card mb-3 ${borderClass} ${bgClass}`}>
                                                        <div className="card-body p-3">
                                                            <div className="d-flex justify-content-between align-items-center mb-2">
                                                                <h6 className={`font-weight-bold mb-0 ${isUnlocked ? 'text-success' : ''}`}>
                                                                    {perk.name}
                                                                </h6>
                                                                {isUnlocked && <i className="fas fa-check text-success"></i>}
                                                                {canUnlock && <i className="fas fa-unlock text-warning"></i>}
                                                                {!isUnlocked && !canUnlock && <i className="fas fa-lock text-muted"></i>}
                                                            </div>

                                                            <p className="small mb-2 text-muted">{perk.description}</p>

                                                            <div className="small text-muted mb-2">
                                                                Requires Level {perk.requiredLevel}
                                                            </div>

                                                            {!isUnlocked && (
                                                                <button
                                                                    className={`btn btn-sm btn-block ${canUnlock ? 'btn-warning' : 'btn-secondary'}`}
                                                                    disabled={!canUnlock}
                                                                    onClick={() => handleUnlockPerk(perk.id)}
                                                                >
                                                                    {canUnlock ? 'Unlock (1 Point)' : 'Locked'}
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

                    {activeTab === 'quests' && (
                         <div className="card shadow mb-4">
                            <div className="card-header py-3">
                                <h6 className="m-0 font-weight-bold text-primary">Daily & Weekly Quests</h6>
                            </div>
                            <div className="card-body">
                                <QuestList limit={10} />
                            </div>
                        </div>
                    )}

                    {activeTab === 'achievements' && (
                         <div className="card shadow mb-4">
                            <div className="card-header py-3">
                                <h6 className="m-0 font-weight-bold text-primary">Achievements</h6>
                            </div>
                            <div className="card-body">
                                <p className="text-muted">Achievements tracking coming soon...</p>
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </Layout>
    );
};

export default ManagerProfile;
