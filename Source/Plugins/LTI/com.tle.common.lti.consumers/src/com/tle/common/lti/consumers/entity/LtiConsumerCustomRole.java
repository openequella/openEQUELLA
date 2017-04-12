package com.tle.common.lti.consumers.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.AccessType;

import com.tle.beans.IdCloneable;

@Entity
@AccessType("field")
public class LtiConsumerCustomRole implements Serializable, IdCloneable
{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(length = 255)
	private String ltiRole;
	@Column(length = 255)
	private String equellaRole;

	@JoinColumn(name = "lti_consumer_id", insertable = false, updatable = false, nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	private LtiConsumer consumer;

	@Override
	public void setId(long id)
	{
		this.id = id;
	}

	@Override
	public long getId()
	{
		return id;
	}

	public LtiConsumer getConsumer()
	{
		return consumer;
	}

	public void setConsumer(LtiConsumer consumer)
	{
		this.consumer = consumer;
	}

	public String getEquellaRole()
	{
		return equellaRole;
	}

	public void setEquellaRole(String equellaRole)
	{
		this.equellaRole = equellaRole;
	}

	public String getLtiRole()
	{
		return ltiRole;
	}

	public void setLtiRole(String ltiRole)
	{
		this.ltiRole = ltiRole;
	}
}
