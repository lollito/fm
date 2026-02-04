package com.lollito.fm.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "sponsor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Sponsor implements Serializable {
	
	@Transient
	private static final long serialVersionUID = 1L;
    
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	@GenericGenerator(name = "native", strategy = "native")
	@EqualsAndHashCode.Include
	private Long id;
	
	private String name;
    private String logo;
    private String website;
    private String industry;

    @Enumerated(EnumType.STRING)
    private SponsorTier tier;

    private BigDecimal maxAnnualBudget;
    private BigDecimal minClubReputation;
    private Integer minLeagueLevel;

    // Sponsor preferences
    private Boolean prefersWinningTeams;
    private Boolean prefersYoungTeams;
    private Boolean prefersLocalTeams;
    private String preferredRegion;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<SponsorshipType> availableTypes = new HashSet<>();

    @OneToMany(mappedBy = "sponsor", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<SponsorshipDeal> deals = new ArrayList<>();

    @OneToMany(mappedBy = "sponsor", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<SponsorshipOffer> offers = new ArrayList<>();
    
    private Boolean isActive;
    private LocalDate createdDate;
}
