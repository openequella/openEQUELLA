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

package com.tle.core.connectors.brightspace.beans;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Aaron
 *
 */
@XmlRootElement
public class BrightspaceEquellaLink
{
	@JsonProperty("OrgUnitName")
	private String orgUnitName;
	@JsonProperty("OrgUnitId")
	private int orgUnitId;
	@JsonProperty("OrgUnitCode")
	private String orgUnitCode;
	@JsonProperty("ParentModuleName")
	private String parentModuleName;
	@JsonProperty("ParentModuelId")
	private int parentModuleId;
	@JsonProperty("TopicName")
	private String topicName;
	@JsonProperty("TopicId")
	private int topicId;
	@JsonProperty("LastModifiedDate")
	private String lastModifiedDate;
	@JsonProperty("Visible")
	private Boolean visible;
	@JsonProperty("D2LTopicLink")
	private String d2lTopicLink;
	@JsonProperty("LTILinkId")
	private int ltiLinkId;
	@JsonProperty("LTILink")
	private String ltiLink;
	@JsonProperty("TotalEquellaLinkUsageCount")
	private int totalEquellaLinkUsageCount;

	public String getOrgUnitName()
	{
		return orgUnitName;
	}

	public void setOrgUnitName(String orgUnitName)
	{
		this.orgUnitName = orgUnitName;
	}

	public int getOrgUnitId()
	{
		return orgUnitId;
	}

	public void setOrgUnitId(int orgUnitId)
	{
		this.orgUnitId = orgUnitId;
	}

	public String getOrgUnitCode()
	{
		return orgUnitCode;
	}

	public void setOrgUnitCode(String orgUnitCode)
	{
		this.orgUnitCode = orgUnitCode;
	}

	public String getParentModuleName()
	{
		return parentModuleName;
	}

	public void setParentModuleName(String parentModuleName)
	{
		this.parentModuleName = parentModuleName;
	}

	public int getParentModuleId()
	{
		return parentModuleId;
	}

	public void setParentModuleId(int parentModuleId)
	{
		this.parentModuleId = parentModuleId;
	}

	public String getTopicName()
	{
		return topicName;
	}

	public void setTopicName(String topicName)
	{
		this.topicName = topicName;
	}

	public int getTopicId()
	{
		return topicId;
	}

	public void setTopicId(int topicId)
	{
		this.topicId = topicId;
	}

	public String getLastModifiedDate()
	{
		return lastModifiedDate;
	}

	public void setLastModifiedDate(String lastModifiedDate)
	{
		this.lastModifiedDate = lastModifiedDate;
	}

	public Boolean getVisible()
	{
		return visible;
	}

	public void setVisible(Boolean visible)
	{
		this.visible = visible;
	}

	public String getD2lTopicLink()
	{
		return d2lTopicLink;
	}

	public void setD2lTopicLink(String d2lTopicLink)
	{
		this.d2lTopicLink = d2lTopicLink;
	}

	public int getLtiLinkId()
	{
		return ltiLinkId;
	}

	public void setLtiLinkId(int ltiLinkId)
	{
		this.ltiLinkId = ltiLinkId;
	}

	public String getLtiLink()
	{
		return ltiLink;
	}

	public void setLtiLink(String ltiLink)
	{
		this.ltiLink = ltiLink;
	}

	public int getTotalEquellaLinkUsageCount()
	{
		return totalEquellaLinkUsageCount;
	}

	public void setTotalEquellaLinkUsageCount(int totalEquellaLinkUsageCount)
	{
		this.totalEquellaLinkUsageCount = totalEquellaLinkUsageCount;
	}
}
