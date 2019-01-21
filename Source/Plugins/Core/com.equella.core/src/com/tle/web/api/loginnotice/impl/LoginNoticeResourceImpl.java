package com.tle.web.api.loginnotice.impl;

import com.tle.core.guice.Bind;
import com.tle.core.settings.loginnotice.LoginNoticeService;
import com.tle.web.api.loginnotice.LoginNoticeResource;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.Response;

@Bind(LoginNoticeResource.class)
@Singleton
public class LoginNoticeResourceImpl implements LoginNoticeResource
{
	@Inject
	LoginNoticeService noticeService;

	@Override
	public Response retrieveNotice()
	{
		String loginNotice = noticeService.getNotice();
		return Response.ok(loginNotice, "text/plain").build();
	}

	@Override
	public Response setNotice(String loginNotice)
	{
		noticeService.setNotice(loginNotice);
		return Response.ok().build();
	}

	@Override
	public Response deleteNotice()
	{
		noticeService.deleteNotice();
		return Response.ok().build();
	}
}
