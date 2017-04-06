/*
 * Created on Jun 23, 2005
 */
package com.tle.beans.entity.itemdef;

import java.io.Serializable;
import java.util.List;

public class SearchDetails implements Serializable
{
	private static final long serialVersionUID = 1;

	private List<DisplayNode> displayNodes;
	private String attDisplay;
	private boolean disableThumbnail;
	private boolean standardOpen;
	private boolean integrationOpen;

	public List<DisplayNode> getDisplayNodes()
	{
		return displayNodes;
	}

	public void setDisplayNodes(List<DisplayNode> nodes)
	{
		this.displayNodes = nodes;
	}

	public String getAttDisplay()
	{
		return attDisplay;
	}

	public void setAttDisplay(String attDisplay)
	{
		this.attDisplay = attDisplay;
	}

	public boolean isDisableThumbnail()
	{
		return disableThumbnail;
	}

	public void setDisableThumbnail(boolean disableThumbnail)
	{
		this.disableThumbnail = disableThumbnail;
	}

	public boolean isStandardOpen()
	{
		return standardOpen;
	}

	public void setStandardOpen(boolean standardOpen)
	{
		this.standardOpen = standardOpen;
	}

	public boolean isIntegrationOpen()
	{
		return integrationOpen;
	}

	public void setIntegrationOpen(boolean integrationOpen)
	{
		this.integrationOpen = integrationOpen;
	}
}
