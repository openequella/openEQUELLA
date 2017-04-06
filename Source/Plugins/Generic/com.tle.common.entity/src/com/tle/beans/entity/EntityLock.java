package com.tle.beans.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

import com.tle.beans.Institution;

/**
 * @author Nicholas Read
 */
@Entity
@AccessType("field")
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"entity_id"})})
public class EntityLock
{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	@OneToOne(fetch = FetchType.LAZY)
	private BaseEntity entity;

	@Column(length = 40)
	private String userSession;
	private String userID;

	@ManyToOne(fetch = FetchType.LAZY)
	@Index(name = "entityLockInstIndex")
	private Institution institution;

	public EntityLock()
	{
		super();
	}

	public String getUserSession()
	{
		return userSession;
	}

	public String getUserID()
	{
		return userID;
	}

	public void setUserID(String userID)
	{
		this.userID = userID;
	}

	public void setUserSession(String userSession)
	{
		this.userSession = userSession;
	}

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public BaseEntity getEntity()
	{
		return entity;
	}

	public void setEntity(BaseEntity entity)
	{
		this.entity = entity;
	}

	public Institution getInstitution()
	{
		return institution;
	}

	public void setInstitution(Institution institution)
	{
		this.institution = institution;
	}
}
