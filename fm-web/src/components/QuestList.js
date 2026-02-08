import React, { useState, useEffect } from 'react';
import { getQuests, claimQuest } from '../services/api';

const QuestList = ({ limit }) => {
  const [quests, setQuests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [claiming, setClaiming] = useState(null);

  const fetchQuests = async () => {
    try {
      const response = await getQuests();
      setQuests(response.data);
    } catch (err) {
      setError('Failed to load quests');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchQuests();
  }, []);

  const handleClaim = async (id) => {
    setClaiming(id);
    try {
      await claimQuest(id);
      await fetchQuests();
    } catch (err) {
      console.error('Failed to claim reward', err);
    } finally {
      setClaiming(null);
    }
  };

  if (loading) return <div>Loading quests...</div>;
  if (error) return <div>{error}</div>;

  const activeQuests = quests.filter(q => q.status === 'ACTIVE' || q.status === 'COMPLETED');

  // Sort: COMPLETED first, then ACTIVE
  activeQuests.sort((a, b) => {
      if (a.status === 'COMPLETED' && b.status !== 'COMPLETED') return -1;
      if (a.status !== 'COMPLETED' && b.status === 'COMPLETED') return 1;
      return 0;
  });

  const dailyQuests = activeQuests.filter(q => q.frequency === 'DAILY');
  const weeklyQuests = activeQuests.filter(q => q.frequency === 'WEEKLY');

  const renderQuest = (quest) => {
    const isCompleted = quest.status === 'COMPLETED';
    const progress = Math.min((quest.currentValue / quest.targetValue) * 100, 100);

    return (
      <div key={quest.id} className="card mb-3" style={{ borderLeft: isCompleted ? '4px solid #28a745' : '4px solid #007bff' }}>
        <div className="card-body p-3">
          <div className="d-flex justify-content-between align-items-center mb-2">
            <h5 className="mb-0" style={{ fontSize: '1rem' }}>{quest.description}</h5>
            {isCompleted && (
              <button
                className="btn btn-sm btn-success"
                onClick={() => handleClaim(quest.id)}
                disabled={claiming === quest.id}
              >
                {claiming === quest.id ? 'Claiming...' : 'Claim Reward'}
              </button>
            )}
          </div>
          <div className="d-flex justify-content-between text-muted small mb-1">
            <span>{quest.currentValue} / {quest.targetValue}</span>
            <span>{quest.rewardXp} XP</span>
          </div>
          <div className="progress" style={{ height: '6px' }}>
            <div
              className={`progress-bar ${isCompleted ? 'bg-success' : 'bg-primary'}`}
              role="progressbar"
              style={{ width: `${progress}%` }}
              aria-valuenow={progress}
              aria-valuemin="0"
              aria-valuemax="100"
            />
          </div>
        </div>
      </div>
    );
  };

  return (
    <div className="quest-list">
        {activeQuests.length === 0 && <p className="text-muted">No active quests.</p>}

        {dailyQuests.length > 0 && (
            <div className="mb-4">
                <h6 className="text-uppercase text-muted mb-3" style={{ fontSize: '0.75rem', letterSpacing: '1px' }}>Daily Quests</h6>
                {dailyQuests.slice(0, limit || dailyQuests.length).map(renderQuest)}
            </div>
        )}

        {weeklyQuests.length > 0 && (
            <div>
                <h6 className="text-uppercase text-muted mb-3" style={{ fontSize: '0.75rem', letterSpacing: '1px' }}>Weekly Quests</h6>
                {weeklyQuests.slice(0, limit || weeklyQuests.length).map(renderQuest)}
            </div>
        )}
    </div>
  );
};

export default QuestList;
