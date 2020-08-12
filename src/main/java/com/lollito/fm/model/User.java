package com.lollito.fm.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lollito.fm.model.rest.RegistrationRequest;

@Entity
@Table(name="user")
public class User implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
	@GenericGenerator(name = "native", strategy = "native")
	private Long id;
	
	private String username;
	
	private String name;
	
	private String surname;
	
	private String email;
	
	@JsonIgnore
	private String password;
	
	@ManyToOne( fetch = FetchType.LAZY  )
	@JoinColumn( name = "country_id" )
	private Country country;
	
	@Type(type = "yes_no")
	private Boolean active = Boolean.FALSE;

	private String activationToken;
	
	@OneToOne( fetch = FetchType.LAZY  )
   	@JoinColumn( name = "club_id" )
//    @JsonIgnore
    private Club club;
	
	@ManyToMany
	private Set<Role> roles = new HashSet<>();
	 
	public User() {
		
	}
	
	public User(String username, String name, String surname, String email, String password) {
		this.username = username;
		this.name = name;
		this.surname = surname;
		this.email = email;
		this.password = password;
	}


	public User(RegistrationRequest request) {
		this.username = request.getUsername();
		this.name = request.getName();
		this.surname = request.getSurname();
		this.email = request.getEmail();
		this.password = request.getPassword();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Country getCountry() {
		return country;
	}

	public void setCountry(Country country) {
		this.country = country;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public String getActivationToken() {
		return activationToken;
	}

	public void setActivationToken(String activationToken) {
		this.activationToken = activationToken;
	}

	public Club getClub() {
		return club;
	}

	public void setClub(Club club) {
		this.club = club;
	}

	public Set<Role> getRoles() {
		return roles;
	}

	public void setRoles(Set<Role> roles) {
		this.roles = roles;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(11, 121).append(id).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof User)) {
			return false;
		} else if (this == obj) {
			return true;
		} else {
			User other = (User) obj;
			return new EqualsBuilder().append(id, other.id).isEquals();
		}
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
				.append("id", id)
				.append("username", username)
				.append("email", email)
				.append("country", country)
				//.append("players", players)
				.toString();
	}
}
