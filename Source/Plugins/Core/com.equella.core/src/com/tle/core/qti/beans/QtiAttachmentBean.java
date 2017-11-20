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

package com.tle.core.qti.beans;

import com.tle.web.api.item.equella.interfaces.beans.EquellaAttachmentBean;

@SuppressWarnings("nls")
public class QtiAttachmentBean extends EquellaAttachmentBean
{
	private String xmlFullPath;
	private String testUuid;
	private String toolName;
	private String toolVersion;
	private long maxTime;
	private int questionCount;
	private int sectionCount;
	private String navigationMode;
	private String manifestPath;

	@Override
	public String getRawAttachmentType()
	{
		return "custom/qtitest";
	}

	public String getXmlFullPath()
	{
		return xmlFullPath;
	}

	public void setXmlFullPath(String xmlFullPath)
	{
		this.xmlFullPath = xmlFullPath;
	}

	public String getTestUuid()
	{
		return testUuid;
	}

	public void setTestUuid(String testUuid)
	{
		this.testUuid = testUuid;
	}

	public String getToolName()
	{
		return toolName;
	}

	public void setToolName(String toolName)
	{
		this.toolName = toolName;
	}

	public String getToolVersion()
	{
		return toolVersion;
	}

	public void setToolVersion(String toolVersion)
	{
		this.toolVersion = toolVersion;
	}

	public long getMaxTime()
	{
		return maxTime;
	}

	public void setMaxTime(long maxTime)
	{
		this.maxTime = maxTime;
	}

	public int getQuestionCount()
	{
		return questionCount;
	}

	public void setQuestionCount(int questionCount)
	{
		this.questionCount = questionCount;
	}

	public int getSectionCount()
	{
		return sectionCount;
	}

	public void setSectionCount(int sectionCount)
	{
		this.sectionCount = sectionCount;
	}

	public String getNavigationMode()
	{
		return navigationMode;
	}

	public void setNavigationMode(String navigationMode)
	{
		this.navigationMode = navigationMode;
	}

	public String getManifestPath()
	{
		return manifestPath;
	}

	public void setManifestPath(String manifestPath)
	{
		this.manifestPath = manifestPath;
	}

}