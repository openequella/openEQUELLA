package com.tle.web.core.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface ThreadLocalExtension
{
	void doFilter(HttpServletRequest request, HttpServletResponse response);
}
