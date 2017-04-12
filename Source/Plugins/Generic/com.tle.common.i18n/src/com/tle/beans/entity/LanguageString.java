package com.tle.beans.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

import com.tle.beans.IdCloneable;
import com.tle.common.Check;

@Entity
@AccessType("property")
public class LanguageString implements Serializable, IdCloneable
{
	private static final long serialVersionUID = 1L;

	private long id;
	private String locale;
	private int priority;
	private String text;
	private LanguageBundle bundle;

	public LanguageString()
	{
		super();
	}

	@Override
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public long getId()
	{
		return id;
	}

	@Override
	public void setId(long id)
	{
		this.id = id;
	}

	@Column(length = 20, nullable = false)
	@Index(name = "localeIndex")
	public String getLocale()
	{
		return locale;
	}

	public void setLocale(String locale)
	{
		this.locale = locale;
	}

	public int getPriority()
	{
		return priority;
	}

	public void setPriority(int priority)
	{
		this.priority = priority;
	}

	@Lob
	public String getText()
	{
		return text;
	}

	public void setText(String text)
	{
		this.text = text;
	}

	@JoinColumn(nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	@Index(name = "bundleIndex")
	public LanguageBundle getBundle()
	{
		return bundle;
	}

	public void setBundle(LanguageBundle bundle)
	{
		this.bundle = bundle;
	}

	@Override
	public String toString()
	{
		return locale + '=' + text;
	}

	@Override
	public boolean equals(Object obj)
	{
		if( this == obj )
		{
			// Reflexitivity
			return true;
		}
		else if( obj == null )
		{
			// Non-null
			return false;
		}
		else if( this.getClass() != obj.getClass() )
		{
			// Symmetry
			return false;
		}
		else
		{
			return ((LanguageString) obj).getId() == this.getId();
		}
	}

	@Override
	public int hashCode()
	{
		return Check.getHashCode(locale, text);
	}
}
