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

package com.tle.integration.blackboard.gateways;

import java.awt.Color;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.valuebean.ItemKey;

public class BlackboardContent extends Content
{
	private static final Log LOGGER = LogFactory.getLog(BlackboardContent.class);
	private static final int COLOUR_BASE = 16;

	public static final String LESSON_TYPE = "lesson";
	public static final String FILE_LESSON_TYPE = "file_only_lesson";
	public static final String CONTENT_TYPE = "content";
	public static final String DEFAULT_TYPE = "default";
	public static final String FOLDER_TYPE = "folder";
	public static final String RESOURCE_TYPE = "resource/tle-resource";
	public static final String PLAN_TYPE = "resource/tle-plan";
	public static final String PLAN_LIVE_TYPE = "resource/tle-plan-live";
	public static final String LINK_TYPE = "link";
	public static final String WEB_TYPE = "web";
	public static final String TEST_TYPE = "test";
	public static final String SURVEY_TYPE = "survey";
	public static final String ASSIGNMENT_TYPE = "assignment";
	public static final String UNKNOWN_TYPE = "unknown";

	protected String type = DEFAULT_TYPE;

	protected Date afterDate;
	protected Date untilDate;
	protected Color color;
	protected boolean visible;
	protected boolean track;
	protected boolean metadata;
	protected boolean isFolder;
	protected boolean displayAfter;
	protected boolean displayUntil;
	protected boolean sequential;
	protected boolean launch;
	protected boolean offline;
	protected String offlineName;
	protected String offlinePath;
	private ItemKey planId;

	// protected PlanItem plan;

	public BlackboardContent()
	{
		super();

		afterDate = Calendar.getInstance().getTime();
		untilDate = afterDate;

		displayAfter = false;
		displayUntil = false;
		visible = true;
		track = false;
		metadata = false;
		sequential = true;
		isFolder = false;
		launch = false;

		offlinePath = "";
		offlineName = "";

		offline = false;

		color = Color.BLACK;
	}

	public BlackboardContent(PropBagEx xml)
	{
		super(xml);
		try
		{
			DateFormat dateFormatter = getDateFormatter();
			afterDate = dateFormatter.parse(xml.getNode("after"));
			untilDate = dateFormatter.parse(xml.getNode("until"));
		}
		catch( ParseException pe )
		{
			afterDate = Calendar.getInstance().getTime();
			untilDate = afterDate;
		}

		displayAfter = xml.isNodeTrue("after/@selected");
		displayUntil = xml.isNodeTrue("until/@selected");
		visible = xml.isNodeTrue("visible");
		track = xml.isNodeTrue("track");
		metadata = xml.isNodeTrue("metadata");
		sequential = xml.isNodeTrue("sequential");
		isFolder = xml.isNodeTrue("folder");
		launch = xml.isNodeTrue("launch");

		offlinePath = xml.getNode("offlinePath");
		offlineName = xml.getNode("offlineName");

		offline = offlinePath.length() > 0;

		String rgb = xml.getNode("colour");
		if( rgb.length() == 6 )
		{
			try
			{
				color = new Color(Integer.parseInt(rgb, COLOUR_BASE));
			}
			catch( NumberFormatException nfe )
			{
				LOGGER.warn("Could not convert RGB to int");
				color = Color.BLACK;
			}
		}
		else
		{
			color = Color.BLACK;
		}

		type = xml.getNode("type");

		setPlanFromHtml(xml);
	}

	private void setPlanFromHtml(PropBagEx xml)
	{
		if( type.equals(PLAN_TYPE) && html.startsWith("<!--") )
		{
			int temp = html.indexOf("-->");
			String newbody = html.substring(4, temp);

			PropBagEx itemxml = new PropBagEx(newbody);
			if( itemxml.nodeExists("result") )
			{
				itemxml = xml.getSubtree("result");
			}

			// see Jira Defect TLE-1032 :
			// http://apps.dytech.com.au/jira/browse/TLE-1032
			if( itemxml.nodeExists("item") && !itemxml.getNodeName().equals("item") )
			{
				itemxml = xml.getSubtree("item");
			}

			String id = itemxml.getNode("@id");
			int version = itemxml.getIntNode("@version", 1);
			String itemdefid = itemxml.getNode("@itemdefid");
			planId = new ItemKey(id, version, itemdefid);
		}
	}

	@Override
	public PropBagEx getXml()
	{
		DateFormat dateFormatter = getDateFormatter();

		PropBagEx xml = super.getXml();
		xml.setNode("after", dateFormatter.format(afterDate));
		xml.setNode("after/@selected", displayAfter);
		xml.setNode("until", dateFormatter.format(untilDate));
		xml.setNode("until/@selected", displayUntil);
		xml.setNode("visible", visible);
		xml.setNode("track", track);
		xml.setNode("metadata", metadata);
		xml.setNode("sequential", sequential);
		xml.setNode("folder", isFolder || type.equals(FOLDER_TYPE));
		xml.setNode("launch", launch);

		xml.setNode("offlinePath", offlinePath);
		xml.setNode("offlineName", offlineName);

		xml.setNode("type", type);
		String rgb = Integer.toHexString(color.getRGB());
		xml.setNode("colour", rgb.substring(2).toUpperCase());

		return xml;
	}

	public boolean isDisplayUntil()
	{
		return displayUntil;
	}

	public void setDisplayUntil(boolean displayAfter)
	{
		this.displayUntil = displayAfter;
	}

	public boolean isDisplayAfter()
	{
		return displayAfter;
	}

	public void setDisplayAfter(boolean displayBefore)
	{
		this.displayAfter = displayBefore;
	}

	public boolean isMetadata()
	{
		return metadata;
	}

	public void setMetadata(boolean metadata)
	{
		this.metadata = metadata;
	}

	public boolean isTrack()
	{
		return track;
	}

	public void setTrack(boolean track)
	{
		this.track = track;
	}

	public boolean isVisible()
	{
		return visible;
	}

	public void setVisible(boolean visible)
	{
		this.visible = visible;
	}

	public Date getAfterDate()
	{
		return afterDate;
	}

	public void setAfterDate(Date beforeDate)
	{
		this.afterDate = beforeDate;
	}

	public Date getUntilDate()
	{
		return untilDate;
	}

	public void setUntilDate(Date untilDate)
	{
		this.untilDate = untilDate;
	}

	public Color getColor()
	{
		return color;
	}

	public void setColor(Color color)
	{
		this.color = color;
	}

	public boolean isFolder()
	{
		return isFolder;
	}

	public void setFolder(boolean isFolder)
	{
		this.isFolder = isFolder;
	}

	public boolean isLaunch()
	{
		return launch;
	}

	public void setLaunch(boolean launch)
	{
		this.launch = launch;
	}

	public boolean isSequential()
	{
		return sequential;
	}

	public void setSequential(boolean sequential)
	{
		this.sequential = sequential;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public boolean isOffline()
	{
		return offline;
	}

	public void setOffline(boolean offline)
	{
		this.offline = offline;
	}

	public String getOfflineName()
	{
		return offlineName;
	}

	public void setOfflineName(String offlineName)
	{
		this.offlineName = offlineName;
	}

	public String getOfflinePath()
	{
		return offlinePath;
	}

	public void setOfflinePath(String offlinePath)
	{
		this.offlinePath = offlinePath;
	}

	public ItemKey getPlanId()
	{
		return planId;
	}

	public static DateFormat getDateFormatter()
	{
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}
}
