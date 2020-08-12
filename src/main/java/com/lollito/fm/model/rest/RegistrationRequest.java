package com.lollito.fm.model.rest;

import java.io.Serializable;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lollito.fm.model.Country;

public class RegistrationRequest implements Serializable{
	
	private static final long serialVersionUID = -2922813997308541538L;

	@NotBlank
	@Size(min = 4, max = 40)
	private String name;
	
	@NotBlank
	@Size(min = 4, max = 40)
	private String surname;

	@NotBlank
	@Size(min = 3, max = 15)
	private String username;

	@NotBlank
	@Size(max = 40)
	@Email
	private String email;

	@NotBlank
	@Size(max = 40)
	@Email
	private String emailConfirm;
	
	@NotBlank
	@Size(min = 6, max = 20)
	private String password;
	
	@NotBlank
	@Size(min = 6, max = 20)
    private String passwordConfirm;
	
	@NotNull
	private Long countryId;
	
	@NotBlank
	@Size(min = 3, max = 15)
	private String clubName;
	
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

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
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

	public String getEmailConfirm() {
		return emailConfirm;
	}

	public void setEmailConfirm(String emailConfirm) {
		this.emailConfirm = emailConfirm;
	}

	public String getPasswordConfirm() {
		return passwordConfirm;
	}

	public void setPasswordConfirm(String passwordConfirm) {
		this.passwordConfirm = passwordConfirm;
	}

	public Long getCountryId() {
		return countryId;
	}

	public void setCountryId(Long countryId) {
		this.countryId = countryId;
	}

	public String getClubName() {
		return clubName;
	}

	public void setClubName(String clubName) {
		this.clubName = clubName;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RegistrationRequest [name=");
		builder.append(name);
		builder.append(", surname=");
		builder.append(surname);
		builder.append(", username=");
		builder.append(username);
		builder.append(", email=");
		builder.append(email);
		builder.append(", emailConfirm=");
		builder.append(emailConfirm);
		builder.append(", password=");
		builder.append(password);
		builder.append(", passwordConfirm=");
		builder.append(passwordConfirm);
		builder.append(", countryId=");
		builder.append(countryId);
		builder.append(", clubName=");
		builder.append(clubName);
		builder.append("]");
		return builder.toString();
	}

	
}