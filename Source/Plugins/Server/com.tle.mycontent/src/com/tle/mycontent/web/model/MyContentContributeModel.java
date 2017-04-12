package com.tle.mycontent.web.model;

import java.util.Set;

import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.equella.layout.TwoColumnLayout;
import com.tle.web.sections.generic.CachedData;

/**
 * @author aholland
 */
public class MyContentContributeModel extends TwoColumnLayout.TwoColumnModel
{
	@Bookmarked
	private String contributeId;
	private final CachedData<Set<String>> allowedHandlers = new CachedData<Set<String>>();

	public String getContributeId()
	{
		return contributeId;
	}

	public void setContributeId(String contributeId)
	{
		this.contributeId = contributeId;
	}

	public CachedData<Set<String>> getAllowedHandlers()
	{
		return allowedHandlers;
	}
}
