package com.tle.web.dispatcher;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface WebFilter
{
	FilterResult filterRequest(HttpServletRequest request, HttpServletResponse response) throws IOException,
		ServletException;
}
