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
