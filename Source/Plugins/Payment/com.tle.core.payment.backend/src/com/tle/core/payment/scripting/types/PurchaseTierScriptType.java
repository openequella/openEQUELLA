package com.tle.core.payment.scripting.types;

/**
 * a read-only bean to be available to advanced scripting
 * 
 * @author larry
 */
public interface PurchaseTierScriptType extends BaseTierScriptType
{
	PriceScriptType getPrice();
}
