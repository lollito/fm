package com.lollito.fm.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "staff")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Staff implements Serializable {

    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    private String name;
    private String surname;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDate birth;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    @ToString.Exclude
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
    @ToString.Exclude
    private Country nationality;

    private String description; // Staff background/bio
    private Integer experience; // Years of experience

    @Transient
    public int getAge() {
        if (birth == null) return 0;
        return java.time.Period.between(birth, LocalDate.now()).getYears();
    }
}
