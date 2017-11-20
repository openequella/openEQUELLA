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

import org.apache.commons.fileupload.ProgressListener;
import org.apache.log4j.Logger;

import com.tle.common.Check;
import com.tle.common.beans.progress.ProgressCallback;
import com.tle.core.guice.Bind;
import com.tle.core.services.user.UserSessionService;

import net.sf.json.JSONSerializer;

@Bind
@Singleton
public class ProgressServlet extends HttpServlet
{
	private static final String KEY_PREFIX = "progress"; //$NON-NLS-1$
	private static final Logger LOGGER = Logger.getLogger(ProgressServlet.class);

	@Inject
	private UserSessionService sessionService;

	public void addCallback(String id, ProgressCallback callback)
	{
		String key = getKey(id);
		// TODO: Callback needs to be immutable
		sessionService.setAttribute(key, callback);
	}

	public void addListener(String id, ProgressListener listener)
	{
		String key = getKey(id);
		sessionService.setAttribute(key, listener);
	}

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException
	{
		final String id = request.getParameter("id"); //$NON-NLS-1$
		final String key = getKey(id);

		ProgressCallback callback = null;
		try
		{
			final Object listenerOrCallback = sessionService.getAttribute(key);
			callback = (ProgressCallback) listenerOrCallback;

			// FIXME: we need to do something about this! The progress bar can
			// request the callback before it's been added to session,
			// but it will also keep requesting the callback forever in cases
			// where the session is lost. We cannot know the difference between
			// the two...
			if( callback == null )
			{
				callback = new ProgressCallback()
				{
					private static final long serialVersionUID = 1L;

					@Override
					public ProgressResponse getResponse()
					{
						return new ProgressResponse()
						{
							@Override
							public String getErrorMessage()
							{
								// Do not change!
								return "PROGRESSID_NOT_FOUND";
							}

							@Override
							public boolean isFinished()
							{
								return true;
							}
						};
					}

					@Override
					public boolean isFinished()
					{
						return true;
					}

					@Override
					public void setFinished()
					{
						// Nothng to do here
					}
				};
			}
		}
		catch( final Exception e )
		{
			LOGGER.error("ProgressServlet Error", e); //$NON-NLS-1$
			callback = new ProgressCallback()
			{
				private static final long serialVersionUID = 1L;

				@Override
				public ProgressResponse getResponse()
				{
					return new ProgressResponse()
					{
						@Override
						public String getErrorMessage()
						{
							return e.getMessage();
						}

						@Override
						public boolean isFinished()
						{
							return true;
						}
					};
				}

				@Override
				public boolean isFinished()
				{
					return true;
				}

				@Override
				public void setFinished()
				{
					// Nothing to do here
				}
			};
		}

		resp.setHeader("Cache-Control", "no-cache, no-store"); //$NON-NLS-1$//$NON-NLS-2$
		resp.setContentType("application/json"); //$NON-NLS-1$
		resp.getWriter().write(JSONSerializer.toJSON(callback.getResponse()).toString());

		// if( callback.isFinished() )
		// {
		// sessionService.setAttribute(key, null);
		// }
	}

	private String getKey(String id)
	{
		if( !Check.isEmpty(id) )
		{
			return KEY_PREFIX + '_' + id;
		}
		return KEY_PREFIX;
	}
}