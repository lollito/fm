import React, { useState, useEffect } from 'react';
import { getTrainingPlan, updateTrainingPlan } from '../services/api';

const TrainingPlan = ({ teamId }) => {
    const [plan, setPlan] = useState(null);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);

    const trainingFocusOptions = [
        { value: 'ATTACKING', label: 'Attacking', description: 'Improves scoring, winger, passing' },
        { value: 'DEFENDING', label: 'Defending', description: 'Improves defending, playmaking' },
        { value: 'PHYSICAL', label: 'Physical', description: 'Improves stamina' },
        { value: 'TECHNICAL', label: 'Technical', description: 'Improves passing, set pieces' },
        { value: 'GOALKEEPING', label: 'Goalkeeping', description: 'Improves goalkeeping, set pieces' },
        { value: 'BALANCED', label: 'Balanced', description: 'Improves all skills moderately' }
    ];

    const intensityOptions = [
        { value: 'LIGHT', label: 'Light', description: 'Low improvement, low fatigue' },
        { value: 'MODERATE', label: 'Moderate', description: 'Normal improvement, normal fatigue' },
        { value: 'INTENSIVE', label: 'Intensive', description: 'High improvement, high fatigue' },
        { value: 'RECOVERY', label: 'Recovery', description: 'Minimal improvement, restores condition' }
    ];

    useEffect(() => {
        if (teamId) {
            loadTrainingPlan();
        }
    }, [teamId]);

    const loadTrainingPlan = async () => {
        try {
            const response = await getTrainingPlan(teamId);
            setPlan(response.data || null);
        } catch (error) {
            console.error('Error loading training plan:', error);
            setPlan(null);
        } finally {
            setLoading(false);
        }
    };

    const handleSave = async () => {
        setSaving(true);
        try {
            const response = await updateTrainingPlan(teamId, plan);
            setPlan(response.data);
            alert('Training plan saved successfully!');
        } catch (error) {
            console.error('Error saving training plan:', error);
            alert('Failed to save training plan.');
        } finally {
            setSaving(false);
        }
    };

    const handleFocusChange = (day, focus) => {
        if (!plan) return;
        setPlan(prev => ({
            ...prev,
            [day + 'Focus']: focus
        }));
    };

    if (loading) return <div>Loading training plan...</div>;
    if (!plan) return <div>No plan data available</div>;

    return (
        <div className="training-plan">
            <h2>Training Plan</h2>

            <div className="training-schedule">
                <h3>Weekly Schedule</h3>
                {['monday', 'tuesday', 'wednesday', 'thursday', 'friday', 'saturday', 'sunday'].map(day => (
                    <div key={day} className="training-day" style={{ marginBottom: '15px' }}>
                        <label className="day-label" style={{ fontWeight: 'bold', marginRight: '10px' }}>
                            {day.charAt(0).toUpperCase() + day.slice(1)}
                        </label>
                        <select
                            value={plan[day + 'Focus'] || ''}
                            onChange={(e) => handleFocusChange(day, e.target.value)}
                            className="focus-select"
                            style={{ padding: '5px', marginRight: '10px' }}
                        >
                            <option value="">Rest Day</option>
                            {trainingFocusOptions.map(option => (
                                <option key={option.value} value={option.value}>
                                    {option.label}
                                </option>
                            ))}
                        </select>
                        {plan[day + 'Focus'] && (
                            <span className="focus-description" style={{ fontSize: '0.9em', color: '#666' }}>
                                {trainingFocusOptions.find(o => o.value === plan[day + 'Focus'])?.description}
                            </span>
                        )}
                    </div>
                ))}
            </div>

            <div className="training-intensity" style={{ marginTop: '20px' }}>
                <h3>Training Intensity</h3>
                {intensityOptions.map(option => (
                    <div key={option.value} className="intensity-option" style={{ marginBottom: '5px' }}>
                        <label>
                            <input
                                type="radio"
                                name="intensity"
                                value={option.value}
                                checked={plan.intensity === option.value}
                                onChange={(e) => setPlan(prev => ({
                                    ...prev,
                                    intensity: e.target.value
                                }))}
                            />
                            <span className="intensity-label" style={{ fontWeight: 'bold', margin: '0 5px' }}>{option.label}</span>
                            <span className="intensity-description" style={{ fontSize: '0.9em', color: '#666' }}>({option.description})</span>
                        </label>
                    </div>
                ))}
            </div>

            <div className="weekend-settings" style={{ marginTop: '20px' }}>
                <label className="weekend-rest">
                    <input
                        type="checkbox"
                        checked={plan.restOnWeekends || false}
                        onChange={(e) => setPlan(prev => ({
                            ...prev,
                            restOnWeekends: e.target.checked
                        }))}
                    />
                    Rest on weekends
                </label>
            </div>

            <div className="training-actions" style={{ marginTop: '20px' }}>
                <button
                    onClick={handleSave}
                    disabled={saving}
                    className="save-button"
                    style={{ padding: '10px 20px', backgroundColor: '#007bff', color: 'white', border: 'none', borderRadius: '5px', cursor: 'pointer' }}
                >
                    {saving ? 'Saving...' : 'Save Training Plan'}
                </button>
            </div>
        </div>
    );
};

export default TrainingPlan;
