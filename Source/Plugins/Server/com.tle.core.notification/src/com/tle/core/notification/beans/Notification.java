package com.tle.core.notification.beans;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.tle.beans.Institution;

@Entity
@AccessType("field")
@SuppressWarnings("nls")
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"institution_id", "itemid", "reason", "userTo"}))
public class Notification
{
	public static final String REASON_WENTLIVE = "wentlive";
	public static final String REASON_REJECTED = "rejected";
	public static final String REASON_BADURL = "badurl";
	public static final String REASON_WENTLIVE2 = "wentliv2";
	public static final String REASON_OVERDUE = "overdue";
	public static final String REASON_MODERATE = "moderate";

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	@Index(name = "inst_idx")
	@XStreamOmitField
	private Institution institution;

	@Column(length = 255, nullable = false)
	@Index(name = "itemid_idx")
	private String itemid;
	@Column(length = 50, nullable = false)
	@Index(name = "itemidonly_idx")
	private String itemidOnly;
	@Column(length = 8, nullable = false)
	private String reason;

	@Column(length = 255, nullable = false)
	@Index(name = "userto_idx")
	private String userTo;
	@Column(nullable = false)
	private Date date;

	@Index(name = "processed_idx")
	private boolean processed;
	@Index(name = "batched_idx")
	private boolean batched;

	@Column
	@Index(name = "lastattempt_idx")
	private Date lastAttempt;
	@Column(length = 36)
	@Index(name = "attempt_idx")
	private String attemptId;

	// Used by older institutions on import
	@Transient
	private transient Object extras; // NOSONAR

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public String getItemid()
	{
		return itemid;
	}

	public void setItemid(String itemid)
	{
		this.itemid = itemid;
	}

	public String getReason()
	{
		return reason;
	}

	public void setReason(String reason)
	{
		this.reason = reason;
	}

	public Institution getInstitution()
	{
		return institution;
	}

	public void setInstitution(Institution institution)
	{
		this.institution = institution;
	}

	public String getUserTo()
	{
		return userTo;
	}

	public void setUserTo(String userTo)
	{
		this.userTo = userTo;
	}

	public Date getDate()
	{
		return date;
	}

	public void setDate(Date date)
	{
		this.date = date;
	}

	public String getItemidOnly()
	{
		return itemidOnly;
	}

	public void setItemidOnly(String itemidOnly)
	{
		this.itemidOnly = itemidOnly;
	}

	public boolean isProcessed()
	{
		return processed;
	}

	public void setProcessed(boolean processed)
	{
		this.processed = processed;
	}

	public boolean isBatched()
	{
		return batched;
	}

	public void setBatched(boolean batched)
	{
		this.batched = batched;
	}

	public Date getLastAttempt()
	{
		return lastAttempt;
	}

	public void setLastAttempt(Date lastAttempt)
	{
		this.lastAttempt = lastAttempt;
	}

	public String getAttemptId()
	{
		return attemptId;
	}

	public void setAttemptId(String attemptId)
	{
		this.attemptId = attemptId;
	}
}
