package com.tle.admin.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dytech.edge.common.valuebean.UserBean;
import com.dytech.gui.filter.FilterModel;
import com.tle.common.Format;
import com.tle.core.remoting.RemoteUserService;

/**
 * @author Nicholas Read
 */
public class FilterUserBeanModel extends FilterModel<UserBean>
{
	private static final Log LOGGER = LogFactory.getLog(FilterUserBeanModel.class);

	private RemoteUserService userService;

	public FilterUserBeanModel(RemoteUserService userService)
	{
		this.userService = userService;
	}

	@Override
	public List<UserBean> search(String pattern)
	{
		try
		{
			List<UserBean> users = removeExclusions(userService.searchUsers(pattern));
			Collections.sort(users, Format.USER_BEAN_COMPARATOR);
			return users;
		}
		catch( Exception ex )
		{
			LOGGER.warn("Error searching for users matching " + pattern, ex);
			return new ArrayList<UserBean>(0);
		}
	}
}
