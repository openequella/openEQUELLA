package com.tle.web.sections.equella.utils;

import java.util.Map;

import com.dytech.edge.common.Constants;
import com.tle.core.services.user.UserService;
import com.tle.core.user.CurrentUser;
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
