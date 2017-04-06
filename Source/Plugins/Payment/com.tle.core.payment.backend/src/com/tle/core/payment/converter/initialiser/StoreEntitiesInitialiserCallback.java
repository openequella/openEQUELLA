package com.tle.core.payment.converter.initialiser;

import com.tle.common.payment.entity.Sale;
import com.tle.common.payment.entity.SubscriptionPeriod;
import com.tle.core.initialiser.Property;
import com.tle.core.institution.convert.EntityInitialiserCallback;

/**
 * @author Aaron
 */
public class StoreEntitiesInitialiserCallback extends EntityInitialiserCallback
{
	@Override
	public void set(Object obj, Property property, Object value)
	{
		if( value instanceof SubscriptionPeriod )
		{
			SubscriptionPeriod toset = (SubscriptionPeriod) value;
			toset.setUuid(((SubscriptionPeriod) property.get(obj)).getUuid());
		}
		else if( value instanceof Sale )
		{
			Sale toset = (Sale) value;
			toset.setUuid(((Sale) property.get(obj)).getUuid());
		}
		super.set(obj, property, value);
	}

	@Override
	public void entitySimplified(Object old, Object newObj)
	{
		if( old instanceof SubscriptionPeriod )
		{
			SubscriptionPeriod toset = (SubscriptionPeriod) newObj;
			SubscriptionPeriod oldObj = (SubscriptionPeriod) old;
			toset.setUuid(oldObj.getUuid());
		}
		else if( old instanceof Sale )
		{
			Sale toset = (Sale) newObj;
			Sale oldObj = (Sale) old;
			toset.setUuid(oldObj.getUuid());
		}
		super.entitySimplified(old, newObj);
	}
}
