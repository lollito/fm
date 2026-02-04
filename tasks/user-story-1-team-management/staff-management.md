# Staff Management System Implementation

## Overview
Implement a comprehensive staff management system allowing clubs to hire and manage coaching staff, medical staff, and scouts that provide various bonuses to team performance.

## Technical Requirements

### Database Schema Changes

#### New Entity: Staff
```java
@Entity
@Table(name = "staff")
public class Staff {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String surname;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDate birth;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private Club club;
    
    @Enumerated(EnumType.STRING)
    private StaffRole role;
    
    @Enumerated(EnumType.STRING)
    private StaffSpecialization specialization;
    
    private Integer ability; // 1-20 rating
    private Integer reputation; // 1-20 rating
    private BigDecimal monthlySalary;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDate contractStart;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDate contractEnd;
    
    @Enumerated(EnumType.STRING)
    private StaffStatus status; // ACTIVE, INJURED, SUSPENDED, TERMINATED
    
    private Double motivationBonus; // Bonus to player morale
    private Double trainingBonus; // Bonus to training effectiveness
    private Double injuryPreventionBonus; // Reduces injury probability
    private Double recoveryBonus; // Speeds up injury recovery
    private Double scoutingBonus; // Improves scouting accuracy
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nationality_id")
    private Country nationality;
    
    private String description; // Staff background/bio
    private Integer experience; // Years of experience
}
```

#### Enums to Create
```java
public enum StaffRole {
    HEAD_COACH("Head Coach", 50000, 200000),
    ASSISTANT_COACH("Assistant Coach", 20000, 80000),
    FITNESS_COACH("Fitness Coach", 15000, 60000),
    GOALKEEPING_COACH("Goalkeeping Coach", 18000, 70000),
    YOUTH_COACH("Youth Coach", 12000, 50000),
    HEAD_PHYSIO("Head Physio", 25000, 90000),
    PHYSIO("Physio", 15000, 60000),
    DOCTOR("Doctor", 30000, 120000),
    HEAD_SCOUT("Head Scout", 20000, 80000),
    SCOUT("Scout", 10000, 40000),
    ANALYST("Analyst", 15000, 55000);
    
    private final String displayName;
    private final int minSalary;
    private final int maxSalary;
}

public enum StaffSpecialization {
    // Coaching specializations
    ATTACKING_PLAY("Attacking Play"),
    DEFENSIVE_PLAY("Defensive Play"),
    SET_PIECES("Set Pieces"),
    YOUTH_DEVELOPMENT("Youth Development"),
    PLAYER_DEVELOPMENT("Player Development"),
    
    // Medical specializations
    INJURY_PREVENTION("Injury Prevention"),
    REHABILITATION("Rehabilitation"),
    SPORTS_PSYCHOLOGY("Sports Psychology"),
    
    // Scouting specializations
    DOMESTIC_SCOUTING("Domestic Scouting"),
    INTERNATIONAL_SCOUTING("International Scouting"),
    YOUTH_SCOUTING("Youth Scouting"),
    OPPOSITION_ANALYSIS("Opposition Analysis");
    
    private final String displayName;
}

public enum StaffStatus {
    ACTIVE, INJURED, SUSPENDED, TERMINATED
}
```

#### New Entity: StaffContract
```java
@Entity
@Table(name = "staff_contract")
public class StaffContract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id")
    private Staff staff;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private Club club;
    
    private BigDecimal monthlySalary;
    private BigDecimal signingBonus;
    private BigDecimal performanceBonus;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDate startDate;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDate endDate;
    
    @Enumerated(EnumType.STRING)
    private ContractStatus status; // ACTIVE, EXPIRED, TERMINATED
    
    private String terminationClause;
    private BigDecimal terminationFee;
}
```

#### Club Entity Updates
```java
// Add to Club.java
@OneToMany(mappedBy = "club", cascade = CascadeType.ALL)
private List<Staff> staff = new ArrayList<>();

@Transient
public List<Staff> getActiveStaff() {
    return staff.stream()
        .filter(s -> s.getStatus() == StaffStatus.ACTIVE)
        .collect(Collectors.toList());
}

@Transient
public Double getTotalStaffSalaries() {
    return getActiveStaff().stream()
        .mapToDouble(s -> s.getMonthlySalary().doubleValue())
        .sum();
}
```

### Service Layer Implementation

#### StaffService
```java
@Service
public class StaffService {
    
    @Autowired
    private StaffRepository staffRepository;
    
    @Autowired
    private StaffContractRepository staffContractRepository;
    
    @Autowired
    private ClubService clubService;
    
    @Autowired
    private NameService nameService;
    
    /**
     * Generate available staff for hiring
     */
    public List<Staff> generateAvailableStaff(StaffRole role, int count) {
        List<Staff> availableStaff = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            Staff staff = generateRandomStaff(role);
            availableStaff.add(staff);
        }
        
        return staffRepository.saveAll(availableStaff);
    }
    
    /**
     * Generate a random staff member
     */
    private Staff generateRandomStaff(StaffRole role) {
        String name = nameService.generateRandomName();
        String surname = nameService.generateRandomSurname();
        LocalDate birth = generateStaffBirthDate(); // 25-65 years old
        
        int ability = RandomUtils.randomValue(1, 20);
        int reputation = RandomUtils.randomValue(1, 20);
        int experience = RandomUtils.randomValue(1, 40);
        
        BigDecimal salary = calculateStaffSalary(role, ability, reputation, experience);
        
        Staff staff = Staff.builder()
            .name(name)
            .surname(surname)
            .birth(birth)
            .role(role)
            .specialization(generateSpecialization(role))
            .ability(ability)
            .reputation(reputation)
            .experience(experience)
            .monthlySalary(salary)
            .status(StaffStatus.ACTIVE)
            .nationality(generateRandomNationality())
            .description(generateStaffDescription(role, ability, experience))
            .build();
        
        // Calculate bonuses based on role and ability
        calculateStaffBonuses(staff);
        
        return staff;
    }
    
    /**
     * Calculate staff bonuses based on role and ability
     */
    private void calculateStaffBonuses(Staff staff) {
        double abilityMultiplier = staff.getAbility() / 20.0; // 0.05 to 1.0
        
        switch (staff.getRole()) {
            case HEAD_COACH, ASSISTANT_COACH -> {
                staff.setMotivationBonus(abilityMultiplier * 0.2); // Up to 20% morale bonus
                staff.setTrainingBonus(abilityMultiplier * 0.3); // Up to 30% training bonus
            }
            case FITNESS_COACH -> {
                staff.setTrainingBonus(abilityMultiplier * 0.25);
                staff.setInjuryPreventionBonus(abilityMultiplier * 0.3);
            }
            case GOALKEEPING_COACH -> {
                staff.setTrainingBonus(abilityMultiplier * 0.4); // Specialized bonus for GK training
            }
            case YOUTH_COACH -> {
                staff.setTrainingBonus(abilityMultiplier * 0.2); // Youth development bonus
            }
            case HEAD_PHYSIO, PHYSIO -> {
                staff.setInjuryPreventionBonus(abilityMultiplier * 0.4);
                staff.setRecoveryBonus(abilityMultiplier * 0.5);
            }
            case DOCTOR -> {
                staff.setInjuryPreventionBonus(abilityMultiplier * 0.3);
                staff.setRecoveryBonus(abilityMultiplier * 0.6);
            }
            case HEAD_SCOUT, SCOUT -> {
                staff.setScoutingBonus(abilityMultiplier * 0.4);
            }
            case ANALYST -> {
                staff.setTrainingBonus(abilityMultiplier * 0.15);
                staff.setScoutingBonus(abilityMultiplier * 0.25);
            }
        }
    }
    
    /**
     * Hire staff member for club
     */
    public StaffContract hireStaff(Long clubId, Long staffId, HireStaffRequest request) {
        Club club = clubService.findById(clubId);
        Staff staff = staffRepository.findById(staffId)
            .orElseThrow(() -> new EntityNotFoundException("Staff not found"));
        
        if (staff.getClub() != null) {
            throw new IllegalStateException("Staff member already employed");
        }
        
        // Check if club can afford the salary
        BigDecimal totalMonthlyCosts = club.getTotalStaffSalaries() + staff.getMonthlySalary().doubleValue();
        if (totalMonthlyCosts.compareTo(club.getFinance().getBalance().multiply(BigDecimal.valueOf(0.1))) > 0) {
            throw new InsufficientFundsException("Cannot afford staff salary");
        }
        
        // Create contract
        StaffContract contract = StaffContract.builder()
            .staff(staff)
            .club(club)
            .monthlySalary(staff.getMonthlySalary())
            .signingBonus(request.getSigningBonus())
            .performanceBonus(request.getPerformanceBonus())
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusYears(request.getContractYears()))
            .status(ContractStatus.ACTIVE)
            .terminationFee(calculateTerminationFee(staff, request.getContractYears()))
            .build();
        
        // Update staff
        staff.setClub(club);
        staff.setContractStart(contract.getStartDate());
        staff.setContractEnd(contract.getEndDate());
        
        // Deduct signing bonus from club finances
        if (request.getSigningBonus().compareTo(BigDecimal.ZERO) > 0) {
            club.getFinance().setBalance(
                club.getFinance().getBalance().subtract(request.getSigningBonus())
            );
        }
        
        staffRepository.save(staff);
        return staffContractRepository.save(contract);
    }
    
    /**
     * Fire staff member
     */
    public void fireStaff(Long staffId, String reason) {
        Staff staff = staffRepository.findById(staffId)
            .orElseThrow(() -> new EntityNotFoundException("Staff not found"));
        
        if (staff.getClub() == null) {
            throw new IllegalStateException("Staff member not employed");
        }
        
        // Calculate termination fee
        StaffContract contract = staffContractRepository.findByStaffAndStatus(staff, ContractStatus.ACTIVE)
            .orElseThrow(() -> new EntityNotFoundException("Active contract not found"));
        
        BigDecimal terminationFee = contract.getTerminationFee();
        
        // Deduct termination fee from club finances
        Club club = staff.getClub();
        club.getFinance().setBalance(
            club.getFinance().getBalance().subtract(terminationFee)
        );
        
        // Update staff and contract
        staff.setClub(null);
        staff.setStatus(StaffStatus.TERMINATED);
        contract.setStatus(ContractStatus.TERMINATED);
        
        staffRepository.save(staff);
        staffContractRepository.save(contract);
        
        // Create news item about firing
        createStaffFiringNews(staff, reason);
    }
    
    /**
     * Get staff bonuses for a club
     */
    public StaffBonuses calculateClubStaffBonuses(Long clubId) {
        Club club = clubService.findById(clubId);
        List<Staff> activeStaff = club.getActiveStaff();
        
        double totalMotivationBonus = activeStaff.stream()
            .mapToDouble(s -> s.getMotivationBonus() != null ? s.getMotivationBonus() : 0.0)
            .sum();
            
        double totalTrainingBonus = activeStaff.stream()
            .mapToDouble(s -> s.getTrainingBonus() != null ? s.getTrainingBonus() : 0.0)
            .sum();
            
        double totalInjuryPreventionBonus = activeStaff.stream()
            .mapToDouble(s -> s.getInjuryPreventionBonus() != null ? s.getInjuryPreventionBonus() : 0.0)
            .sum();
            
        double totalRecoveryBonus = activeStaff.stream()
            .mapToDouble(s -> s.getRecoveryBonus() != null ? s.getRecoveryBonus() : 0.0)
            .sum();
            
        double totalScoutingBonus = activeStaff.stream()
            .mapToDouble(s -> s.getScoutingBonus() != null ? s.getScoutingBonus() : 0.0)
            .sum();
        
        return StaffBonuses.builder()
            .motivationBonus(Math.min(1.0, totalMotivationBonus)) // Cap at 100%
            .trainingBonus(Math.min(1.0, totalTrainingBonus))
            .injuryPreventionBonus(Math.min(0.8, totalInjuryPreventionBonus)) // Cap at 80%
            .recoveryBonus(Math.min(1.0, totalRecoveryBonus))
            .scoutingBonus(Math.min(1.0, totalScoutingBonus))
            .build();
    }
    
    /**
     * Process monthly staff salaries
     */
    @Scheduled(cron = "0 0 8 1 * *") // First day of month at 8 AM
    public void processMonthlyStaffSalaries() {
        List<Staff> activeStaff = staffRepository.findByStatus(StaffStatus.ACTIVE);
        
        for (Staff staff : activeStaff) {
            if (staff.getClub() != null) {
                Club club = staff.getClub();
                BigDecimal salary = staff.getMonthlySalary();
                
                // Check if club can afford salary
                if (club.getFinance().getBalance().compareTo(salary) >= 0) {
                    club.getFinance().setBalance(
                        club.getFinance().getBalance().subtract(salary)
                    );
                } else {
                    // Club cannot afford salary - fire staff or take loan
                    handleUnpaidStaffSalary(staff);
                }
            }
        }
    }
    
    /**
     * Renew staff contract
     */
    public StaffContract renewContract(Long staffId, RenewContractRequest request) {
        Staff staff = staffRepository.findById(staffId)
            .orElseThrow(() -> new EntityNotFoundException("Staff not found"));
        
        StaffContract currentContract = staffContractRepository
            .findByStaffAndStatus(staff, ContractStatus.ACTIVE)
            .orElseThrow(() -> new EntityNotFoundException("Active contract not found"));
        
        // End current contract
        currentContract.setStatus(ContractStatus.EXPIRED);
        currentContract.setEndDate(LocalDate.now());
        
        // Create new contract
        StaffContract newContract = StaffContract.builder()
            .staff(staff)
            .club(staff.getClub())
            .monthlySalary(request.getNewSalary())
            .signingBonus(request.getSigningBonus())
            .performanceBonus(request.getPerformanceBonus())
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusYears(request.getContractYears()))
            .status(ContractStatus.ACTIVE)
            .terminationFee(calculateTerminationFee(staff, request.getContractYears()))
            .build();
        
        // Update staff
        staff.setMonthlySalary(request.getNewSalary());
        staff.setContractEnd(newContract.getEndDate());
        
        staffContractRepository.save(currentContract);
        staffRepository.save(staff);
        return staffContractRepository.save(newContract);
    }
    
    private BigDecimal calculateStaffSalary(StaffRole role, int ability, int reputation, int experience) {
        double baseSalary = (role.getMinSalary() + role.getMaxSalary()) / 2.0;
        
        // Adjust based on ability (50% weight)
        double abilityMultiplier = 0.5 + (ability / 20.0) * 0.5;
        
        // Adjust based on reputation (30% weight)
        double reputationMultiplier = 0.7 + (reputation / 20.0) * 0.3;
        
        // Adjust based on experience (20% weight)
        double experienceMultiplier = 0.8 + (experience / 40.0) * 0.2;
        
        double finalSalary = baseSalary * abilityMultiplier * reputationMultiplier * experienceMultiplier;
        
        return BigDecimal.valueOf(finalSalary).setScale(0, RoundingMode.HALF_UP);
    }
}
```

### API Endpoints

#### StaffController
```java
@RestController
@RequestMapping("/api/staff")
public class StaffController {
    
    @Autowired
    private StaffService staffService;
    
    @GetMapping("/club/{clubId}")
    public ResponseEntity<List<StaffDTO>> getClubStaff(@PathVariable Long clubId) {
        List<Staff> staff = staffService.getClubStaff(clubId);
        return ResponseEntity.ok(staff.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList()));
    }
    
    @GetMapping("/available")
    public ResponseEntity<List<StaffDTO>> getAvailableStaff(
            @RequestParam(required = false) StaffRole role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Staff> staff = staffService.getAvailableStaff(role, PageRequest.of(page, size));
        return ResponseEntity.ok(staff.getContent().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList()));
    }
    
    @PostMapping("/hire")
    public ResponseEntity<StaffContractDTO> hireStaff(@RequestBody HireStaffRequest request) {
        StaffContract contract = staffService.hireStaff(
            request.getClubId(), 
            request.getStaffId(), 
            request
        );
        return ResponseEntity.ok(convertToDTO(contract));
    }
    
    @PostMapping("/{staffId}/fire")
    public ResponseEntity<Void> fireStaff(
            @PathVariable Long staffId,
            @RequestBody FireStaffRequest request) {
        staffService.fireStaff(staffId, request.getReason());
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{staffId}/renew")
    public ResponseEntity<StaffContractDTO> renewContract(
            @PathVariable Long staffId,
            @RequestBody RenewContractRequest request) {
        StaffContract contract = staffService.renewContract(staffId, request);
        return ResponseEntity.ok(convertToDTO(contract));
    }
    
    @GetMapping("/club/{clubId}/bonuses")
    public ResponseEntity<StaffBonusesDTO> getClubStaffBonuses(@PathVariable Long clubId) {
        StaffBonuses bonuses = staffService.calculateClubStaffBonuses(clubId);
        return ResponseEntity.ok(convertToDTO(bonuses));
    }
    
    @PostMapping("/generate/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StaffDTO>> generateStaff(
            @PathVariable StaffRole role,
            @RequestParam(defaultValue = "10") int count) {
        List<Staff> staff = staffService.generateAvailableStaff(role, count);
        return ResponseEntity.ok(staff.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList()));
    }
}
```

### Frontend Implementation

#### StaffManagement Component (fm-web)
```jsx
import React, { useState, useEffect } from 'react';
import { getClubStaff, getAvailableStaff, hireStaff, fireStaff, getStaffBonuses } from '../services/api';

const StaffManagement = ({ clubId }) => {
    const [clubStaff, setClubStaff] = useState([]);
    const [availableStaff, setAvailableStaff] = useState([]);
    const [staffBonuses, setStaffBonuses] = useState(null);
    const [selectedTab, setSelectedTab] = useState('current');
    const [selectedRole, setSelectedRole] = useState('');
    const [loading, setLoading] = useState(true);

    const staffRoles = [
        'HEAD_COACH', 'ASSISTANT_COACH', 'FITNESS_COACH', 'GOALKEEPING_COACH',
        'YOUTH_COACH', 'HEAD_PHYSIO', 'PHYSIO', 'DOCTOR', 'HEAD_SCOUT', 'SCOUT', 'ANALYST'
    ];

    useEffect(() => {
        loadStaffData();
    }, [clubId]);

    const loadStaffData = async () => {
        try {
            const [staffResponse, bonusesResponse] = await Promise.all([
                getClubStaff(clubId),
                getStaffBonuses(clubId)
            ]);
            setClubStaff(staffResponse.data);
            setStaffBonuses(bonusesResponse.data);
        } catch (error) {
            console.error('Error loading staff data:', error);
        } finally {
            setLoading(false);
        }
    };

    const loadAvailableStaff = async (role = '') => {
        try {
            const response = await getAvailableStaff(role);
            setAvailableStaff(response.data);
        } catch (error) {
            console.error('Error loading available staff:', error);
        }
    };

    const handleHireStaff = async (staffId, contractDetails) => {
        try {
            await hireStaff({
                clubId,
                staffId,
                ...contractDetails
            });
            loadStaffData();
            loadAvailableStaff(selectedRole);
            // Show success notification
        } catch (error) {
            console.error('Error hiring staff:', error);
            // Show error notification
        }
    };

    const handleFireStaff = async (staffId, reason) => {
        try {
            await fireStaff(staffId, { reason });
            loadStaffData();
            // Show success notification
        } catch (error) {
            console.error('Error firing staff:', error);
            // Show error notification
        }
    };

    const getRoleColor = (role) => {
        const colors = {
            HEAD_COACH: '#ff6b35',
            ASSISTANT_COACH: '#f7931e',
            FITNESS_COACH: '#4caf50',
            GOALKEEPING_COACH: '#2196f3',
            YOUTH_COACH: '#9c27b0',
            HEAD_PHYSIO: '#e91e63',
            PHYSIO: '#f06292',
            DOCTOR: '#d32f2f',
            HEAD_SCOUT: '#795548',
            SCOUT: '#8d6e63',
            ANALYST: '#607d8b'
        };
        return colors[role] || '#666';
    };

    const getAbilityStars = (ability) => {
        const stars = Math.ceil(ability / 4); // Convert 1-20 to 1-5 stars
        return '⭐'.repeat(stars) + '☆'.repeat(5 - stars);
    };

    if (loading) return <div>Loading staff...</div>;

    return (
        <div className="staff-management">
            <div className="staff-header">
                <h2>Staff Management</h2>
                
                {staffBonuses && (
                    <div className="staff-bonuses">
                        <h3>Current Staff Bonuses</h3>
                        <div className="bonus-grid">
                            <div className="bonus-item">
                                <span>Training:</span>
                                <span>+{Math.round(staffBonuses.trainingBonus * 100)}%</span>
                            </div>
                            <div className="bonus-item">
                                <span>Motivation:</span>
                                <span>+{Math.round(staffBonuses.motivationBonus * 100)}%</span>
                            </div>
                            <div className="bonus-item">
                                <span>Injury Prevention:</span>
                                <span>-{Math.round(staffBonuses.injuryPreventionBonus * 100)}%</span>
                            </div>
                            <div className="bonus-item">
                                <span>Recovery:</span>
                                <span>+{Math.round(staffBonuses.recoveryBonus * 100)}%</span>
                            </div>
                            <div className="bonus-item">
                                <span>Scouting:</span>
                                <span>+{Math.round(staffBonuses.scoutingBonus * 100)}%</span>
                            </div>
                        </div>
                    </div>
                )}
            </div>

            <div className="staff-tabs">
                <button 
                    className={selectedTab === 'current' ? 'active' : ''}
                    onClick={() => setSelectedTab('current')}
                >
                    Current Staff ({clubStaff.length})
                </button>
                <button 
                    className={selectedTab === 'available' ? 'active' : ''}
                    onClick={() => {
                        setSelectedTab('available');
                        loadAvailableStaff();
                    }}
                >
                    Available Staff
                </button>
            </div>

            {selectedTab === 'current' && (
                <div className="current-staff">
                    <div className="staff-grid">
                        {clubStaff.map(staff => (
                            <div key={staff.id} className="staff-card">
                                <div className="staff-header">
                                    <h4>{staff.name} {staff.surname}</h4>
                                    <span 
                                        className="staff-role"
                                        style={{ backgroundColor: getRoleColor(staff.role) }}
                                    >
                                        {staff.role.replace('_', ' ')}
                                    </span>
                                </div>
                                
                                <div className="staff-info">
                                    <div className="info-row">
                                        <span>Age:</span>
                                        <span>{staff.age}</span>
                                    </div>
                                    <div className="info-row">
                                        <span>Ability:</span>
                                        <span>{getAbilityStars(staff.ability)}</span>
                                    </div>
                                    <div className="info-row">
                                        <span>Reputation:</span>
                                        <span>{getAbilityStars(staff.reputation)}</span>
                                    </div>
                                    <div className="info-row">
                                        <span>Experience:</span>
                                        <span>{staff.experience} years</span>
                                    </div>
                                    <div className="info-row">
                                        <span>Salary:</span>
                                        <span>${staff.monthlySalary.toLocaleString()}/month</span>
                                    </div>
                                </div>

                                <div className="staff-specialization">
                                    <strong>Specialization:</strong>
                                    <span>{staff.specialization?.replace('_', ' ')}</span>
                                </div>

                                <div className="staff-bonuses">
                                    {staff.trainingBonus > 0 && (
                                        <div className="bonus">Training: +{Math.round(staff.trainingBonus * 100)}%</div>
                                    )}
                                    {staff.motivationBonus > 0 && (
                                        <div className="bonus">Motivation: +{Math.round(staff.motivationBonus * 100)}%</div>
                                    )}
                                    {staff.injuryPreventionBonus > 0 && (
                                        <div className="bonus">Injury Prevention: -{Math.round(staff.injuryPreventionBonus * 100)}%</div>
                                    )}
                                    {staff.recoveryBonus > 0 && (
                                        <div className="bonus">Recovery: +{Math.round(staff.recoveryBonus * 100)}%</div>
                                    )}
                                    {staff.scoutingBonus > 0 && (
                                        <div className="bonus">Scouting: +{Math.round(staff.scoutingBonus * 100)}%</div>
                                    )}
                                </div>

                                <div className="staff-contract">
                                    <small>Contract until: {new Date(staff.contractEnd).toLocaleDateString()}</small>
                                </div>

                                <div className="staff-actions">
                                    <button 
                                        className="renew-btn"
                                        onClick={() => {/* Open renew contract modal */}}
                                    >
                                        Renew Contract
                                    </button>
                                    <button 
                                        className="fire-btn"
                                        onClick={() => handleFireStaff(staff.id, 'Performance issues')}
                                    >
                                        Fire
                                    </button>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            )}

            {selectedTab === 'available' && (
                <div className="available-staff">
                    <div className="staff-filters">
                        <select 
                            value={selectedRole}
                            onChange={(e) => {
                                setSelectedRole(e.target.value);
                                loadAvailableStaff(e.target.value);
                            }}
                        >
                            <option value="">All Roles</option>
                            {staffRoles.map(role => (
                                <option key={role} value={role}>
                                    {role.replace('_', ' ')}
                                </option>
                            ))}
                        </select>
                    </div>

                    <div className="staff-grid">
                        {availableStaff.map(staff => (
                            <div key={staff.id} className="staff-card available">
                                <div className="staff-header">
                                    <h4>{staff.name} {staff.surname}</h4>
                                    <span 
                                        className="staff-role"
                                        style={{ backgroundColor: getRoleColor(staff.role) }}
                                    >
                                        {staff.role.replace('_', ' ')}
                                    </span>
                                </div>
                                
                                <div className="staff-info">
                                    <div className="info-row">
                                        <span>Age:</span>
                                        <span>{staff.age}</span>
                                    </div>
                                    <div className="info-row">
                                        <span>Ability:</span>
                                        <span>{getAbilityStars(staff.ability)}</span>
                                    </div>
                                    <div className="info-row">
                                        <span>Reputation:</span>
                                        <span>{getAbilityStars(staff.reputation)}</span>
                                    </div>
                                    <div className="info-row">
                                        <span>Experience:</span>
                                        <span>{staff.experience} years</span>
                                    </div>
                                    <div className="info-row">
                                        <span>Asking Salary:</span>
                                        <span>${staff.monthlySalary.toLocaleString()}/month</span>
                                    </div>
                                </div>

                                <div className="staff-description">
                                    <p>{staff.description}</p>
                                </div>

                                <div className="staff-actions">
                                    <button 
                                        className="hire-btn"
                                        onClick={() => handleHireStaff(staff.id, {
                                            contractYears: 2,
                                            signingBonus: 0,
                                            performanceBonus: 0
                                        })}
                                    >
                                        Hire
                                    </button>
                                    <button 
                                        className="negotiate-btn"
                                        onClick={() => {/* Open negotiation modal */}}
                                    >
                                        Negotiate
                                    </button>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
};

export default StaffManagement;
```

### Integration with Existing Systems

#### Update TrainingService to use staff bonuses
```java
// In TrainingService.java
private Double calculateEffectiveness(Team team) {
    double baseEffectiveness = 1.0;
    
    // Add staff bonuses
    StaffBonuses bonuses = staffService.calculateClubStaffBonuses(team.getClub().getId());
    baseEffectiveness += bonuses.getTrainingBonus();
    
    return Math.min(2.0, baseEffectiveness);
}
```

#### Update InjuryService to use staff bonuses
```java
// In InjuryService.java
public boolean checkForInjury(Player player, Double matchIntensity) {
    double baseProbability = 0.02;
    
    // Apply staff injury prevention bonus
    StaffBonuses bonuses = staffService.calculateClubStaffBonuses(
        player.getTeam().getClub().getId());
    double injuryReduction = bonuses.getInjuryPreventionBonus();
    
    double finalProbability = baseProbability * (1.0 - injuryReduction) * 
                            ageFactor * conditionFactor * matchIntensity;
    
    return RandomUtils.randomValue(0.0, 1.0) < finalProbability;
}
```

### Testing Strategy

#### Unit Tests
```java
@ExtendWith(MockitoExtension.class)
class StaffServiceTest {
    
    @Mock
    private StaffRepository staffRepository;
    
    @InjectMocks
    private StaffService staffService;
    
    @Test
    void testStaffBonusCalculation() {
        Staff headCoach = createTestStaff(StaffRole.HEAD_COACH, 15);
        Staff physio = createTestStaff(StaffRole.PHYSIO, 12);
        
        Club club = createTestClub();
        club.getStaff().addAll(List.of(headCoach, physio));
        
        StaffBonuses bonuses = staffService.calculateClubStaffBonuses(club.getId());
        
        assertThat(bonuses.getTrainingBonus()).isGreaterThan(0);
        assertThat(bonuses.getInjuryPreventionBonus()).isGreaterThan(0);
    }
    
    @Test
    void testStaffHiring() {
        Club club = createTestClub();
        Staff staff = createTestStaff(StaffRole.ASSISTANT_COACH, 10);
        
        HireStaffRequest request = new HireStaffRequest();
        request.setContractYears(2);
        request.setSigningBonus(BigDecimal.valueOf(10000));
        
        StaffContract contract = staffService.hireStaff(club.getId(), staff.getId(), request);
        
        assertThat(contract.getStaff()).isEqualTo(staff);
        assertThat(contract.getClub()).isEqualTo(club);
        assertThat(staff.getClub()).isEqualTo(club);
    }
}
```

### Configuration

#### Application Properties
```properties
# Staff management configuration
fm.staff.salary.processing.day-of-month=1
fm.staff.salary.processing.time=08:00
fm.staff.max-bonus.training=1.0
fm.staff.max-bonus.motivation=1.0
fm.staff.max-bonus.injury-prevention=0.8
fm.staff.max-bonus.recovery=1.0
fm.staff.max-bonus.scouting=1.0
```

## Implementation Notes

1. **Staff Bonuses**: Bonuses should stack but be capped to prevent overpowered combinations
2. **Salary Management**: Clubs should face consequences for not paying staff salaries
3. **Contract Negotiations**: More complex negotiation system could be added later
4. **Staff Development**: Staff could improve their abilities over time
5. **Staff Relationships**: Staff could have relationships affecting team chemistry
6. **Performance Tracking**: Track staff performance and adjust bonuses accordingly
7. **Staff Specializations**: Different specializations should provide different bonuses

## Dependencies

- Club financial system for salary payments
- Training system integration for training bonuses
- Injury system integration for medical staff bonuses
- Player morale system for motivation bonuses
- Scouting system integration (future)
- News system for staff-related news
- Notification system for contract renewals and salary issues