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

package com.tle.admin.common;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dytech.gui.filter.FilterModel;
import com.tle.common.NameValue;
import com.tle.common.usermanagement.user.valuebean.GroupBean;
import com.tle.common.usermanagement.util.UserBeanUtils;
import com.tle.core.remoting.RemoteUserService;

public class FilterGroupModel extends FilterModel<NameValue>
{
	private static final Log LOGGER = LogFactory.getLog(FilterGroupModel.class);

	private final RemoteUserService userService;

	public FilterGroupModel(RemoteUserService userService)
	{
		this.userService = userService;
	}

	@Override
	public List<NameValue> search(String pattern)
	{
		try
		{
			return removeExclusions(pairUp(userService.searchGroups(pattern)));
		}
		catch( Exception ex )
		{
			LOGGER.warn("Error searching groups matching " + pattern, ex);
			return new ArrayList<NameValue>(0);
		}
	}

	protected List<NameValue> pairUp(List<GroupBean> groups)
	{
		List<NameValue> results = new ArrayList<NameValue>(groups.size());

		for( GroupBean group : groups )
		{
			results.add(UserBeanUtils.formatGroup(group));
		}
		return results;
	}
}
