package com.tle.web.dispatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface WebFilterCallback
{
	void afterServlet(HttpServletRequest request, HttpServletResponse response);
}
