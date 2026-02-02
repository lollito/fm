package com.lollito.fm.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
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
import com.lollito.fm.model.rest.RegistrationRequest;
import com.lollito.fm.utils.Level;

@Entity
@Table(name="user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class User implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	@GenericGenerator(name = "native", strategy = "native")
	@EqualsAndHashCode.Include
	private Long id;
	
	private String username;
	
	private String name;
	
	private String surname;
	
	private String email;
	
	@JsonIgnore
	private String password;
	
	@ManyToOne( fetch = FetchType.LAZY  )
	@JoinColumn( name = "country_id" )
	@ToString.Exclude
	private Country country;
	
	@jakarta.persistence.Convert(converter = org.hibernate.type.YesNoConverter.class)
	@Builder.Default
	private Boolean active = Boolean.FALSE;

	private String activationToken;
	
	@OneToOne( fetch = FetchType.LAZY  )
   	@JoinColumn( name = "club_id" )
	@ToString.Exclude
    private Club club;
	
	@ManyToMany
	@Builder.Default
	@ToString.Exclude
	private Set<Role> roles = new HashSet<>();
	
	@Builder.Default
	private double experience = 0; 
	 
	public User(String username, String name, String surname, String email, String password) {
		this();
		this.username = username;
		this.name = name;
		this.surname = surname;
		this.email = email;
		this.password = password;
	}


	public User(RegistrationRequest request) {
		this();
		this.username = request.getUsername();
		this.name = request.getName();
		this.surname = request.getSurname();
		this.email = request.getEmail();
		this.password = request.getPassword();
	}

	@Transient
	public Integer getLevel() {
		return Level.level(experience);
	}
	
	@Transient
	public Double getLevelToExp() {
		return Level.levelToExp(Level.level(experience));
	}
	
	@Transient
	public Double getLevelProgress() {
		return Level.getLevelProgress(experience);
	}
	
}
