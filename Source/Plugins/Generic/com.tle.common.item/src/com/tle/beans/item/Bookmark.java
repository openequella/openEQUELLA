package com.tle.beans.item;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Index;

import com.tle.beans.Institution;

@Entity
@AccessType("field")
public class Bookmark implements Serializable, ForeignItemKey
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@JoinColumn(nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	@Index(name = "bookmarkItem")
	private Item item;

	@ElementCollection(fetch = FetchType.LAZY)
	@JoinTable(name = "bookmark_keywords")
	@Fetch(value = FetchMode.SUBSELECT)
	@Column(name = "element")
	private Collection<String> keywords;

	private Date dateModified;

	@Column(nullable = false)
	private String owner;

	@JoinColumn(nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	@Index(name = "bmkInstitutionIndex")
	private Institution institution;

	private boolean alwaysLatest;

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
		return this.id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public Item getItem()
	{
		return this.item;
	}

	@Override
	public void setItem(Item item)
	{
		this.item = item;
	}

	public Collection<String> getKeywords()
	{
		return this.keywords;
	}

	public void setKeywords(Collection<String> keywords)
	{
		this.keywords = keywords;
	}

	public String getOwner()
	{
		return owner;
	}

	public void setOwner(String owner)
	{
		this.owner = owner;
	}

	public Date getDateModified()
	{
		return dateModified;
	}

	public void setDateModified(Date dateModified)
	{
		this.dateModified = dateModified;
	}

	public boolean isAlwaysLatest()
	{
		return alwaysLatest;
	}

	public void setAlwaysLatest(boolean alwaysLatest)
	{
		this.alwaysLatest = alwaysLatest;
	}
}
