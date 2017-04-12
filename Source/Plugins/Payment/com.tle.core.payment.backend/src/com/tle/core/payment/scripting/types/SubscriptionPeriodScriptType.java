package com.tle.core.payment.scripting.types;

import java.io.Serializable;

/**
 * @author Aaron
 */
public interface SubscriptionPeriodScriptType extends Serializable
{
	String getUuid();

	String getName();

	int getDuration();

	String getDurationUnit();
}
