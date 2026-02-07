import React, { useState, useEffect } from 'react';
import { getClubs, createClub, updateClub, deleteClub } from '../services/api';
import { useToast } from '../context/ToastContext';
import ConfirmationModal from '../components/ConfirmationModal';

const ClubManagement = () => {
    const { showToast } = useToast();
    const [clubs, setClubs] = useState([]);
    const [selectedClub, setSelectedClub] = useState(null);
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [confirmationModal, setConfirmationModal] = useState({ isOpen: false, title: '', message: '', onConfirm: () => {} });
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');

    useEffect(() => {
        loadClubs();
    }, []);

    const loadClubs = async () => {
        try {
            const response = await getClubs();
            setClubs(response.data);
        } catch (error) {
            console.error('Error loading clubs:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleCreateClub = async (clubData) => {
        try {
            await createClub(clubData);
            loadClubs();
            setShowCreateModal(false);
            showToast('Club created successfully', 'success');
        } catch (error) {
            console.error('Error creating club:', error);
            showToast('Error creating club: ' + error.message, 'error');
        }
    };

    const handleDeleteClub = async (clubId) => {
        setConfirmationModal({
            isOpen: true,
            title: 'Delete Club',
            message: 'Are you sure you want to delete this club?',
            onConfirm: async () => {
                try {
                    await deleteClub(clubId);
                    loadClubs();
                    showToast('Club deleted successfully', 'success');
                } catch (error) {
                    console.error('Error deleting club:', error);
                    showToast('Error deleting club', 'error');
                }
            }
        });
    };

    const filteredClubs = clubs.filter(club => {
        return club.name.toLowerCase().includes(searchTerm.toLowerCase());
    });

    if (loading) return <div>Loading clubs...</div>;

    return (
        <div style={{padding: '20px'}}>
            <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px'}}>
                <h1>Club Management</h1>
                <button onClick={() => setShowCreateModal(true)} style={{padding: '10px 20px', backgroundColor: '#007bff', color: 'white', border: 'none', borderRadius: '5px', cursor: 'pointer'}}>
                    Create New Club
                </button>
            </div>

            <div style={{marginBottom: '20px'}}>
                <input
                    type="text"
                    placeholder="Search clubs..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    style={{padding: '8px', width: '300px'}}
                />
            </div>

            <table style={{width: '100%', borderCollapse: 'collapse'}}>
                <thead>
                    <tr style={{textAlign: 'left', backgroundColor: '#f8f9fa'}}>
                        <th style={{padding: '10px'}}>Name</th>
                        <th style={{padding: '10px'}}>Founded</th>
                        <th style={{padding: '10px'}}>City</th>
                        <th style={{padding: '10px'}}>Stadium</th>
                        <th style={{padding: '10px'}}>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    {filteredClubs.map(club => (
                        <tr key={club.id} style={{borderBottom: '1px solid #eee'}}>
                            <td style={{padding: '10px'}}>{club.name}</td>
                            <td style={{padding: '10px'}}>{club.foundation}</td>
                            <td style={{padding: '10px'}}>{club.city || '-'}</td>
                            <td style={{padding: '10px'}}>{club.stadium?.name || 'No Stadium'}</td>
                            <td style={{padding: '10px'}}>
                                <button
                                    onClick={() => handleDeleteClub(club.id)}
                                    style={{padding: '5px 10px', backgroundColor: '#dc3545', color: 'white', border: 'none', borderRadius: '3px', cursor: 'pointer'}}
                                >
                                    Delete
                                </button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>

            {showCreateModal && (
                <CreateClubModal
                    onClose={() => setShowCreateModal(false)}
                    onSubmit={handleCreateClub}
                />
            )}

            <ConfirmationModal
                isOpen={confirmationModal.isOpen}
                onClose={() => setConfirmationModal({ ...confirmationModal, isOpen: false })}
                onConfirm={confirmationModal.onConfirm}
                title={confirmationModal.title}
                message={confirmationModal.message}
            />
        </div>
    );
};

const CreateClubModal = ({ onClose, onSubmit }) => {
    const [formData, setFormData] = useState({
        name: '',
        foundedYear: new Date().getFullYear(),
        city: '',
        leagueId: '', // Need to fetch leagues? For now text input or simplified
        initialBudget: 1000000,
        generateInitialSquad: true,
        stadiumRequest: {
            name: '',
            capacity: 20000
        }
    });

    const handleSubmit = (e) => {
        e.preventDefault();
        onSubmit(formData);
    };

    return (
        <div style={{position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', justifyContent: 'center', alignItems: 'center'}}>
            <div style={{backgroundColor: 'white', padding: '30px', borderRadius: '8px', width: '500px', maxHeight: '90vh', overflowY: 'auto'}}>
                <h2>Create New Club</h2>
                <form onSubmit={handleSubmit}>
                    <div style={{marginBottom: '15px'}}>
                        <label style={{display: 'block', marginBottom: '5px'}}>Club Name</label>
                        <input
                            type="text"
                            value={formData.name}
                            onChange={(e) => setFormData({...formData, name: e.target.value})}
                            required
                            style={{width: '100%', padding: '8px'}}
                        />
                    </div>
                    <div style={{marginBottom: '15px'}}>
                        <label style={{display: 'block', marginBottom: '5px'}}>City</label>
                        <input
                            type="text"
                            value={formData.city}
                            onChange={(e) => setFormData({...formData, city: e.target.value})}
                            style={{width: '100%', padding: '8px'}}
                        />
                    </div>
                    <div style={{marginBottom: '15px'}}>
                        <label style={{display: 'block', marginBottom: '5px'}}>Founded Year</label>
                        <input
                            type="number"
                            value={formData.foundedYear}
                            onChange={(e) => setFormData({...formData, foundedYear: parseInt(e.target.value)})}
                            style={{width: '100%', padding: '8px'}}
                        />
                    </div>
                    <div style={{marginBottom: '15px'}}>
                        <label style={{display: 'block', marginBottom: '5px'}}>League ID</label>
                        <input
                            type="number"
                            value={formData.leagueId}
                            onChange={(e) => setFormData({...formData, leagueId: e.target.value ? parseInt(e.target.value) : ''})}
                            style={{width: '100%', padding: '8px'}}
                        />
                    </div>
                    <div style={{marginBottom: '15px'}}>
                        <label style={{display: 'block', marginBottom: '5px'}}>Initial Budget</label>
                        <input
                            type="number"
                            value={formData.initialBudget}
                            onChange={(e) => setFormData({...formData, initialBudget: parseInt(e.target.value)})}
                            style={{width: '100%', padding: '8px'}}
                        />
                    </div>
                    <div style={{marginBottom: '15px'}}>
                        <label>
                            <input
                                type="checkbox"
                                checked={formData.generateInitialSquad}
                                onChange={(e) => setFormData({...formData, generateInitialSquad: e.target.checked})}
                            />
                             Generate Initial Squad
                        </label>
                    </div>

                    <h3>Stadium</h3>
                    <div style={{marginBottom: '15px'}}>
                        <label style={{display: 'block', marginBottom: '5px'}}>Name</label>
                        <input
                            type="text"
                            value={formData.stadiumRequest.name}
                            onChange={(e) => setFormData({...formData, stadiumRequest: {...formData.stadiumRequest, name: e.target.value}})}
                            style={{width: '100%', padding: '8px'}}
                        />
                    </div>
                    <div style={{marginBottom: '15px'}}>
                        <label style={{display: 'block', marginBottom: '5px'}}>Capacity</label>
                        <input
                            type="number"
                            value={formData.stadiumRequest.capacity}
                            onChange={(e) => setFormData({...formData, stadiumRequest: {...formData.stadiumRequest, capacity: parseInt(e.target.value)}})}
                            style={{width: '100%', padding: '8px'}}
                        />
                    </div>

                    <div style={{display: 'flex', justifyContent: 'flex-end', gap: '10px'}}>
                        <button type="button" onClick={onClose} style={{padding: '10px', cursor: 'pointer'}}>Cancel</button>
                        <button type="submit" style={{padding: '10px 20px', backgroundColor: '#007bff', color: 'white', border: 'none', borderRadius: '5px', cursor: 'pointer'}}>Create</button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default ClubManagement;
