package ru.rcslabs.webcall.server.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.codehaus.jackson.annotate.JsonBackReference;

/**
 * An application configuration parameter.
 *
 */
@Entity
public class ApplicationParameter implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private long id;
	
	@ManyToOne
	@JsonBackReference
	private Application application;
	
	@Column(nullable = false)
	private String name;
	
	@Column(nullable = false)
	private String value;

	public long getId() {
		return id;
	}

	public Application getApplication() {
		return application;
	}
	
	public void setApplication(Application application) {
		this.application = application;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return String.format("ApplicationParameter [id=%s, name=%s, value=%s]",
				id, name, value);
	}
}
