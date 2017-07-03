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

package com.tle.core.util.ims.beans;

import java.util.ArrayList;
import java.util.List;

import com.tle.core.xstream.XMLData;
import com.tle.core.xstream.XMLDataMappings;
import com.tle.core.xstream.mapping.DataMapping;
import com.tle.core.xstream.mapping.ListMapping;
import com.tle.core.xstream.mapping.NodeMapping;

/**
 * @author jmaginnis
 */
public class IMSMetadata implements XMLData
{
	private static final long serialVersionUID = 1L;

	private static XMLDataMappings mappings;

	private boolean activityplan;
	private String planTitle;
	private String title;
	private String keywords;
	private String description;
	private List<IMSActivity> activities = new ArrayList<IMSActivity>();
	private IMSCustomData data;

	public boolean isActivityplan()
	{
		return activityplan;
	}

	public String getDescription()
	{
		return description;
	}

	public String getKeywords()
	{
		return keywords;
	}

	public String getPlanTitle()
	{
		return planTitle;
	}

	public String getTitle()
	{
		return title;
	}

	public List<IMSActivity> getActivities()
	{
		return activities;
	}

	public void setActivityplan(boolean activityplan)
	{
		this.activityplan = activityplan;
	}

	public void setPlanTitle(String planTitle)
	{
		this.planTitle = planTitle;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public void setKeywords(String keywords)
	{
		this.keywords = keywords;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public void setActivities(List<IMSActivity> activities)
	{
		this.activities = activities;
	}

	public IMSCustomData getData()
	{
		return data;
	}

	public void setData(IMSCustomData data)
	{
		this.data = data;
	}

	@Override
	@SuppressWarnings("nls")
	public synchronized XMLDataMappings getMappings()
	{
		if( mappings == null )
		{
			mappings = new XMLDataMappings();
			mappings.addNodeMapping(new NodeMapping("activityplan", "lom/general/activityplan"));
			mappings.addNodeMapping(new NodeMapping("planTitle", "lom/general/plantitle/langstring"));
			mappings.addNodeMapping(new NodeMapping("title", "lom/general/title/langstring"));
			mappings.addNodeMapping(new NodeMapping("keywords", "lom/general/keyword/langstring"));
			mappings.addNodeMapping(new NodeMapping("description", "lom/general/description/langstring"));
			// Presumably the intent is to return the implementation class, so
			// we ignore Sonar's "loose coupling" warning
			mappings.addNodeMapping(new ListMapping("activities", "lom/general/activities/activity", ArrayList.class, // NOSONAR
				IMSActivity.class));
			mappings.addNodeMapping(new DataMapping("data", "data", IMSCustomData.class));
		}
		return mappings;
	}
}
