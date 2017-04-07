package com.tle.json.framework;

import java.net.URI;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class PageContext
{
	private final String baseUrl;
	private String integUrl;
	private String namePrefix;
	private String subPrefix;
	private String prefix = "";
	private final Map<Object, Object> attr = Maps.newHashMap();
	private final TestConfig testConfig;

	public PageContext(TestConfig testConfig, String baseUrl)
	{
		Preconditions.checkNotNull(baseUrl);
		this.testConfig = testConfig;
		this.baseUrl = baseUrl;
	}

	public PageContext(PageContext existing, String baseUrl)
	{
		this.testConfig = existing.testConfig;
		attr.putAll(existing.attr);
		this.integUrl = existing.integUrl;
		this.namePrefix = existing.namePrefix;
		this.subPrefix = existing.subPrefix;
		this.prefix = existing.prefix;
		this.baseUrl = baseUrl;
	}

	public String getNamePrefix()
	{
		return namePrefix;
	}

	public void setNamePrefix(String namePrefix)
	{
		this.namePrefix = namePrefix;
		changePrefix();
	}

	private void changePrefix()
	{
		if( namePrefix != null )
		{
			prefix = namePrefix + " - ";
			if( subPrefix != null )
				prefix += subPrefix + " ";
		}
		else
		{
			prefix = "";
		}
	}

	public String getFullName(String name)
	{
		return prefix + name;
	}

	public String getNamePrefix(String name)
	{
		return getNamePrefix() + " - " + name;
	}

	public String getSubPrefix()
	{
		return subPrefix;
	}

	public void setSubPrefix(String subPrefix)
	{
		this.subPrefix = subPrefix;
		changePrefix();
	}

	public URI getBaseURI()
	{
		return URI.create(baseUrl);
	}

	public String getBaseUrl()
	{
		return baseUrl;
	}

	public String getIntegUrl()
	{
		return integUrl;
	}

	public void setIntegUrl(String integUrl)
	{
		this.integUrl = integUrl;
	}

	@SuppressWarnings("unchecked")
	public <T> T getAttribute(Object key)
	{
		return (T) attr.get(key);
	}

	public void setAttribute(Object key, Object value)
	{
		attr.put(key, value);
	}

	public TestConfig getTestConfig()
	{
		return testConfig;
	}

}
