/*
 * Copyright 2019 Apereo
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

import java.util.Map;

import com.dytech.edge.common.Constants;
import com.tle.core.services.user.UserService;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.web.sections.BookmarkModifier;
import com.tle.web.sections.SectionInfo;

public class TokenModifier implements BookmarkModifier
{
	private UserService userService;

	public TokenModifier(UserService userService)
	{
		this.userService = userService;
	}

	@Override
	public void addToBookmark(SectionInfo info, Map<String, String[]> bookmarkState)
	{
		String token = userService.getGeneratedToken(Constants.APPLET_SECRET_ID, CurrentUser.getUsername());
		bookmarkState.put("token", new String[]{token}); //$NON-NLS-1$
	}

}
