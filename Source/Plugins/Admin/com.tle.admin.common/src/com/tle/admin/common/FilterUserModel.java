package com.tle.admin.common;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dytech.edge.common.valuebean.UserBean;
import com.dytech.gui.filter.FilterModel;
import com.tle.common.NameValue;
import com.tle.common.util.UserBeanUtils;
import com.tle.core.remoting.RemoteUserService;

public class FilterUserModel extends FilterModel<NameValue>
{
	private static final Log LOGGER = LogFactory.getLog(FilterUserModel.class);

	private final RemoteUserService userService;

	public FilterUserModel(RemoteUserService userService)
	{
		this.userService = userService;
	}

	@Override
	public List<NameValue> search(String pattern)
	{
		try
		{
			return removeExclusions(pairUp(userService.searchUsers(pattern)));
		}
		catch( Exception ex )
		{
			LOGGER.warn("Error searching for users matching " + pattern, ex);
			return new ArrayList<NameValue>(0);
		}
	}

	protected List<NameValue> pairUp(List<UserBean> users)
	{
		List<NameValue> results = new ArrayList<NameValue>(users.size());
		for( UserBean user : users )
		{
			results.add(UserBeanUtils.formatUser(user));
		}
		return results;
	}
}
