package com.tle.common.wizard.controls.universal.handlers;

import com.dytech.edge.wizard.beans.control.CustomControl;
import com.tle.common.wizard.controls.universal.UniversalSettings;

@SuppressWarnings("nls")
public class FlickrSettings extends UniversalSettings
{
	private static final String API_KEY = "FlickrApiKey";
	private static final String API_SECRET = "FlickrApiSharedSecret";

	public FlickrSettings(CustomControl wrapped)
	{
		super(wrapped);
	}

	public FlickrSettings(UniversalSettings settings)
	{
		super(settings.getWrapped());
	}

	public String getApiKey()
	{
		return (String) wrapped.getAttributes().get(API_KEY);
	}

	public void setApiKey(String apiKey)
	{
		wrapped.getAttributes().put(API_KEY, apiKey);
	}

	public String getApiSharedSecret()
	{
		return (String) wrapped.getAttributes().get(API_SECRET);
	}

	public void setApiSharedSecret(String apiSharedSecret)
	{
		wrapped.getAttributes().put(API_SECRET, apiSharedSecret);
	}
}
