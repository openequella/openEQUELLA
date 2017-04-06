package com.tle.core.payment.scripting.types;

import java.io.Serializable;

/**
 * For conveying prices within a single subscription/SubscriptionScriptType
 * 
 * @author larry
 */
public interface PriceScriptType extends Serializable
{
	long getRawValue();

	String getCurrency();

	double getValue();

	SubscriptionPeriodScriptType getSubscriptionPeriod();

	CatalogueScriptType getCatalogue();

	RegionScriptType getRegion();
}
