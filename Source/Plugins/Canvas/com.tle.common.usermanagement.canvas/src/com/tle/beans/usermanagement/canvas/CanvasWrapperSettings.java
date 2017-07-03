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

package com.tle.beans.usermanagement.canvas;

import com.tle.beans.ump.UserManagementSettings;
import com.tle.common.settings.annotation.Property;

/**
 * @author aholland
 */
public class CanvasWrapperSettings extends UserManagementSettings
{
	@Property(key = "wrapper.canvas.url")
	private String canvasUrl;
	@Property(key = "wrapper.canvas.clientid")
	private String clientId;
	@Property(key = "wrapper.canvas.clientsecret")
	private String clientSecret;
	@Property(key = "wrapper.canvas.enabled")
	private boolean enabled;
	@Property(key = "wrapper.canvas.bypasslogonpage")
	private boolean bypassLogonPage;

	public String getCanvasUrl()
	{
		return canvasUrl;
	}

	public void setCanvasUrl(String canvasUrl)
	{
		this.canvasUrl = canvasUrl;
	}

	public String getClientId()
	{
		return clientId;
	}

	public void setClientId(String clientId)
	{
		this.clientId = clientId;
	}

	public String getClientSecret()
	{
		return clientSecret;
	}

	public void setClientSecret(String clientSecret)
	{
		this.clientSecret = clientSecret;
	}

	@Override
	public boolean isEnabled()
	{
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public boolean isBypassLogonPage()
	{
		return bypassLogonPage;
	}

	public void setBypassLogonPage(boolean bypassLogonPage)
	{
		this.bypassLogonPage = bypassLogonPage;
	}
}
