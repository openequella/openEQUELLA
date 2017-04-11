package com.tle.web.api.collection.interfaces.beans;

import java.util.List;
import java.util.Map;

import com.tle.web.api.interfaces.beans.security.BaseEntitySecurityBean;
import com.tle.web.api.interfaces.beans.security.DynamicRuleBean;
import com.tle.web.api.interfaces.beans.security.TargetListEntryBean;

public class CollectionSecurityBean extends BaseEntitySecurityBean
{
	private Map<String, ItemMetadataSecurityBean> metadata;
	private Map<String, List<TargetListEntryBean>> statuses;
	private List<DynamicRuleBean> dynamicRules;

	public Map<String, ItemMetadataSecurityBean> getMetadata()
	{
		return metadata;
	}

	public void setMetadata(Map<String, ItemMetadataSecurityBean> metadata)
	{
		this.metadata = metadata;
	}

	public Map<String, List<TargetListEntryBean>> getStatuses()
	{
		return statuses;
	}

	public void setStatuses(Map<String, List<TargetListEntryBean>> statuses)
	{
		this.statuses = statuses;
	}

	public List<DynamicRuleBean> getDynamicRules()
	{
		return dynamicRules;
	}

	public void setDynamicRules(List<DynamicRuleBean> dynamicRules)
	{
		this.dynamicRules = dynamicRules;
	}
}
