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

package com.tle.web.core.servlet;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tle.core.guice.Bind;
import com.tle.web.ajax.services.ScormAPIHandler;

@SuppressWarnings("nls")
@Bind
@Singleton
public class ScormServlet extends HttpServlet
{
	@Inject
	private ScormAPIHandler handler;

	public static final String INITIALIZE = "initialize";
	public static final String TERMINATE = "terminate";
	public static final String GETVALUE = "getvalue";
	public static final String SETVALUE = "setvalue";
	public static final String COMMIT = "commit";
	public static final String GETLASTERROR = "getlasterror";
	public static final String GETERRORSTRING = "geterrorstring";
	public static final String GETDIAGNOSTIC = "getdiagnostic";

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		String method = req.getParameter("method");
		String responseString = null;
		if( method.equals(INITIALIZE) )
		{
			responseString = handler.initialize(req.getParameter("param1"));
		}
		else if( method.equals(TERMINATE) )
		{
			responseString = handler.terminate(req.getParameter("param1"));
		}
		else if( method.equals(GETVALUE) )
		{
			responseString = handler.getValue(req.getParameter("param1"));
		}
		else if( method.equals(SETVALUE) )
		{
			responseString = handler.setValue(req.getParameter("param1"), req.getParameter("param2"));
		}
		else if( method.equals(COMMIT) )
		{
			responseString = handler.commit(req.getParameter("param1"));
		}
		else if( method.equals(GETLASTERROR) )
		{
			responseString = handler.getLastError();
		}
		else if( method.equals(GETERRORSTRING) )
		{
			responseString = handler.getErrorString(req.getParameter("param1"));
		}
		else if( method.equals(GETDIAGNOSTIC) )
		{
			responseString = handler.getDiagnostic(req.getParameter("param1"));
		}
		else
		{
			throw new RuntimeException();
		}

		resp.getWriter().write(responseString);
	}
}
