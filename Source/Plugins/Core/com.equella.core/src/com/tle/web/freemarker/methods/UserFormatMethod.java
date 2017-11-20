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

package com.tle.web.freemarker.methods;

import java.util.List;

import com.tle.common.Format;
import com.tle.common.Utils;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.core.services.user.UserService;

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