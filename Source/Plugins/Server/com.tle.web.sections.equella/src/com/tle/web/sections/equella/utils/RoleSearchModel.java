package com.tle.web.sections.equella.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.dytech.edge.common.valuebean.RoleBean;
import com.google.common.base.Strings;
import com.tle.common.Format;
import com.tle.core.services.user.UserService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.standard.MultiSelectionList;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.model.SimpleOption;

public class RoleSearchModel extends DynamicHtmlListModel<RoleBean>
{
	private final TextField query;
	private final UserService userService;
	private MultiSelectionList<RoleBean> currentlySelected;
	private final int limit;

	public RoleSearchModel(TextField query, UserService userService, int limit)
	{
		this.query = query;
		this.userService = userService;
		this.limit = limit;
		setSort(true);
	}

	@Override
	protected Iterable<RoleBean> populateModel(SectionInfo info)
	{
		String queryText = query.getValue(info);
		Set<RoleBean> roles = new HashSet<RoleBean>();

		// refuse to search for full wildcard (try doing this on an LDAP server)
		if( searchable(queryText) )
		{
			roles.addAll(userService.searchRoles(queryText));
		}

		if( currentlySelected != null )
		{
			roles.addAll(userService.getInformationForRoles(currentlySelected.getSelectedValuesAsStrings(info))
				.values());
		}

		return roles;
	}

	@Override
	public List<Option<RoleBean>> getOptions(SectionInfo info)
	{
		List<Option<RoleBean>> roles = super.getOptions(info);
		// return the top X roles
		return roles.subList(0, Math.min(limit, roles.size()));
	}

	private boolean searchable(String queryText)
	{
		String q = Strings.nullToEmpty(queryText);
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
	protected Option<RoleBean> convertToOption(SectionInfo info, RoleBean ub)
	{
		return new SimpleOption<RoleBean>(Format.format(ub), ub.getUniqueID(), ub);
	}

	public void setCurrentlySelected(MultiSelectionList<RoleBean> currentlySelected)
	{
		this.currentlySelected = currentlySelected;
	}
}
