package com.tle.core.connectors.brightspace.beans;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Aaron
 *
 */
@XmlRootElement
public class BrightspaceQuicklink
{
	@JsonProperty("LtiLinkId")
	private String ltiLinkId;
	@JsonProperty("PublicUrl")
	private String publicUrl;

	public String getLtiLinkId()
	{
		return ltiLinkId;
	}

	public void setLtiLinkId(String ltiLinkId)
	{
		this.ltiLinkId = ltiLinkId;
	}

	public String getPublicUrl()
	{
		return publicUrl;
	}

	public void setPublicUrl(String publicUrl)
	{
		this.publicUrl = publicUrl;
	}
}
