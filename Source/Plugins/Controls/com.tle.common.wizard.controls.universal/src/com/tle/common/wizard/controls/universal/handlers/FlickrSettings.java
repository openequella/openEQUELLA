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
