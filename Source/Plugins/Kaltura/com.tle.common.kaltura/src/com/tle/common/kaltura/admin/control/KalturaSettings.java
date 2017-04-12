package com.tle.common.kaltura.admin.control;

import com.dytech.edge.wizard.beans.control.CustomControl;
import com.tle.common.wizard.controls.universal.UniversalSettings;

@SuppressWarnings("nls")
public class KalturaSettings extends UniversalSettings
{
	public enum KalturaOption
	{
		UPLOAD, EXISTING;
	}

	public static final String KEY_SERVER_UUID = "kalturaServerUUID";
	public static final String KEY_SERVER_RESTRICTION = "kalturaServerRestriction";

	public KalturaSettings(CustomControl wrapped)
	{
		super(wrapped);
	}

	public String getServerUuid()
	{
		return (String) wrapped.getAttributes().get(KEY_SERVER_UUID);
	}

	public void setServerUuid(String serverUuid)
	{
		wrapped.getAttributes().put(KEY_SERVER_UUID, serverUuid);
	}

	public String getRestriction()
	{
		return (String) wrapped.getAttributes().get(KEY_SERVER_RESTRICTION);
	}

	public void setRestriction(String restriction)
	{
		if( restriction == null )
		{
			wrapped.getAttributes().remove(KEY_SERVER_RESTRICTION);
		}
		else
		{
			wrapped.getAttributes().put(KEY_SERVER_RESTRICTION, restriction);
		}
	}

}
