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

package com.tle.web.sections.equella.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.dytech.edge.common.valuebean.UserBean;
import com.tle.common.Check;
import com.tle.common.Format;
import com.tle.core.services.user.UserService;
import com.tle.plugins.ump.UserDirectoryUtils;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.standard.MultiSelectionList;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.model.SimpleOption;

public class UserSearchModel extends DynamicHtmlListModel<UserBean>
{
	private final TextField query;
	private final UserService userService;
	private final Set<String> groupFilter;
	private final int limit;

	private MultiSelectionList<UserBean> currentlySelected;

	public UserSearchModel(TextField query, UserService userService, Set<String> groupFilter, int limit)
	{
		this.query = query;
		this.userService = userService;
		this.groupFilter = groupFilter;
		this.limit = limit;

		setSort(true);
	}

	@Override
	protected Iterable<UserBean> populateModel(SectionInfo info)
	{
		String queryText = query.getValue(info);
		Collection<UserBean> users = new HashSet<UserBean>();

		// refuse to search for full wildcard (try doing this on an LDAP server)
		if( UserDirectoryUtils.searchQueryContainsNonWildcards(queryText) )
		{
			if( !Check.isEmpty(groupFilter) )
			{
				for( String groupUuid : groupFilter )
				{
					users.addAll(userService.searchUsers(queryText, groupUuid, true));
					if( users.size() >= limit )
					{
						break;
					}
				}
			}
			else
			{
				users.addAll(userService.searchUsers(queryText));
			}
		}

		if( currentlySelected != null )
		{
			users.addAll(userService.getInformationForUsers(currentlySelected.getSelectedValuesAsStrings(info))
				.values());
		}

		return users;
	}

	@Override
	public List<Option<UserBean>> getOptions(SectionInfo info)
	{
		List<Option<UserBean>> users = super.getOptions(info);
		// return the top X users
		return users.subList(0, Math.min(limit, users.size()));
	}

	@Override
	protected Option<UserBean> convertToOption(SectionInfo info, UserBean ub)
	{
		return new SimpleOption<UserBean>(Format.format(ub), ub.getUniqueID(), ub);
	}

	public void setCurrentlySelected(MultiSelectionList<UserBean> currentlySelected)
	{
		this.currentlySelected = currentlySelected;
	}
}