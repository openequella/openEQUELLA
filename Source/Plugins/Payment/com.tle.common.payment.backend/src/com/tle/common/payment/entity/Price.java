package com.tle.common.payment.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Currency;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Min;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

import com.tle.beans.IdCloneable;

/**
 * Assigned against a tier + subscription period (if any, e.g. not for a
 * purchase tier) + optionally a catalogue + optionally a region
 */
@Entity
@AccessType("field")
public class Price implements Serializable, IdCloneable
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	/**
	 * Will be disabled for subscription periods that are turned off
	 */
	private boolean enabled;

	/**
	 * Cents (or whatever the currency is). You can use
	 * Currency.getDefaultFractionDigits() to convert into floating point for
	 * display purposes
	 */
	@Min(0)
	private long value;

	/**
	 * E.g. USD.
	 */
	@Column(length = 10, nullable = false)
	private Currency currency;

	@JoinColumn(nullable = false)
	@ManyToOne(fetch = FetchType.EAGER)
	@Index(name = "price_pricetier_index")
	private PricingTier tier;

	@JoinColumn(nullable = true)
	@ManyToOne(fetch = FetchType.EAGER)
	@Index(name = "price_period_idx")
	private SubscriptionPeriod period;

	@JoinColumn(nullable = true)
	@ManyToOne(fetch = FetchType.LAZY)
	@Index(name = "price_catalogue_index")
	private Catalogue catalogue;

	@JoinColumn(nullable = true)
	@ManyToOne(fetch = FetchType.LAZY)
	@Index(name = "price_region_index")
	private Region region;

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

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public long getValue()
	{
		return value;
	}

	/**
	 * A number adjusted to local currency. E.g. value is stored as 100 cents,
	 * but double value returns 1.00
	 * 
	 * @return
	 */
	public double getDoubleValue()
	{
		final BigDecimal bd = new BigDecimal(getValue());
		return bd.movePointLeft(getCurrency().getDefaultFractionDigits()).doubleValue();
	}

	public void setValue(long value)
	{
		this.value = value;
	}

	public Currency getCurrency()
	{
		return currency;
	}

	public void setCurrency(Currency currency)
	{
		this.currency = currency;
	}

	public PricingTier getTier()
	{
		return tier;
	}

	public void setTier(PricingTier tier)
	{
		this.tier = tier;
	}

	public SubscriptionPeriod getPeriod()
	{
		return period;
	}

	public void setPeriod(SubscriptionPeriod period)
	{
		this.period = period;
	}

	public Catalogue getCatalogue()
	{
		return catalogue;
	}

	public void setCatalogue(Catalogue catalogue)
	{
		this.catalogue = catalogue;
	}

	public Region getRegion()
	{
		return region;
	}

	public void setRegion(Region region)
	{
		this.region = region;
	}
}
