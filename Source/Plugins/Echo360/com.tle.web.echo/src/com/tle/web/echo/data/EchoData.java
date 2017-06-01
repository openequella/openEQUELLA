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

package com.tle.web.echo.data;

import java.util.Date;


public class EchoData
{
	private String echoSystemID;

	private String echoId;
	private String echoStatus;
	private String echoTitle;
	private String echoDesc;
	private Date echoPublishedDate;
	private Date echoCapturedDate;
	private long echoDuration;

	private String echoLinkUrl;
	private String echoCenterUrl;
	private String podcastUrl;
	private String vodcastUrl;

	private String sectionId;
	private String sectionName;

	private String courseId;
	private String courseName;
	private String courseIdentifier;

	public String getEchoSystemID()
	{
		return echoSystemID;
	}

	public void setEchoSystemID(String echoSystemID)
	{
		this.echoSystemID = echoSystemID;
	}

	public String getEchoId()
	{
		return echoId;
	}

	public void setEchoId(String echoId)
	{
		this.echoId = echoId;
	}

	public String getEchoStatus()
	{
		return echoStatus;
	}

	public void setEchoStatus(String echoStatus)
	{
		this.echoStatus = echoStatus;
	}

	public String getEchoTitle()
	{
		return echoTitle;
	}

	public void setEchoTitle(String echoTitle)
	{
		this.echoTitle = echoTitle;
	}

	public String getEchoDesc()
	{
		return echoDesc;
	}

	public void setEchoDesc(String echoDesc)
	{
		this.echoDesc = echoDesc;
	}

	public Date getEchoPublishedDate()
	{
		return echoPublishedDate;
	}

	public void setEchoPublishedDate(Date echoPublishedDate)
	{
		this.echoPublishedDate = echoPublishedDate;
	}

	public Date getEchoCapturedDate()
	{
		return echoCapturedDate;
	}

	public void setEchoCapturedDate(Date echoCapturedDate)
	{
		this.echoCapturedDate = echoCapturedDate;
	}

	public long getEchoDuration()
	{
		return echoDuration;
	}

	public void setEchoDuration(long echoDuration)
	{
		this.echoDuration = echoDuration;
	}

	public String getEchoLinkUrl()
	{
		return echoLinkUrl;
	}

	public void setEchoLinkUrl(String echoLinkUrl)
	{
		this.echoLinkUrl = echoLinkUrl;
	}

	public String getEchoCenterUrl()
	{
		return echoCenterUrl;
	}

	public void setEchoCenterUrl(String echoCenterUrl)
	{
		this.echoCenterUrl = echoCenterUrl;
	}

	public String getPodcastUrl()
	{
		return podcastUrl;
	}

	public void setPodcastUrl(String podcastUrl)
	{
		this.podcastUrl = podcastUrl;
	}

	public String getVodcastUrl()
	{
		return vodcastUrl;
	}

	public void setVodcastUrl(String vodcastUrl)
	{
		this.vodcastUrl = vodcastUrl;
	}

	public String getSectionId()
	{
		return sectionId;
	}

	public void setSectionId(String sectionId)
	{
		this.sectionId = sectionId;
	}

	public String getSectionName()
	{
		return sectionName;
	}

	public void setSectionName(String sectionName)
	{
		this.sectionName = sectionName;
	}

	public String getCourseId()
	{
		return courseId;
	}

	public void setCourseId(String courseId)
	{
		this.courseId = courseId;
	}

	public String getCourseName()
	{
		return courseName;
	}

	public void setCourseName(String courseName)
	{
		this.courseName = courseName;
	}

	public String getCourseIdentifier()
	{
		return courseIdentifier;
	}

	public void setCourseIdentifier(String courseIdentifier)
	{
		this.courseIdentifier = courseIdentifier;
	}
	
}