/*
 * Copyright 2019 Apereo
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

package com.tle.web.sections.equella.utils;

import com.tle.common.i18n.CurrentLocale;
import com.tle.web.sections.standard.model.Option;

public class VoidKeyOption implements Option<Void>
{
	private static final Object[] NO_VALUES = new Object[0];

	private final String name;
	private final Object[] nameValues;
	private final String value;
	private boolean disabled;

	public VoidKeyOption(String name, String value)
	{
		this(name, NO_VALUES, value);
	}

	public VoidKeyOption(String name, Object[] nameValues, String value)
	{
		this(name, nameValues, value, false);
	}

	public VoidKeyOption(String name, String value, boolean disabled)
	{
		this(name, NO_VALUES, value, disabled);
	}

	public VoidKeyOption(String name, Object[] nameValues, String value, boolean disabled)
	{
		this.name = name;
		this.nameValues = nameValues;
		this.value = value;
		this.disabled = disabled;
	}

	@Override
	public String getName()
	{
		return CurrentLocale.get(name, nameValues);
	}

	@Override
	public boolean isNameHtml()
	{
		return false;
	}

	@Override
	public Void getObject()
	{
		return null;
	}

	@Override
	public String getValue()
	{
		return value;
	}

	@Override
	public boolean isDisabled()
	{
		return disabled;
	}

	@Override
	public boolean hasAltTitleAttr()
	{
		return false;
	}

	@Override
	public String getAltTitleAttr()
	{
		return null;
	}

	@Override
	public String getGroupName()
	{
		return null;
	}

	@Override
	public void setDisabled(boolean disabled)
	{
		this.disabled = disabled;
	}
}
