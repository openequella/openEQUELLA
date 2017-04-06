package com.tle.core.xslt.ext;

import javax.inject.Inject;

import org.w3c.dom.Node;

import com.dytech.edge.common.valuebean.UserBean;
import com.tle.common.util.UserXmlUtils;
import com.tle.core.services.user.UserService;

public class Users
{
	@Inject
	private static UserService userService;

	public Node getUserById(String id)
	{
		UserBean bean = userService.getInformationForUser(id);
		return UserXmlUtils.getUserAsXml(bean).getRootElement();
	}
}
