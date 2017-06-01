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
