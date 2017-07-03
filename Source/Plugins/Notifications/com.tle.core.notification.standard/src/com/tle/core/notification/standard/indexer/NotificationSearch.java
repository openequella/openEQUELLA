package com.tle.core.notification.standard.indexer;

import java.util.List;

import com.tle.common.search.DefaultSearch;
import com.tle.common.searching.Field;
import com.tle.common.searching.SortField;
import com.tle.common.usermanagement.user.CurrentUser;

public class NotificationSearch extends DefaultSearch
{
	private static final long serialVersionUID = 1L;

	@Override
	public String getSearchType()
	{
		return NotificationIndex.INDEXID;
	}

	@Override
	protected void addExtraMusts(List<List<Field>> musts)
	{
		musts.add(createFields(NotificationIndex.FIELD_USER, CurrentUser.getUserID()));
	}

	@Override
	public SortField[] getSortFields()
	{
		return new SortField[]{new SortField(NotificationIndex.FIELD_DATE, true)};
	}
}
