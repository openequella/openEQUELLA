/*
 * Created on 17/06/2006
 */
package com.tle.web.ajax.services.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.core.services.user.UserSessionService;
import com.tle.web.ajax.services.ScormAPIHandler;
import com.tle.web.scorm.DefaultScormAPI;
import com.tle.web.scorm.ScormAPI;

/**
 * This class is the DWR interface for SCORM API calls
 */
@Bind(ScormAPIHandler.class)
@Singleton
public class ScormAPIHandlerImpl implements ScormAPIHandler
{
	public static final String KEY_SCORMAPI = "$SCORM$API"; //$NON-NLS-1$

	@Inject
	private UserSessionService sessionService;

	private ScormAPI defaultScormAPI = new DefaultScormAPI();

	private ScormAPI getScormApi()
	{
		ScormAPI api = sessionService.getAttribute(KEY_SCORMAPI);
		if( api == null )
		{
			api = defaultScormAPI;
		}
		return api;
	}

	@Override
	public void setCurrentIdentifier(String ident)
	{
		getScormApi().setCurrentIdentifier(ident);
	}

	@Override
	public String initialize(String param)
	{
		return getScormApi().initialize(param);
	}

	@Override
	public String terminate(String param)
	{
		return getScormApi().terminate(param);
	}

	@Override
	public String getValue(String name)
	{
		return getScormApi().getValue(name);
	}

	@Override
	public String setValue(String name, String value)
	{
		return getScormApi().setValue(name, value);
	}

	@Override
	public String commit(String param)
	{
		return getScormApi().commit(param);
	}

	@Override
	public String getLastError()
	{
		return getScormApi().getLastError();
	}

	@Override
	public String getErrorString(String errCode)
	{
		return getScormApi().getErrorString(errCode);
	}

	@Override
	public String getDiagnostic(String error)
	{
		return getScormApi().getDiagnostic(error);
	}

	// To match all versions of API

	@Override
	public String lmsInitialize(String param)
	{
		return initialize(param);
	}

	@Override
	public String lmsTerminate(String param)
	{
		return terminate(param);
	}

	@Override
	public String lmsFinish(String param)
	{
		return finish(param);
	}

	@Override
	public String lmsCommit(String param)
	{
		return commit(param);
	}

	@Override
	public String lmsGetValue(String name)
	{
		return getValue(name);
	}

	@Override
	public String lmsSetValue(String name, String value)
	{
		return setValue(name, value);
	}

	@Override
	public String finish(String param)
	{
		return terminate(param);
	}

	@Override
	public String lmsGetLastError()
	{
		return getLastError();
	}

	@Override
	public String lmsGetErrorString(String errCode)
	{
		return getErrorString(errCode);
	}

	@Override
	public String lmsGetDiagnostic(String error)
	{
		return getDiagnostic(error);
	}
}
