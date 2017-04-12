/*
 * Created on 17/06/2006
 */
package com.tle.web.scorm;

public class DefaultScormAPI implements ScormAPI
{
	@Override
	public String initialize(String param)
	{
		return "true"; //$NON-NLS-1$
	}

	@Override
	public String terminate(String param)
	{
		return "true"; //$NON-NLS-1$
	}

	@Override
	public String getValue(String name)
	{
		return ""; //$NON-NLS-1$
	}

	@Override
	public String setValue(String name, String value)
	{
		return "true"; //$NON-NLS-1$
	}

	@Override
	public String commit(String param)
	{
		return "true"; //$NON-NLS-1$
	}

	@Override
	public String getLastError()
	{
		return "0"; //$NON-NLS-1$
	}

	@Override
	public String getErrorString(String errCode)
	{
		return ""; //$NON-NLS-1$
	}

	@Override
	public String getDiagnostic(String error)
	{
		return ""; //$NON-NLS-1$
	}

	@Override
	public void setCurrentIdentifier(String ident)
	{
		// IGNORE
	}

	public String getStartPage()
	{
		return null;
	}
}
