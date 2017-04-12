package com.tle.web.activation;

import java.util.Date;

import com.tle.common.search.DefaultSearch;
import com.tle.core.activation.ActivationConstants;
import com.tle.core.guice.Bind;

@Bind
public class ActivationSearch extends DefaultSearch
{
	private static final long serialVersionUID = 1L;

	@Override
	public String getSearchType()
	{
		return ActivationConstants.ACTIVATION_INDEX_ID;
	}

	@Override
	public String getPrivilege()
	{
		return ActivationConstants.VIEW_ACTIVATION_ITEM;
	}

	@Override
	public String getPrivilegeToCollect()
	{
		return ActivationConstants.DELETE_ACTIVATION_ITEM;
	}

	@Override
	public Date[] getDateRange()
	{
		return dateRange;
	}

	@Override
	public void setDateRange(Date[] dateRange)
	{
		this.dateRange = dateRange;
	}
}
