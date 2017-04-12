package com.tle.common.payment.entity;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.AccessType;

import com.google.common.collect.Sets;
import com.tle.beans.entity.BaseEntity;

@SuppressWarnings("nls")
@Entity
@AccessType("field")
public class PaymentGateway extends BaseEntity
{
	private static final long serialVersionUID = 1L;
	public static final String ENTITY_TYPE = "PAYMENT_GATEWAY";

	@ManyToMany(fetch = FetchType.LAZY)
	private Set<Region> regions = Sets.newHashSet();

	@Column(length = 16, nullable = false)
	private String gatewayType;

	@Transient
	private Object extraData;

	public PaymentGateway()
	{
	}

	public PaymentGateway(String gatewayType)
	{
		this.gatewayType = gatewayType;
	}

	public Set<Region> getRegions()
	{
		return regions;
	}

	public void setRegions(Set<Region> regions)
	{
		this.regions = regions;
	}

	public String getGatewayType()
	{
		return gatewayType;
	}

	public void setGatewayType(String gatewayType)
	{
		this.gatewayType = gatewayType;
	}

	public Object getExtraData()
	{
		return extraData;
	}

	public void setExtraData(Object extraData)
	{
		this.extraData = extraData;
	}
}
