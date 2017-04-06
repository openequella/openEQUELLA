/*
 * Created on Oct 27, 2005
 */
package com.tle.beans.item;

import java.io.Serializable;

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

@Entity
@AccessType("field")
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"item_id"})})
public class ItemLock implements Serializable
{
	private static final long serialVersionUID = 1L;

	// Primary key and foreign key to base entity table.
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	@OneToOne(fetch = FetchType.LAZY)
	private Item item;

	@Column(length = 40)
	private String userSession;
	private String userID;

	@ManyToOne(fetch = FetchType.LAZY)
	@Index(name = "itemLockInstIndex")
	private Institution institution;

	public ItemLock()
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

	public Item getItem()
	{
		return item;
	}

	public void setItem(Item item)
	{
		this.item = item;
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
