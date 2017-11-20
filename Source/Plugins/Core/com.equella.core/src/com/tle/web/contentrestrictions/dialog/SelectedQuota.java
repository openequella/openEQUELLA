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

package com.tle.web.contentrestrictions.dialog;

import java.io.Serializable;
import java.util.Map;

import com.google.common.collect.Maps;

public class SelectedQuota implements Serializable
{
	private int quotaIndex;
	private long quota;
	private String expression;
	private final Map<String, Object> validationErrors = Maps.newHashMap();

	public SelectedQuota()
	{
	}

	public SelectedQuota(int quotaIndex, long quota, String expression)
	{
		this.quotaIndex = quotaIndex;
		this.quota = quota;
		this.expression = expression;
	}

	public int getQuotaIndex()
	{
		return quotaIndex;
	}

	public void setQuotaIndex(int quotaIndex)
	{
		this.quotaIndex = quotaIndex;
	}

	public long getQuota()
	{
		return quota;
	}

	public void setQuota(long quota)
	{
		this.quota = quota;
	}

	public String getExpression()
	{
		return expression;
	}

	public void setExpression(String expression)
	{
		this.expression = expression;
	}

	public Map<String, Object> getValidationErrors()
	{
		return validationErrors;
	}
}
