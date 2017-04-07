package com.tle.web.api.item.interfaces.beans;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.tle.web.api.interfaces.beans.AbstractExtendableBean;


@XmlRootElement
public class NavigationTreeBean extends AbstractExtendableBean
{
	private boolean hideUnreferencedAttachments;
	private boolean showSplitOption;
	private List<NavigationNodeBean> nodes;

	@Deprecated
	public void setShowUnreferencedAttachments(boolean showUnreferencedAttachments)
	{
		this.hideUnreferencedAttachments = showUnreferencedAttachments;
	}

	public boolean isShowSplitOption()
	{
		return showSplitOption;
	}

	public void setShowSplitOption(boolean showSplitOption)
	{
		this.showSplitOption = showSplitOption;
	}

	public List<NavigationNodeBean> getNodes()
	{
		return nodes;
	}

	public void setNodes(List<NavigationNodeBean> nodes)
	{
		this.nodes = nodes;
	}

	public boolean isHideUnreferencedAttachments()
	{
		return hideUnreferencedAttachments;
	}

	public void setHideUnreferencedAttachments(boolean hideUnreferencedAttachments)
	{
		this.hideUnreferencedAttachments = hideUnreferencedAttachments;
	}
}
