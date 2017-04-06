package com.tle.common.payment.storefront.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Min;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.IndexColumn;

import com.google.common.collect.Lists;
import com.tle.beans.Institution;

/**
 * @author Aaron
 */
@Entity
// What a load of bollocks. Hibernate should just handle this
@Table(name = "\"Order\"")
@AccessType("field")
public class Order implements Serializable
{
	private static final long serialVersionUID = 1L;

	/*
	 * Do not re-order this enum!
	 */
	public enum Status
	{
		CART, APPROVAL, REJECTED, PAYMENT, COMPLETE, PENDING
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(length = 40, nullable = false)
	@Index(name = "orderUuidIndex")
	private String uuid;

	@JoinColumn(nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	@Index(name = "orderInstitutionIndex")
	private Institution institution;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@Cascade(org.hibernate.annotations.CascadeType.ALL)
	@Fetch(value = FetchMode.SUBSELECT)
	@JoinColumn(name = "order_id", nullable = false)
	private List<OrderStorePart> storeParts = Lists.newArrayList();

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@IndexColumn(name = "histindex", nullable = false)
	@Cascade(org.hibernate.annotations.CascadeType.ALL)
	@Fetch(value = FetchMode.SUBSELECT)
	@JoinColumn(name = "order_id", nullable = false)
	private List<OrderHistory> history = Lists.newArrayList();

	@Column(length = 255, nullable = false)
	private String createdBy;
	// Date of first submission (ie when cart became an order)
	@Column
	private Date createdDate;

	@Column
	private String approversExpression;
	@Column
	private String payersExpression;

	// The following 3 fields should be the same as the latest order history
	// details
	/**
	 * Last Submit, Reject etc date
	 */
	@Column
	private Date lastActionDate;

	@Column(length = 255)
	private String lastActionUser;

	@Min(0)
	private int status;

	@Column(length = 40, nullable = true)
	@Index(name = "origorderUuidIndex")
	private String originalOrderUuid;

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}

	public Institution getInstitution()
	{
		return institution;
	}

	public void setInstitution(Institution institution)
	{
		this.institution = institution;
	}

	public String getCreatedBy()
	{
		return createdBy;
	}

	public void setCreatedBy(String createdBy)
	{
		this.createdBy = createdBy;
	}

	public Date getCreatedDate()
	{
		return createdDate;
	}

	public void setCreatedDate(Date createdDate)
	{
		this.createdDate = createdDate;
	}

	public Date getLastActionDate()
	{
		return lastActionDate;
	}

	public void setLastActionDate(Date lastActionDate)
	{
		this.lastActionDate = lastActionDate;
	}

	public String getLastActionUser()
	{
		return lastActionUser;
	}

	public void setLastActionUser(String lastActionUser)
	{
		this.lastActionUser = lastActionUser;
	}

	public String getApproversExpression()
	{
		return approversExpression;
	}

	public void setApproversExpression(String approversExpression)
	{
		this.approversExpression = approversExpression;
	}

	public String getPayersExpression()
	{
		return payersExpression;
	}

	public void setPayersExpression(String payersExpression)
	{
		this.payersExpression = payersExpression;
	}

	public Status getStatus()
	{
		return Status.values()[status];
	}

	public void setStatus(Status status)
	{
		this.status = status.ordinal();
	}

	public List<OrderStorePart> getStoreParts()
	{
		return storeParts;
	}

	public void setStoreParts(List<OrderStorePart> storeParts)
	{
		this.storeParts = storeParts;
	}

	public List<OrderHistory> getHistory()
	{
		return history;
	}

	public void setHistory(List<OrderHistory> history)
	{
		this.history = history;
	}

	public String getOriginalOrderUuid()
	{
		return originalOrderUuid;
	}

	public void setOriginalOrderUuid(String originalOrderUuid)
	{
		this.originalOrderUuid = originalOrderUuid;
	}
}
