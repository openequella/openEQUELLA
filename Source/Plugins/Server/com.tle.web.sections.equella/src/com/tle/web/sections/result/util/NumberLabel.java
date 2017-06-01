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

package com.tle.web.sections.result.util;

import java.io.Serializable;
import java.text.NumberFormat;

import com.tle.common.i18n.CurrentLocale;
import com.tle.web.sections.render.Label;

public class NumberLabel implements Label, Serializable
{
	private static final long serialVersionUID = 1L;

	protected final Number number;
	protected Integer minDecimals;
	protected Integer maxDecimals;

	public NumberLabel(Number number, int minDecimals, int maxDecimals)
	{
		this(number);
		this.minDecimals = minDecimals;
		this.maxDecimals = maxDecimals;
	}

	public NumberLabel(Number number)
	{
		this.number = number;
	}

	public Number getNumber()
	{
		return number;
	}

	public Integer getMinDecimals()
	{
		return minDecimals;
	}

	public NumberLabel setMinDecimals(Integer minDecimals)
	{
		this.minDecimals = minDecimals;
		return this;
	}

	public Integer getMaxDecimals()
	{
		return maxDecimals;
	}

	public NumberLabel setMaxDecimals(Integer maxDecimals)
	{
		this.maxDecimals = maxDecimals;
		return this;
	}

	@Override
	public String getText()
	{
		final NumberFormat numberFormat = NumberFormat.getInstance(CurrentLocale.getLocale());
		if( minDecimals != null )
		{
			numberFormat.setMinimumFractionDigits(minDecimals);
		}
		if( maxDecimals != null )
		{
			numberFormat.setMaximumFractionDigits(maxDecimals);
		}
		return numberFormat.format(number);
	}

	@Override
	public boolean isHtml()
	{
		return false;
	}
}
