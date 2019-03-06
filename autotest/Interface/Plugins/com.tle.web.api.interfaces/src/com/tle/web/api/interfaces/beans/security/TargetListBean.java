package com.tle.web.api.interfaces.beans.security;

import java.util.List;

import com.tle.web.api.interfaces.beans.RestBean;

public class TargetListBean implements RestBean
{
	private List<TargetListEntryBean> entries;

	public List<TargetListEntryBean> getEntries()
	{
		return entries;
	}

	public TargetListBean setEntries(List<TargetListEntryBean> entries)
	{
		this.entries = entries;
		return this;
	}
}
