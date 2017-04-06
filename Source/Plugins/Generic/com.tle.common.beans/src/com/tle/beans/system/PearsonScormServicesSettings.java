package com.tle.beans.system;

import com.tle.common.property.ConfigurationProperties;
import com.tle.common.property.annotation.Property;

public class PearsonScormServicesSettings implements ConfigurationProperties
{
	private static final long serialVersionUID = -6453184772230531991L;

	@Property(key = "scorm.pss.enable")
	private boolean enable;

	@Property(key = "scorm.pss.namespace")
	private String accountNamespace;

	@Property(key = "scorm.pss.consumerkey")
	private String consumerKey;

	@Property(key = "scorm.pss.consumersecret")
	private String consumerSecret;

	@Property(key = "scorm.pss.baseurl")
	private String baseUrl;

	public boolean isEnable()
	{
		return enable;
	}

	public void setEnable(boolean enable)
	{
		this.enable = enable;
	}

	public String getAccountNamespace()
	{
		return accountNamespace;
	}

	public void setAccountNamespace(String accountNamespace)
	{
		this.accountNamespace = accountNamespace;
	}

	public String getConsumerKey()
	{
		return consumerKey;
	}

	public void setConsumerKey(String consumerKey)
	{
		this.consumerKey = consumerKey;
	}

	public String getConsumerSecret()
	{
		return consumerSecret;
	}

	public void setConsumerSecret(String consumerSecret)
	{
		this.consumerSecret = consumerSecret;
	}

	public String getBaseUrl()
	{
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl)
	{
		this.baseUrl = baseUrl;
	}
}
