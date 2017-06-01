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