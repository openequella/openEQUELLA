package com.tle.core.payment.scripting.types;

import java.util.List;

/**
 * @author larry
 */
public interface SubscriptionTierScriptType extends BaseTierScriptType
{
	List<PriceScriptType> getPrices();
}
