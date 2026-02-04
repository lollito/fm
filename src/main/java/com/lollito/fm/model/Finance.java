package com.lollito.fm.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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
@Table(name = "finance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Finance implements Serializable {
	
	@Transient
	private static final long serialVersionUID = 1L;
    
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	@GenericGenerator(name = "native", strategy = "native")
	@EqualsAndHashCode.Include
	private Long id;

    @OneToOne(mappedBy = "finance")
    @ToString.Exclude
    private Club club;
	
	@Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;
    
    @Builder.Default
    private BigDecimal monthlyBudget = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal seasonBudget = BigDecimal.ZERO;

    // Revenue tracking
    @Builder.Default
    private BigDecimal totalRevenue = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal matchdayRevenue = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal sponsorshipRevenue = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal merchandiseRevenue = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal transferRevenue = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal prizeMoneyRevenue = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal tvRightsRevenue = BigDecimal.ZERO;

    // Expense tracking
    @Builder.Default
    private BigDecimal totalExpenses = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal playerSalaries = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal staffSalaries = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal facilityMaintenance = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal transferExpenses = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal operationalCosts = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal loanInterest = BigDecimal.ZERO;

    // Financial health indicators
    @Builder.Default
    private BigDecimal netWorth = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal debt = BigDecimal.ZERO;
    @Builder.Default
    private Double profitMargin = 0.0;

    private LocalDate lastUpdated;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn( name = "finance_id" )
	@Builder.Default
	@ToString.Exclude
    private List<Sponsorship> sponsorships = new ArrayList<>();
    
    @OneToMany(mappedBy = "finance", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<FinancialTransaction> transactions = new ArrayList<>();

    @OneToMany(mappedBy = "finance", cascade = CascadeType.ALL)
    @Builder.Default
    @ToString.Exclude
    private List<FinancialReport> reports = new ArrayList<>();

	public Finance(BigDecimal balance) {
		this();
		this.balance = balance;
	}

}
