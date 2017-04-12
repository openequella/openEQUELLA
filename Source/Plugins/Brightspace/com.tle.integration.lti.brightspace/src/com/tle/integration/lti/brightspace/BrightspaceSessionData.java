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
}
