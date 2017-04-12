/*
 * Created on 14/03/2006
 */
package com.tle.web.core.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dytech.edge.exceptions.NotFoundException;

@SuppressWarnings("nls")
public abstract class AbstractIdPathServlet extends HttpServlet
{
	@Override
	protected final void service(HttpServletRequest request, HttpServletResponse response) throws ServletException,
		IOException
	{
		String path = request.getPathInfo();
		int i = path.indexOf('/', 1);
		if( i < 0 )
		{
			throw new NotFoundException(path, true);
		}

		if( path.startsWith("/") )
		{
			path = path.substring(1);
		}

		int firstPart = path.indexOf('/');
		if( firstPart < 0 )
		{
			throw new NotFoundException(path, true);
		}

		service(request, response, path.substring(0, firstPart), path.substring(firstPart + 1));
	}

	protected abstract void service(HttpServletRequest request, HttpServletResponse response, String id, String path)
		throws ServletException, IOException;
}
