package com.tle.core.pss.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

import com.tle.beans.Institution;
import com.tle.beans.item.Item;

@Entity
@AccessType("field")
public class PssCallbackLog
{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@JoinColumn(nullable = true)
	@OneToOne(fetch = FetchType.LAZY)
	@Index(name = "pssItemIndex")
	private Item item;

	@Column(nullable = false)
	@Index(name = "pssTrackingNumberIndex")
	private int trackingNumber;

	@Column(nullable = false)
	private Date lastAttempt;

	@Column(nullable = false)
	private int attemptNumber;

	@Lob
	private String status;

	@Lob
	private String message;

	@JoinColumn(nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	@Index(name = "pssInstitutionIndex")
	private Institution institution;

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

	public int getTrackingNumber()
	{
		return trackingNumber;
	}

	public void setTrackingNumber(int trackingNumber)
	{
		this.trackingNumber = trackingNumber;
	}

	public Date getLastAttempt()
	{
		return lastAttempt;
	}

	public void setLastAttempt(Date lastAttempt)
	{
		this.lastAttempt = lastAttempt;
	}

	public int getAttemptNumber()
	{
		return attemptNumber;
	}

	public void setAttemptNumber(int attemptNumber)
	{
		this.attemptNumber = attemptNumber;
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
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
