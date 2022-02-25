package br.com.beilke.api.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@Entity(name = "password_reset_tokens")
public class PasswordResetToken implements Serializable {

	private static final long serialVersionUID = -3316505264563905684L;

	@Id
	@GeneratedValue
	private long id;

	private String token;

	@OneToOne
	@JoinColumn(name = "users_id")
	private GeneralUser userDetails;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public GeneralUser getUserDetails() {
		return userDetails;
	}

	public void setUserDetails(GeneralUser userDetails) {
		this.userDetails = userDetails;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}



}
