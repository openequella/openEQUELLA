package com.tle.web.freemarker.methods;

import java.util.List;

import com.dytech.edge.common.valuebean.UserBean;
import com.tle.common.Format;
import com.tle.common.Utils;
import com.tle.core.services.user.UserService;
import com.tle.core.user.CurrentUser;

import freemarker.template.AdapterTemplateModel;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

public class UserFormatMethod implements TemplateMethodModelEx
{
	private final UserService userService;

	public UserFormatMethod(UserService userService)
	{
		this.userService = userService;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object exec(List args) throws TemplateModelException
	{
		UserBean user = null;
		Object userModel = args.get(0);
		if( userModel instanceof AdapterTemplateModel )
		{
			Object wrapped = ((AdapterTemplateModel) userModel).getAdaptedObject(Object.class);
			if( wrapped instanceof UserBean )
			{
				user = (UserBean) wrapped;
			}
		}
		if( user == null && userModel instanceof TemplateScalarModel )
		{
			String userId = ((TemplateScalarModel) userModel).getAsString();
			if( userId.equals("$LoggedInUser") ) //$NON-NLS-1$
			{
				user = CurrentUser.getDetails();
			}
			else
			{
				user = userService.getInformationForUser(userId);
			}
		}
		String format = Format.DEFAULT_USER_BEAN_FORMAT;
		if( args.size() > 1 )
		{
			Object formatModel = args.get(1);
			if( formatModel instanceof TemplateScalarModel )
			{
				format = ((TemplateScalarModel) formatModel).getAsString();
			}
		}

		return new SimpleScalar(Utils.ent(Format.format(user, format)));
	}
}