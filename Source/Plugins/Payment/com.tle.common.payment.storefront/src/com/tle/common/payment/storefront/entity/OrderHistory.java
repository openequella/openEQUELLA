package com.tle.common.payment.storefront.entity;

import java.io.Serializable;
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
import javax.validation.constraints.Min;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * @author Aaron
 */
@Entity
@AccessType("field")
public class OrderHistory implements Serializable
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", insertable = false, updatable = false, nullable = false)
	@XStreamOmitField
	@Index(name = "orderhistOrderIndex")
	private Order order;

	@Lob
	private String comment;

	@Column(length = 255, nullable = false)
	private String userId;

	@Column(nullable = false)
	private Date date;

	/**
	 * The status the Order moved to
	 */
	@Column(nullable = false)
	@Min(0)
	private int status;

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public Order getOrder()
	{
		return order;
	}

	public void setOrder(Order order)
	{
		this.order = order;
	}

	public String getComment()
	{
		return comment;
	}

	public void setComment(String comment)
	{
		this.comment = comment;
	}

	public String getUserId()
	{
		return userId;
	}

	public void setUserId(String userId)
	{
		this.userId = userId;
	}

	public Date getDate()
	{
		return date;
	}

	public void setDate(Date date)
	{
		this.date = date;
	}

	public Order.Status getStatus()
	{
		return Order.Status.values()[status];
	}

	public void setStatus(Order.Status status)
	{
		this.status = status.ordinal();
	}
}
