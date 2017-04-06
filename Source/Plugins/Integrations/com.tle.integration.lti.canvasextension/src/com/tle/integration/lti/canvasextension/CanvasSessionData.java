package com.tle.integration.lti.canvasextension;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.google.common.collect.Sets;
import com.tle.web.integration.IntegrationSessionData;

public class CanvasSessionData implements IntegrationSessionData
{
	private static final long serialVersionUID = 1L;

	private final String action;
	private final String selectionDirective; // e.g. select_link
	private final String extContentIntendedUse; // e.g.navigation,embed
	private final Set<String> extContentReturnTypes = Sets.newHashSet(); // e.g.oembed,lti_launch_url,url,image_url,iframe
	private final String extContentReturnUrl; // e.g.https://equella.instructure.com/external_content/success/external_tool
	private final String launchPresentationLocale; // e.g.en-AU
	private final String launchPresentationReturnUrl; // e.g.https://equella.instructure.com/external_content/success/external_tool
	private String apiDomain;
	private String courseId;
	private String courseInfoCode;
	private String contextTitle;
	private String connectorUuid;

	// private final String resource_link_id
	// 0969a6b970760fd61aee0daf5233f84e6777ce3d
	// private final Stringresource_link_title Aaron EQ

	public CanvasSessionData()
	{
		selectionDirective = null;
		action = null;
		extContentIntendedUse = null;
		extContentReturnUrl = null;
		launchPresentationLocale = null;
		launchPresentationReturnUrl = null;
	}

	public CanvasSessionData(HttpServletRequest request)
	{
		selectionDirective = request.getParameter("selection_directive");
		action = request.getParameter("action");
		extContentIntendedUse = request.getParameter("ext_content_intended_use");
		final String contentReturnTypes = request.getParameter("ext_content_return_types");
		if( contentReturnTypes != null )
		{
			final String[] types = contentReturnTypes.split(",");
			for( String type : types )
			{
				extContentReturnTypes.add(type);
			}
		}

		extContentReturnUrl = request.getParameter("ext_content_return_url");
		launchPresentationLocale = request.getParameter("launch_presentation_locale");
		launchPresentationReturnUrl = request.getParameter("launch_presentation_return_url");
	}

	@Override
	public String getIntegrationType()
	{
		return "canvas";
	}

	public String getSelectionDirective()
	{
		return selectionDirective;
	}

	public String getAction()
	{
		return action;
	}

	public String getExtContentIntendedUse()
	{
		return extContentIntendedUse;
	}

	public Set<String> getExtContentReturnTypes()
	{
		return extContentReturnTypes;
	}

	public String getExtContentReturnUrl()
	{
		return extContentReturnUrl;
	}

	public String getLaunchPresentationLocale()
	{
		return launchPresentationLocale;
	}

	public String getLaunchPresentationReturnUrl()
	{
		return launchPresentationReturnUrl;
	}

	public String getApiDomain()
	{
		return apiDomain;
	}

	public void setApiDomain(String apiDomain)
	{
		this.apiDomain = apiDomain;
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
