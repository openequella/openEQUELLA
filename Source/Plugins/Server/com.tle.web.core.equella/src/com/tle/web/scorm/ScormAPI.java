/*
 * Created on 17/06/2006
 */

package com.tle.web.scorm;

public interface ScormAPI
{
	String initialize(String param);

	String terminate(String param);

	String getValue(String name);

	String setValue(String name, String value);

	String commit(String param);

	String getLastError();

	String getErrorString(String errCode);

	String getDiagnostic(String error);

	void setCurrentIdentifier(String ident);
}