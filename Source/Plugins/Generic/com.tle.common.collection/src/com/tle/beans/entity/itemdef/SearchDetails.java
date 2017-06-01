/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
