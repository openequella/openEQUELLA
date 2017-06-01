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

package com.tle.web.activation;

import java.util.Date;

import com.tle.common.search.DefaultSearch;
import com.tle.core.activation.ActivationConstants;
import com.tle.core.guice.Bind;

@Bind
public class ActivationSearch extends DefaultSearch
{
	private static final long serialVersionUID = 1L;

	@Override
	public String getSearchType()
	{
		return ActivationConstants.ACTIVATION_INDEX_ID;
	}

	@Override
	public String getPrivilege()
	{
		return ActivationConstants.VIEW_ACTIVATION_ITEM;
	}

	@Override
	public String getPrivilegeToCollect()
	{
		return ActivationConstants.DELETE_ACTIVATION_ITEM;
	}

	@Override
	public Date[] getDateRange()
	{
		return dateRange;
	}

	@Override
	public void setDateRange(Date[] dateRange)
	{
		this.dateRange = dateRange;
	}
}
