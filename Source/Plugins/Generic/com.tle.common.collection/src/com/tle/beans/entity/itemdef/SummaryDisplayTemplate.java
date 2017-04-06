package com.tle.beans.entity.itemdef;

import java.io.Serializable;
import java.util.List;

public class SummaryDisplayTemplate implements Serializable
{
	private static final long serialVersionUID = 1;

	private List<SummarySectionsConfig> configList;
	private boolean hideOwner = false;
	private boolean hideCollaborators = false;

	public void setConfigList(List<SummarySectionsConfig> nodes)
	{
		this.configList = nodes;
	}

	public List<SummarySectionsConfig> getConfigList()
	{
		return configList;
	}

	public boolean isHideOwner()
	{
		return hideOwner;
	}

	public void setHideOwner(boolean hideOwner)
	{
		this.hideOwner = hideOwner;
	}

	public boolean isHideCollaborators()
	{
		return hideCollaborators;
	}

	public void setHideCollaborators(boolean hideCollaborators)
	{
		this.hideCollaborators = hideCollaborators;
	}
}
