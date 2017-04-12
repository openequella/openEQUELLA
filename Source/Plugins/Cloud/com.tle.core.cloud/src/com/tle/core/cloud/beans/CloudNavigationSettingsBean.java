package com.tle.core.cloud.beans;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CloudNavigationSettingsBean
{
	private boolean hideUnreferencedAttachments;
	private boolean showSplitOption;
	private List<CloudNavigationNodeBean> nodes;

	public boolean isHideUnreferencedAttachments()
	{
		return hideUnreferencedAttachments;
	}

	public void setHideUnreferencedAttachments(boolean hideUnreferencedAttachments)
	{
		this.hideUnreferencedAttachments = hideUnreferencedAttachments;
	}

	public boolean isShowSplitOption()
	{
		return showSplitOption;
	}

	public void setShowSplitOption(boolean showSplitOption)
	{
		this.showSplitOption = showSplitOption;
	}

	public List<CloudNavigationNodeBean> getNodes()
	{
		return nodes;
	}

	public void setNodes(List<CloudNavigationNodeBean> nodes)
	{
		this.nodes = nodes;
	}
}
