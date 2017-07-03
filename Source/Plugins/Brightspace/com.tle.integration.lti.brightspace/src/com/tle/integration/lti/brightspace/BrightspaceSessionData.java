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

package com.tle.integration.lti.brightspace;

import javax.servlet.http.HttpServletRequest;

import com.tle.web.integration.IntegrationSessionData;

public class BrightspaceSessionData implements IntegrationSessionData
{
	private static final long serialVersionUID = 1L;

	private String courseId;
	private String courseInfoCode;
	private String contextTitle;
	private String connectorUuid;
	private final String action;
	private final String launchPresentationLocale; // e.g.en-AU
	private final String launchPresentationReturnUrl; // e.g.https://equella.instructure.com/external_content/success/external_tool

	public BrightspaceSessionData()
	{
		action = null;
		launchPresentationLocale = null;
		launchPresentationReturnUrl = null;
	}

	public BrightspaceSessionData(HttpServletRequest request, String action)
	{
		launchPresentationLocale = request.getParameter("launch_presentation_locale");
		launchPresentationReturnUrl = request.getParameter("launch_presentation_return_url");
		this.action = action;
	}

	@Override
	public String getIntegrationType()
	{
		return "brightspace";
	}

	public String getAction()
	{
		return action;
	}

	public String getLaunchPresentationLocale()
	{
		return launchPresentationLocale;
	}

	public String getLaunchPresentationReturnUrl()
	{
		return launchPresentationReturnUrl;
	}

	public String getCourseId()
	{
		return courseId;
	}

	public void setCourseId(String courseId)
	{
		this.courseId = courseId;
	}

	public String getCourseInfoCode()
	{
		return courseInfoCode;
	}

	public void setCourseInfoCode(String courseInfoCode)
	{
		this.courseInfoCode = courseInfoCode;
	}

	public String getContextTitle()
	{
		return contextTitle;
	}

	public void setContextTitle(String contextTitle)
	{
		this.contextTitle = contextTitle;
	}

	public String getConnectorUuid()
	{
		return connectorUuid;
	}

	public void setConnectorUuid(String connectorUuid)
	{
		this.connectorUuid = connectorUuid;
	}

	@Override
	public boolean isForSelection()
	{
		return true;
	}
}
