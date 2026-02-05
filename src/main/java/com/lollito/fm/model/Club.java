package com.lollito.fm.model;
import java.io.Serializable;
import java.time.LocalDate;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "club")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Club implements Serializable{
	
	@Transient
	private static final long serialVersionUID = 1L;
    
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	@GenericGenerator(name = "native", strategy = "native")
	@EqualsAndHashCode.Include
	private Long id;
	
    private String name;
    
    private LocalDate foundation;
    
    private String logoURL;
    
    @OneToOne( fetch = FetchType.LAZY , cascade = CascadeType.ALL )
	@JoinColumn( name = "team_id" )
    @JsonIgnore
	@ToString.Exclude
    private Team team;
    
    @OneToOne( fetch = FetchType.LAZY , cascade = CascadeType.ALL )
	@JoinColumn( name = "under18_id" )
    @JsonIgnore
	@ToString.Exclude
    private Team under18;
    
    @OneToOne( fetch = FetchType.LAZY , cascade = CascadeType.ALL )
	@JoinColumn( name = "stadium_id" )
    @JsonIgnore
	@ToString.Exclude
    private Stadium stadium;
    
    @OneToOne( fetch = FetchType.LAZY , cascade = CascadeType.ALL )
	@JoinColumn( name = "training_facility_id" )
    @JsonIgnore
	@ToString.Exclude
    private TrainingFacility trainingFacility;

    @OneToOne( fetch = FetchType.LAZY , cascade = CascadeType.ALL )
	@JoinColumn( name = "medical_center_id" )
    @JsonIgnore
	@ToString.Exclude
    private MedicalCenter medicalCenter;

    @OneToOne( fetch = FetchType.LAZY , cascade = CascadeType.ALL )
	@JoinColumn( name = "youth_academy_id" )
    @JsonIgnore
	@ToString.Exclude
    private YouthAcademy youthAcademy;

    @ManyToOne( fetch = FetchType.LAZY  )
	@JoinColumn( name = "league_id" )
    @JsonIgnore
	@ToString.Exclude
    private League league;
    
    @OneToOne( fetch = FetchType.LAZY, mappedBy="club" )
    @JsonIgnore
	@ToString.Exclude
    private User user;
    
    @OneToOne( fetch = FetchType.LAZY , cascade = CascadeType.ALL )
	@JoinColumn( name = "finance_id" )
	@ToString.Exclude
	private Finance finance;
    
}
