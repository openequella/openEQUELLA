package com.tle.core.payment.storefront.settings;

import com.tle.common.property.ConfigurationProperties;
import com.tle.common.property.annotation.Property;

public class StoreFrontSettings implements ConfigurationProperties
{
	private static final long serialVersionUID = 1;
	/**
	 * Occasional need to refer to the property key in its String form
	 */
	public static final String PROPERTY_KEY = "storefront.collection"; //$NON-NLS-1$
	public static final String TAX_KEY = "storefront.includetax"; //$NON-NLS-1$

	@Property(key = PROPERTY_KEY)
	private String collection;
	@Property(key = TAX_KEY)
	private boolean excludeTax;

	public boolean isIncludeTax()
	{
		// So it defaults true
		return !excludeTax;
	}

	public void setIncludeTax(boolean includeTax)
	{
		this.excludeTax = !includeTax;
	}

	public String getCollection()
	{
		return collection;
	}

	public void setCollection(String collection)
	{
		this.collection = collection;
	}
}
