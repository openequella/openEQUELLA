package com.tle.web.api.payment.store.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tle.core.guice.Bind;
import com.tle.web.api.payment.store.CurrentStoreFront;
import com.tle.web.core.filter.ThreadLocalExtension;

/**
 * @author Aaron
 */
@Bind
public class ClearStoreApi implements ThreadLocalExtension
{
	@Override
	public void doFilter(HttpServletRequest request, HttpServletResponse response)
	{
		CurrentStoreFront.set(null);
	}
}
