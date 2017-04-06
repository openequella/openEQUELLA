package com.tle.common.payment.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

import com.tle.beans.IdCloneable;
import com.tle.beans.item.ForeignItemKey;
import com.tle.beans.item.Item;

@Entity
@AccessType("field")
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"item_id"}))
public class PricingTierAssignment implements Serializable, IdCloneable, ForeignItemKey
{
	private static final long serialVersionUID = -7624353626158022945L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@JoinColumn(nullable = false)
	@OneToOne(fetch = FetchType.LAZY)
	@Index(name = "assignmentItem")
	private Item item;

	@JoinColumn(nullable = true)
	@ManyToOne
	@Index(name = "assignmentPurchaseTier")
	private PricingTier purchasePricingTier;

	@JoinColumn(nullable = true)
	@ManyToOne
	@Index(name = "assignmentSubscriptionTier")
	private PricingTier subscriptionPricingTier;

	private boolean freeItem;

	@Override
	public long getId()
	{
		return id;
	}

	@Override
	public void setId(long id)
	{
		this.id = id;
	}

	public Item getItem()
	{
		return item;
	}

	@Override
	public void setItem(Item item)
	{
		this.item = item;
	}

	public PricingTier getPurchasePricingTier()
	{
		return purchasePricingTier;
	}

	public void setPurchasePricingTier(PricingTier purchasePricingTier)
	{
		this.purchasePricingTier = purchasePricingTier;
	}

	public PricingTier getSubscriptionPricingTier()
	{
		return subscriptionPricingTier;
	}

	public void setSubscriptionPricingTier(PricingTier subscriptionPricingTier)
	{
		this.subscriptionPricingTier = subscriptionPricingTier;
	}

	public boolean isFreeItem()
	{
		return freeItem;
	}

	public void setFreeItem(boolean freeItem)
	{
		this.freeItem = freeItem;
	}
}
