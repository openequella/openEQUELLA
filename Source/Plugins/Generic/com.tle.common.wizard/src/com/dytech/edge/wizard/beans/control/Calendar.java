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

package com.dytech.edge.wizard.beans.control;

public class Calendar extends WizardControl
{
	public static enum DateFormat
	{
		DMY, MY, Y;
	}

	private static final long serialVersionUID = 1;
	public static final String CLASS = "calendar"; //$NON-NLS-1$

	private boolean range;
	private DateFormat format;


	@Override
	public String getClassType()
	{
		return CLASS;
	}

	public boolean isRange()
	{
		return range;
	}

	public void setRange(boolean range)
	{
		this.range = range;
	}

	public DateFormat getFormat()
	{
		return format;
	}

	public void setFormat(DateFormat format)
	{
		this.format = format;
	}
}
