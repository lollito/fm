package com.lollito.fm.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "scouting_assignment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "reports")
public class ScoutingAssignment implements Serializable {

    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scout_id")
    private Scout scout;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_player_id")
    private Player targetPlayer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_club_id")
    private Club targetClub; // For general club scouting

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_region_id")
    private Country targetRegion; // For regional scouting

    @Enumerated(EnumType.STRING)
    private ScoutingType type; // PLAYER, CLUB, REGION, OPPOSITION

    @Enumerated(EnumType.STRING)
    private AssignmentStatus status; // ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED

    private LocalDate assignedDate;
    private LocalDate completionDate;
    private LocalDate expectedCompletionDate;

    private Integer priority; // 1-5, affects resource allocation
    private String instructions; // Special instructions for scout

    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL)
    @Builder.Default
    private List<ScoutingReport> reports = new ArrayList<>();
}
