package ru.rcslabs.webcall.server.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * A Webcall application configuration.
 *
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "name" }))
public class Application implements Serializable {

	private static final long serialVersionUID = 1L;

	public enum State {
		ENABLED, DISABLED
	}

	public enum CaptchaState {
		ENABLED, DISABLED
	}
	
	public enum Security {
		ENABLED, DISABLED
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	private String name;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "application")
	private Set<ApplicationParameter> parameters = new HashSet<ApplicationParameter>();

	@Enumerated(EnumType.STRING)
	private State state = State.ENABLED;

	@Enumerated(EnumType.STRING)
	private CaptchaState captchaState = CaptchaState.DISABLED;
	
	@Enumerated(EnumType.STRING)
	private Security security = Security.DISABLED;
	
	private String allowedHosts;

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<ApplicationParameter> getParameters() {
		return parameters;
	}

	public void setParameters(Set<ApplicationParameter> parameters) {
		this.parameters = parameters;
		for (ApplicationParameter parameter : parameters) {
			parameter.setApplication(this);
		}
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		// #923
		// this.state = state;
		if (state==null) {
			this.state = State.DISABLED;
		} else {
			this.state = state;
		}
		// End #923
	}

	public CaptchaState getCaptchaState() {
		return captchaState;
	}

	public void setCaptchaState(CaptchaState captchaState) {
		// #923
		// this.captchaState = captchaState;
		if (captchaState==null) {
			this.captchaState = CaptchaState.DISABLED;
		} else {
			this.captchaState = captchaState;
		}
		// End #923
	}

	public Security getSecurity() {
		return security;
	}

	public void setSecurity(Security security) {
		// #923
		// this.security = security;
		if (security==null) {
			this.security = Security.DISABLED;
		} else {
			this.security = security;
		}
		// End #923
	}

	public String getAllowedHosts() {
		return allowedHosts;
	}

	public void setAllowedHosts(String allowedHosts) {
		this.allowedHosts = allowedHosts;
	}

	@Override
	public String toString() {
		return String
				.format("Application [id=%s, name=%s, parameters=%s, state=%s, captchaState=%s, security=%s, allowedHosts=%s]",
						id, name, parameters, state, captchaState, security, allowedHosts);
	}
}
