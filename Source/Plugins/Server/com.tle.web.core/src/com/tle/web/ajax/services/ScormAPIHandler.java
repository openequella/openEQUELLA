package com.tle.web.ajax.services;

public interface ScormAPIHandler
{
	void setCurrentIdentifier(String ident);

	String initialize(String param);

	String terminate(String param);

	String getValue(String name);

	String setValue(String name, String value);

	String commit(String param);

	String getLastError();

	String getErrorString(String errCode);

	String getDiagnostic(String error);

	String lmsInitialize(String param);

	String lmsTerminate(String param);

	String lmsFinish(String param);

	String lmsCommit(String param);

	String lmsGetValue(String name);

	String lmsSetValue(String name, String value);

	String finish(String param);

	String lmsGetLastError();

	String lmsGetErrorString(String errCode);

	String lmsGetDiagnostic(String error);
}