package com.tle.beans.taxonomy;

import javax.xml.bind.annotation.XmlRootElement;

import com.tle.web.api.interfaces.beans.BaseEntityBean;

@XmlRootElement
public class TaxonomyBean extends BaseEntityBean
{
	private boolean readonly;

	public boolean isReadonly()
	{
		return readonly;
	}

	public void setReadonly(boolean readonly)
	{
		this.readonly = readonly;
	}

}
