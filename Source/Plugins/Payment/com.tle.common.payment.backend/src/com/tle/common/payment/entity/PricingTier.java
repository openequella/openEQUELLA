package com.tle.common.payment.entity;

import javax.persistence.Entity;

import org.hibernate.annotations.AccessType;

import com.tle.beans.entity.BaseEntity;

@SuppressWarnings("nls")
@Entity
@AccessType("field")
public class PricingTier extends BaseEntity
{
	public static final String ENTITY_TYPE = "TIER";
	private static final long serialVersionUID = 1L;

	/**
	 * If not purchase then it's subscription
	 */
	private boolean purchase;

	public boolean isPurchase()
	{
		return purchase;
	}

	public void setPurchase(boolean purchase)
	{
		this.purchase = purchase;
	}
}
