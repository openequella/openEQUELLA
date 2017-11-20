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

package com.tle.common.quota.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.tle.common.settings.ConfigurationProperties;
import com.tle.common.settings.annotation.Property;
import com.tle.common.settings.annotation.PropertyDataList;
import com.tle.common.settings.annotation.PropertyList;

/**
 * @author Nicholas Read
 */
public class QuotaSettings implements ConfigurationProperties
{
	private static final long serialVersionUID = 1;
	public static final int QUOTA_GIG_PRECISION = 4;

	@PropertyList(key = "banned.extensions")
	private final List<String> bannedExtensions = new ArrayList<String>();

	@PropertyDataList(key = "user.quota.quotas", type = UserQuota.class)
	private final List<UserQuota> quotas = new ArrayList<UserQuota>();

	public QuotaSettings()
	{
		super();
	}

	public List<String> getBannedExtensions()
	{
		return bannedExtensions;
	}

	public List<UserQuota> getQuotas()
	{
		return quotas;
	}

	public static class UserQuota implements ConfigurationProperties
	{
		private static final long serialVersionUID = 1;

		@Property(key = "size")
		private long size;
		@Property(key = "expression")
		private String expression;

		/**
		 * Reflection only.
		 */
		public UserQuota()
		{
			//
		}

		public String getExpression()
		{
			return expression;
		}

		public void setExpression(String expression)
		{
			this.expression = expression;
		}

		public long getSize()
		{
			return size;
		}

		public void setSize(long size)
		{
			this.size = size;
		}

		@Override
		public int hashCode()
		{
			return Long.valueOf(size).hashCode() + (expression == null ? 0 : expression.hashCode());
		}

		@Override
		public boolean equals(Object obj)
		{
			if( obj instanceof UserQuota )
			{
				final UserQuota other = (UserQuota) obj;
				if( size == other.size && Objects.equals(expression, other.expression) )
				{
					return true;
				}
			}
			return false;
		}
	}
}
