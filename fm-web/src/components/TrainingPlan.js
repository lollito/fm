import React, { useState, useEffect } from 'react';
import { getTrainingPlan, updateTrainingPlan } from '../services/api';
import { useToast } from '../context/ToastContext';

const TrainingPlan = ({ teamId }) => {
    const [plan, setPlan] = useState(null);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const { showToast } = useToast();

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
            const payload = {
                mondayFocus: plan.mondayFocus || null,
                tuesdayFocus: plan.tuesdayFocus || null,
                wednesdayFocus: plan.wednesdayFocus || null,
                thursdayFocus: plan.thursdayFocus || null,
                fridayFocus: plan.fridayFocus || null,
                saturdayFocus: plan.saturdayFocus || null,
                sundayFocus: plan.sundayFocus || null,
                intensity: plan.intensity,
                restOnWeekends: plan.restOnWeekends || false
            };

            const response = await updateTrainingPlan(teamId, payload);
            setPlan(response.data);
            showToast('Training plan saved successfully!', 'success');
        } catch (error) {
            console.error('Error saving training plan:', error);
            showToast('Failed to save training plan.', 'error');
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

    if (loading) return (
        <div className="d-flex justify-content-center my-5" role="status">
            <div className="spinner-border text-primary" role="status">
                <span className="visually-hidden">Loading training plan...</span>
            </div>
        </div>
    );
    if (!plan) return <div>No plan data available</div>;

    return (
        <div className="training-plan">
            <h2>Training Plan</h2>

            <div className="training-schedule">
                <h3>Weekly Schedule</h3>
                {['monday', 'tuesday', 'wednesday', 'thursday', 'friday', 'saturday', 'sunday'].map(day => (
                    <div key={day} className="training-day" style={{ marginBottom: '15px' }}>
                        <label
                            htmlFor={`training-focus-${day}`}
                            className="day-label"
                            style={{ fontWeight: 'bold', marginRight: '10px', display: 'inline-block', width: '100px' }}
                        >
                            {day.charAt(0).toUpperCase() + day.slice(1)}
                        </label>
                        <select
                            id={`training-focus-${day}`}
                            value={plan[day + 'Focus'] || ''}
                            onChange={(e) => handleFocusChange(day, e.target.value)}
                            className="focus-select form-control"
                            style={{ marginRight: '10px', display: 'inline-block', width: 'auto' }}
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
                    <div key={option.value} className="intensity-option mb-2">
                        <div className="d-flex align-items-center">
                            <input
                                id={`intensity-${option.value}`}
                                type="radio"
                                name="intensity"
                                value={option.value}
                                checked={plan.intensity === option.value}
                                onChange={(e) => setPlan(prev => ({
                                    ...prev,
                                    intensity: e.target.value
                                }))}
                                aria-describedby={`desc-intensity-${option.value}`}
                                className="me-2"
                            />
                            <label htmlFor={`intensity-${option.value}`} className="fw-bold me-2 mb-0" style={{ cursor: 'pointer' }}>
                                {option.label}
                            </label>
                            <span id={`desc-intensity-${option.value}`} className="text-muted small">
                                ({option.description})
                            </span>
                        </div>
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
                    className="save-button btn btn-primary"
                    aria-busy={saving}
                >
                    {saving ? (
                        <>
                            <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                            Saving...
                        </>
                    ) : 'Save Training Plan'}
                </button>
            </div>
        </div>
    );
};

export default TrainingPlan;
