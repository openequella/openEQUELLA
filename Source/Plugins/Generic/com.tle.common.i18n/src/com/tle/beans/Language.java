package com.tle.beans;

import java.io.Serializable;
import java.util.Locale;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;

@Entity
@AccessType("field")
public class Language implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	@Column(length = 15, nullable = false)
	@Type(type = "blankable")
	private String language = ""; //$NON-NLS-1$
	@Column(length = 15, nullable = false)
	@Type(type = "blankable")
	private String country = ""; //$NON-NLS-1$
	@Column(length = 15, nullable = false)
	@Type(type = "blankable")
	private String variant = ""; //$NON-NLS-1$
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	@Index(name = "languageInstitution")
	private Institution institution;

	public Institution getInstitution()
	{
		return institution;
	}

	public void setInstitution(Institution institution)
	{
		this.institution = institution;
	}

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public String getCountry()
	{
		return country;
	}

	public void setCountry(String country)
	{
		this.country = country;
	}

	public String getLanguage()
	{
		return language;
	}

	public void setLanguage(String language)
	{
		this.language = language;
	}

	public String getVariant()
	{
		return variant;
	}

	public void setVariant(String variant)
	{
		this.variant = variant;
	}

	public Locale getLocale()
	{
		return new Locale(language, country, variant);
	}

}
