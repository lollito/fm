package com.lollito.fm.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "contract")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Contract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    @JsonIgnore
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    private Club club;

    private BigDecimal weeklySalary;
    private BigDecimal signingBonus;
    private BigDecimal loyaltyBonus;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDate startDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDate endDate;

    private BigDecimal releaseClause;
    private Boolean hasReleaseClause;

    @Enumerated(EnumType.STRING)
    private ContractStatus status; // ACTIVE, EXPIRED, TERMINATED, NEGOTIATING

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL)
    @Builder.Default
    private List<ContractClause> clauses = new ArrayList<>();

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL)
    @Builder.Default
    private List<PerformanceBonus> performanceBonuses = new ArrayList<>();

    private Integer automaticExtensionYears; // Auto-extend if conditions met
    private String automaticExtensionConditions;

    private LocalDateTime lastNegotiationDate;
    private Integer negotiationAttempts;
}
