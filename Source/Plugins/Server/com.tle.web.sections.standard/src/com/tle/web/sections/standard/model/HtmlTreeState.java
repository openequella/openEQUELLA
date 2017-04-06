package com.tle.web.sections.standard.model;

public class HtmlTreeState extends HtmlComponentState
{
	private HtmlTreeModel model;
	private HtmlTreeServer treeServer;
	private String nodeId;
	private boolean lazyLoad;
	private boolean allowMultipleOpenBranches;

	public HtmlTreeModel getModel()
	{
		return model;
	}

	public void setModel(HtmlTreeModel model)
	{
		this.model = model;
	}

	public boolean isLazyLoad()
	{
		return lazyLoad;
	}

	public void setLazyLoad(boolean lazyLoad)
	{
		this.lazyLoad = lazyLoad;
	}

	public boolean isAllowMultipleOpenBranches()
	{
		return allowMultipleOpenBranches;
	}

	public void setAllowMultipleOpenBranches(boolean allowMultipleOpenBranches)
	{
		this.allowMultipleOpenBranches = allowMultipleOpenBranches;
	}

	public String getNodeId()
	{
		return nodeId;
	}

	public void setNodeId(String nodeId)
	{
		this.nodeId = nodeId;
	}

	public HtmlTreeServer getTreeServer()
	{
		return treeServer;
	}

	public void setTreeServer(HtmlTreeServer treeServer)
	{
		this.treeServer = treeServer;
	}
}
