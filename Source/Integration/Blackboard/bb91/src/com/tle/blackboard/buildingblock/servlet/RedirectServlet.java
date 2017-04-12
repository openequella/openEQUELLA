package com.tle.blackboard.buildingblock.servlet;

import static com.tle.blackboard.common.BbUtil.CONTENT_ID;
import static com.tle.blackboard.common.BbUtil.COURSE_ID;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.google.common.base.Strings;
import com.tle.blackboard.buildingblock.Configuration;
import com.tle.blackboard.buildingblock.data.WrappedUser;

/*
 * We still need this class due to the Equella course tool link (see the BB
 * manifest). We could replace it later, but not right now.
 */
@SuppressWarnings("nls")
//@NonNullByDefault
public class RedirectServlet extends HttpServlet
{
	private static final Logger LOGGER = Logger.getLogger(RedirectServlet.class);

	private static final String NOSEARCH_TEMPLATE = "blackboardNoSearch";
	private static final String SEARCH_TEMPLATE = "blackboardSearch";
	private static final String CHARSET_ENCODING = "UTF-8";

	@Override
	protected void service(@SuppressWarnings("null") HttpServletRequest request,
		@SuppressWarnings("null") HttpServletResponse response) throws ServletException, IOException
	{
		// FIXME: use a LTI launch!

		String forward = (String) request.getAttribute("forward");
		if( forward == null )
		{
			forward = request.getParameter("forward");
		}
		String template = (String) request.getAttribute("template");
		final Map<String, Object> paramMap = new HashMap<String, Object>();
		WrappedUser user = null;
		boolean login = "true".equals(request.getAttribute("login"));
		try
		{
			user = WrappedUser.getUser(request);

			final String serverURL = Configuration.instance().getEquellaUrl();

			if( template != null && template.equals("search") )
			{
				template = SEARCH_TEMPLATE;
			}
			else
			{
				template = NOSEARCH_TEMPLATE;
			}
			if( !login )
			{
				paramMap.put("token", user.getToken());
			}
			paramMap.put("bbsession", user.getSessionId());
			paramMap.put("bburl", getBbUrl(request));
			paramMap.put("_template", template);
			paramMap.put(COURSE_ID, request.getAttribute(COURSE_ID));
			paramMap.put(CONTENT_ID, request.getAttribute(CONTENT_ID));
			paramMap.put("method", "bb");

			paramMap.put("action", setupForward(paramMap, request, forward, request.getParameter("action")));

			final String options = request.getParameter("options");
			if( options != null )
			{
				paramMap.put("options", options);
			}

			final String location = serverURL + "signon.do?" + getParameterString(paramMap);
			response.sendRedirect(location);
		}
		catch( final Exception e )
		{
			LOGGER.error("Error redirect request to " + forward, e);
			request.setAttribute("javax.servlet.jsp.jspException", e);
			request.getRequestDispatcher("/error.jsp").forward(request, response);
		}
		finally
		{
			if( user != null )
			{
				user.clearContext();
			}
		}
	}

	private String setupForward(Map<String, Object> paramMap, HttpServletRequest request, String forward, String action)
	{
		URL forwardUrl;
		try
		{
			forwardUrl = new URL("http", "local", forward);
			final Map<String, Object> forwardMap = parseParamString(forwardUrl.getQuery());

			final String method = (String) request.getAttribute("method");
			if( method != null )
			{
				forwardMap.put("method", method);
			}

			paramMap.put("contentName", forwardMap.get("contentName"));
			paramMap.put("courseCode", forwardMap.get("courseCode"));

			if( Strings.isNullOrEmpty(action) )
			{
				forwardMap.remove("contentName");
				forwardMap.remove("courseCode");
				forwardMap.remove("startDate");
				forwardMap.remove("endDate");
				forwardMap.remove("courseName");

				return forwardUrl.getPath() + '?' + getParameterString(forwardMap);
			}
			else if( action.equals("structured") )
			{
				// YUCK
				paramMap.put("structure", forwardMap.get("structure"));
			}

			String restriction = (String) forwardMap.get("restriction");
			if( restriction != null )
			{
				paramMap.put(restriction, true);
			}

			return action;
		}
		catch( MalformedURLException e )
		{
			throw new RuntimeException(e);
		}
	}

	private static String getBbUrl(HttpServletRequest request)
	{
		StringBuilder bburl = new StringBuilder("http");
		if( request.isSecure() )
		{
			bburl.append('s');
		}
		bburl.append("://");
		bburl.append(request.getServerName());
		bburl.append(':');
		bburl.append(request.getServerPort());
		bburl.append(request.getContextPath());
		bburl.append('/');

		return bburl.toString();
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> parseParamString(String query)
	{
		final Map<String, Object> paramMap = new HashMap<String, Object>();

		if( query != null && !query.equals("") )
		{
			try
			{
				final String[] qparams = query.split("&");
				for( int i = 0; i < qparams.length; i++ )
				{
					final String qparam = qparams[i];
					final String[] nameVal = qparam.split("=");
					if( nameVal.length == 2 )
					{
						final String name = URLDecoder.decode(nameVal[0], "UTF-8");
						Object value = URLDecoder.decode(nameVal[1], "UTF-8");
						Object current = paramMap.get(name);
						if( current != null )
						{
							if( !(current instanceof Collection) )
							{
								final ArrayList<Object> list = new ArrayList<Object>();
								list.add(current);
								current = list;
							}
							((Collection<Object>) current).add(value);
							value = current;
						}
						paramMap.put(name, value);
					}
				}
			}
			catch( final Exception e )
			{
				throw new RuntimeException(e);
			}
		}
		return paramMap;
	}

	private static String getParameterString(Map<String, Object> map)
	{
		// This is to "pass" on any parameters that have been passed to this jsp
		final StringBuilder parameters = new StringBuilder();

		boolean first = true;

		for( Map.Entry<String, Object> entry : map.entrySet() )
		{
			if( !first )
			{
				parameters.append("&");
			}
			else
			{
				first = false;
			}

			try
			{
				final String key = entry.getKey();
				final Object obj = entry.getValue();
				final Collection<Object> values;
				if( obj != null )
				{
					if( obj instanceof Object[] )
					{
						values = Arrays.asList((Object[]) obj);
					}
					else if( obj instanceof Collection )
					{
						values = (Collection<Object>) obj;
					}
					else
					{
						values = Collections.singleton(obj);
					}
				}
				else
				{
					values = null;
				}
				if( values != null )
				{
					for( Object value : values )
					{
						final String encName = URLEncoder.encode(key, CHARSET_ENCODING);
						final String encValue = URLEncoder.encode(value.toString(), CHARSET_ENCODING);
						parameters.append(encName);
						parameters.append('=');
						parameters.append(encValue);
					}
				}
			}
			catch( final UnsupportedEncodingException ex )
			{
				// This should never happen.... ever.
				// We can ensure that CHARSET_ENCODING will be supported.
				throw new RuntimeException("Problem encoding URLs as " + CHARSET_ENCODING);
			}
		}
		return parameters.toString();
	}
}
