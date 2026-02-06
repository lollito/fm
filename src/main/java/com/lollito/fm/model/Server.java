package com.lollito.fm.model;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "server")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Server implements Serializable{
	
	@Transient
	private static final long serialVersionUID = 1L;
    
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	@GenericGenerator(name = "native", strategy = "native")
	@EqualsAndHashCode.Include
	private Long id;
	
    private String name;
    
    @Column(name="crnt_date")
    private LocalDateTime currentDate;

    @ManyToOne( fetch = FetchType.LAZY )
    @JoinColumn( name = "owner_id" )
    @ToString.Exclude
    private User owner;
    
    @OneToMany(mappedBy = "server", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.LAZY)
	@Builder.Default
	@ToString.Exclude
    private List<League> leagues = new ArrayList<>();
    
	public void addDay(){
		this.currentDate = currentDate.plusDays(1);
	}
	
	public void addLeague(League league) {
		this.leagues.add(league);
	}
	
}
