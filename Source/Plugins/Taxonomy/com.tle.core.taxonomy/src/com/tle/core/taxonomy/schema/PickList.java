package com.tle.core.taxonomy.schema;

import com.dytech.edge.wizard.beans.control.Multi;

public class PickList extends Multi
{
	private static final long serialVersionUID = 1;
	public static final String CLASS = "picklist"; //$NON-NLS-1$

	private String taxonomy;
	private boolean multiple;

	@Override
	public String getClassType()
	{
		return CLASS;
	}

	public String getTaxonomy()
	{
		return taxonomy;
	}

	public void setTaxonomy(String taxonomy)
	{
		this.taxonomy = taxonomy;
	}

	public boolean isMultiple()
	{
		return multiple;
	}

	public void setMultiple(boolean multiple)
	{
		this.multiple = multiple;
	}

}
