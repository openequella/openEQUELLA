package com.tle.core.replicatedcache.dao;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.AccessType;

import com.dytech.devlib.Base64;
import com.tle.beans.Institution;

@Entity
@AccessType("field")
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"institution_id", "cacheId", "key"})})
public class CachedValue
{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@JoinColumn(nullable = false)
	@OneToOne(fetch = FetchType.LAZY)
	private Institution institution;

	private String cacheId;
	private String key;
	@Lob
	private String value;
	private Date ttl;

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public Institution getInstitution()
	{
		return institution;
	}

	public void setInstitution(Institution institution)
	{
		this.institution = institution;
	}

	public String getCacheId()
	{
		return cacheId;
	}

	public void setCacheId(String cacheId)
	{
		this.cacheId = cacheId;
	}

	public String getKey()
	{
		return key;
	}

	public void setKey(String key)
	{
		this.key = key;
	}

	public byte[] getValue()
	{
		return new Base64().decode(value);
	}

	public void setValue(byte[] value)
	{
		this.value = new Base64().encode(value);
	}

	public Date getTtl()
	{
		return ttl;
	}

	public void setTtl(Date ttl)
	{
		this.ttl = ttl;
	}
}
