package com.tle.web.api.oauth.interfaces.beans;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.tle.web.api.interfaces.beans.BaseEntityExportBean;

@XmlRootElement
public class OAuthExportBean extends BaseEntityExportBean
{
	private List<OAuthTokenBean> tokens;

	public List<OAuthTokenBean> getTokens()
	{
		return tokens;
	}

	public void setTokens(List<OAuthTokenBean> tokens)
	{
		this.tokens = tokens;
	}
}
