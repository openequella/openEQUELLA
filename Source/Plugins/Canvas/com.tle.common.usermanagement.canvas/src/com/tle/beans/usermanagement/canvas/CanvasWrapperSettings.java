package com.tle.beans.usermanagement.canvas;

import com.tle.beans.ump.UserManagementSettings;
import com.tle.common.property.annotation.Property;

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
