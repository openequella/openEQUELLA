package com.tle.beans.usermanagement.standard.wrapper;

import java.util.ArrayList;
import java.util.List;

import com.dytech.edge.common.Constants;
import com.tle.common.settings.annotation.PropertyDataList;

public class AppletSharedSecretSettings
	extends
		AbstractSharedSecretSettings<AppletSharedSecretSettings.AppletSharedSecretValue>
{
	private static final long serialVersionUID = -8720493384727445417L;

	@PropertyDataList(key = "security.applet.shared.secrets", type = AppletSharedSecretValue.class)
	private List<AppletSharedSecretValue> sharedSecrets = new ArrayList<AppletSharedSecretValue>();

	public static class AppletSharedSecretValue extends AbstractSharedSecretSettings.AbstractSharedSecretValue
	{
		private static final long serialVersionUID = -6041835505894872576L;

		public AppletSharedSecretValue()
		{
			setId(Constants.APPLET_SECRET_ID);
		}
	}

	@Override
	public boolean isEnabled()
	{
		return true;
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		// not configurable
	}

	@Override
	public List<AppletSharedSecretValue> getSharedSecrets()
	{
		if( sharedSecrets == null )
		{
			sharedSecrets = new ArrayList<AppletSharedSecretValue>();
		}
		return sharedSecrets;
	}

	@Override
	public void setSharedSecrets(List<AppletSharedSecretValue> sharedSecrets)
	{
		this.sharedSecrets = sharedSecrets;
	}
}
