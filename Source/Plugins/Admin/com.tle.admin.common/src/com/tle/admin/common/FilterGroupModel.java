package com.tle.admin.common;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dytech.edge.common.valuebean.GroupBean;
import com.dytech.gui.filter.FilterModel;
import com.tle.common.NameValue;
import com.tle.common.util.UserBeanUtils;
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
