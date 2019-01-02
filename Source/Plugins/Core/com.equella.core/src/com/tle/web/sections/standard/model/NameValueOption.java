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

package com.tle.web.sections.standard.model;

import com.tle.common.NameValue;
import com.tle.common.NameValueExtra;

public class NameValueOption<T> implements Option<T>
{
	private final NameValue nv;
	private final T object;
	private boolean disabled;

	public NameValueOption(NameValue nv, T object)
	{
		this.nv = nv;
		this.object = object;
	}

	@Override
	public T getObject()
	{
		return object;
	}

	@Override
	public String getName()
	{
		return nv.getName();
	}

	@Override
	public boolean isNameHtml()
	{
		return false;
	}

	@Override
	public String getValue()
	{
		return nv.getValue();
	}

	@Override
	public boolean isDisabled()
	{
		return disabled;
	}

	@Override
	public boolean equals(Object obj)
	{
		if( this == obj )
		{
			return true;
		}

		if( !(obj instanceof Option<?>) )
		{
			return false;
		}

		return ((Option<?>) obj).getValue().equals(nv.getValue());
	}

	@Override
	public int hashCode()
	{
		return nv.getValue().hashCode();
	}

	@Override
	public boolean hasAltTitleAttr()
	{
		return NameValueExtra.class.isAssignableFrom(object.getClass()) && ((NameValueExtra) object).getExtra() != null
			&& ((NameValueExtra) object).getExtra().length() > 0;
	}

	@Override
	public String getAltTitleAttr()
	{
		return ((NameValueExtra) object).getExtra();
	}

	@Override
	public void setDisabled(boolean disabled)
	{
		this.disabled = disabled;
	}

	@Override
	public String getGroupName()
	{
		return null;
	}
}
