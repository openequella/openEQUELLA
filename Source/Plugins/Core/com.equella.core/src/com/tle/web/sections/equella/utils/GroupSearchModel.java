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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.tle.common.Check;
import com.tle.common.Format;
import com.tle.common.usermanagement.user.valuebean.GroupBean;
import com.tle.core.services.user.UserService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.standard.MultiSelectionList;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.model.SimpleOption;

public class GroupSearchModel extends DynamicHtmlListModel<GroupBean>
{
	private final TextField query;
	private final UserService userService;
	private final Set<String> groupFilter;
	private MultiSelectionList<GroupBean> currentlySelected;
	private final int limit;

	public GroupSearchModel(TextField query, UserService userService, Set<String> groupFilter, int limit)
	{
		this.query = query;
		this.userService = userService;
		this.limit = limit;
		this.groupFilter = groupFilter;
		setSort(true);
	}

	@Override
	protected Iterable<GroupBean> populateModel(SectionInfo info)
	{
		String queryText = query.getValue(info);
		Set<GroupBean> groups = new HashSet<GroupBean>();

		// refuse to search for full wildcard (try doing this on an LDAP server)
		if( searchable(queryText) )
		{
			if( Check.isEmpty(groupFilter) )
			{
				groups.addAll(userService.searchGroups(queryText));
			}
			else
			{
				for( String parentId : groupFilter )
				{
					groups.addAll(userService.searchGroups(queryText, parentId));
				}
			}
		}

		if( currentlySelected != null )
		{
			groups.addAll(userService.getInformationForGroups(currentlySelected.getSelectedValuesAsStrings(info))
				.values());
		}

		return groups;
	}

	@Override
	public List<Option<GroupBean>> getOptions(SectionInfo info)
	{
		List<Option<GroupBean>> groups = super.getOptions(info);
		// return the top X groups
		return groups.subList(0, Math.min(limit, groups.size()));
	}

	private boolean searchable(String queryText)
	{
		String q = Check.nullToEmpty(queryText);
		for( int i = 0; i < q.length(); i++ )
		{
			if( Character.isLetterOrDigit(q.codePointAt(i)) )
			{
				return true;
			}
		}
		return false;
	}

	@Override
	protected Option<GroupBean> convertToOption(SectionInfo info, GroupBean ub)
	{
		return new SimpleOption<GroupBean>(Format.format(ub), ub.getUniqueID(), ub);
	}

	public void setCurrentlySelected(MultiSelectionList<GroupBean> currentlySelected)
	{
		this.currentlySelected = currentlySelected;
	}
}
