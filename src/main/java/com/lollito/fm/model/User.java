package com.lollito.fm.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lollito.fm.model.rest.RegistrationRequest;
import com.lollito.fm.utils.Level;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
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
	
	@Column(name = "first_name")
	private String firstName;

	@Column(name = "last_name")
	private String lastName;

	@Column(name = "phone_number")
	private String phoneNumber;

	@Column(name = "date_of_birth")
	private LocalDate dateOfBirth;

	@Column(name = "country")
	private String countryString;

	@Column(name = "preferred_language")
	private String preferredLanguage;

	@Column(name = "timezone")
	private String timezone;

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

	@Column(name = "is_active")
	@Builder.Default
	private Boolean isActive = true;

	@Column(name = "is_verified")
	@Builder.Default
	private Boolean isVerified = false;

	@Column(name = "is_banned")
	@Builder.Default
	private Boolean isBanned = false;

	@Column(name = "ban_reason")
	private String banReason;

	@Column(name = "banned_until")
	private LocalDateTime bannedUntil;

	@Column(name = "banned_by")
	private String bannedBy;

	// Login tracking
	@Column(name = "last_login_date")
	private LocalDateTime lastLoginDate;

	@Column(name = "last_login_ip")
	private String lastLoginIp;

	@Column(name = "failed_login_attempts")
	@Builder.Default
	private Integer failedLoginAttempts = 0;

	@Column(name = "account_locked_until")
	private LocalDateTime accountLockedUntil;

	// Account creation and modification
	@Column(name = "created_date")
	private LocalDateTime createdDate;

	@Column(name = "created_by")
	private String createdBy;

	@Column(name = "last_modified_date")
	private LocalDateTime lastModifiedDate;

	@Column(name = "last_modified_by")
	private String lastModifiedBy;

	// Password management
	@Column(name = "password_changed_date")
	private LocalDateTime passwordChangedDate;

	@Column(name = "password_reset_token")
	private String passwordResetToken;

	@Column(name = "password_reset_token_expiry")
	private LocalDateTime passwordResetTokenExpiry;

	@Column(name = "email_verification_token")
	private String emailVerificationToken;

	@Column(name = "email_verification_token_expiry")
	private LocalDateTime emailVerificationTokenExpiry;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	@Builder.Default
	private List<UserSession> sessions = new ArrayList<>();

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	@Builder.Default
	private List<UserActivity> activities = new ArrayList<>();

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	@Builder.Default
	private List<UserNotification> notifications = new ArrayList<>();

	@Enumerated(EnumType.STRING)
	private AdminRole adminRole;

	@OneToMany(mappedBy = "adminUser", cascade = CascadeType.ALL)
	@Builder.Default
	private List<AdminAction> adminActions = new ArrayList<>();

	private String activationToken;
	
	@OneToOne( fetch = FetchType.LAZY  )
   	@JoinColumn( name = "club_id" )
	@ToString.Exclude
    private Club club;
	
    @ManyToOne( fetch = FetchType.LAZY )
    @JoinColumn( name = "server_id" )
    @ToString.Exclude
    private Server server;

	@ManyToMany
	@Builder.Default
	@ToString.Exclude
	private Set<Role> roles = new HashSet<>();
	
	@Builder.Default
	private double experience = 0; 

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	@Builder.Default
	private List<UserAchievement> achievements = new ArrayList<>();
	 
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
