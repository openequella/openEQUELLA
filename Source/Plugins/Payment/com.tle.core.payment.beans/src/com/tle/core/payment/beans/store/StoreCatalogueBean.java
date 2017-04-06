package com.tle.core.payment.beans.store;

import javax.xml.bind.annotation.XmlRootElement;

import com.tle.web.api.interfaces.beans.BaseEntityBean;

/**
 * AH: This should be (mostly) empty. Just name, description and UUID are
 * required by the store
 */
@XmlRootElement
public class StoreCatalogueBean extends BaseEntityBean
{
	private int available;

	public int getAvailable()
	{
		return available;
	}

	public void setAvailable(int available)
	{
		this.available = available;
	}
}