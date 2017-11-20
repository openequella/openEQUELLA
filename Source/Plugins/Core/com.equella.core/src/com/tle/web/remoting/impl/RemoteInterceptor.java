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

package com.tle.web.remoting.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.tle.common.usermanagement.user.UserState;
import org.springframework.core.NestedRuntimeException;
import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;
import org.springframework.remoting.support.RemoteInvocation;

import com.dytech.edge.exceptions.RuntimeApplicationException;
import com.tle.common.util.Logger;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.equella.service.InitialiserService;
import com.tle.core.plugins.PluginAwareObjectInputStream;
import com.tle.core.plugins.PluginAwareObjectOutputStream;
import com.tle.core.services.LoggingService;
import com.tle.common.usermanagement.user.CurrentUser;

@Bind
public class RemoteInterceptor extends HttpInvokerServiceExporter
{
	private static final ThreadLocal<HttpServletRequest> REQUESTS = new ThreadLocal<HttpServletRequest>();

	@Inject
	private InitialiserService initialiser;

	/**
	 * For Guice plugins
	 * 
	 * @param initialiser
	 */
	public void setInitialiserService(InitialiserService initialiser)
	{
		this.initialiser = initialiser;
	}

	@SuppressWarnings("hiding")
	private Logger logger;

	private boolean enableRequestCapturing;

	public RemoteInterceptor()
	{
		super();
		// we handle our own logging
		setRegisterTraceInterceptor(false);
	}

	public static HttpServletRequest getRequest()
	{
		return REQUESTS.get();
	}

	@SuppressWarnings("nls")
	@Override
	public void handleRequest(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		UserState userState = CurrentUser.getUserState();
		if (userState.isGuest())
		{
			response.sendError(401, "Have to be logged in first");
			return;
		}
		Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
		if( enableRequestCapturing )
		{
			REQUESTS.set(request);
		}

		try
		{
			super.handleRequest(request, response);
		}
		// Exceptions don't seem to be thrown from handleRequest anymore.
		// It tries to send the exception back to client.
		catch( Exception t )
		{
			logger.error("Error being thrown back over the wire", t);

			if( t instanceof ServletException )
			{
				throw (ServletException) t;
			}
			else if( t instanceof IOException )
			{
				throw (IOException) t;
			}
			else if( t instanceof RuntimeException )
			{
				throw (RuntimeException) t;
			}
			else
			{
				throw new RuntimeApplicationException(t);
			}
		}
		finally
		{
			if( enableRequestCapturing )
			{
				REQUESTS.remove();
			}
		}
	}

	@Override
	protected ObjectInputStream createObjectInputStream(InputStream is) throws IOException
	{
		return new PluginAwareObjectInputStream(is);
	}

	@Override
	protected Object invoke(final RemoteInvocation invocation, final Object targetObject)
		throws NoSuchMethodException, IllegalAccessException, InvocationTargetException
	{
		Throwable unwrapped = null;
		try
		{
			Object rval = super.invoke(invocation, targetObject);
			rval = initialiser.unwrapHibernate(rval);
			rval = initialiser.initialise(rval, new RemoteSimplifier());
			return rval;
		}
		catch( InvocationTargetException e )
		{
			unwrapped = e.getTargetException();
			Throwable t2 = unwrapped;
			// We want to determine if hibernate exception is thrown at any
			// point
			while( t2 != null )
			{
				if( t2 instanceof NestedRuntimeException )
				{
					logger.error(unwrapped.getMessage(), unwrapped);
					throw new RuntimeApplicationException(unwrapped.getMessage());
				}
				t2 = t2.getCause();
			}
			logger.error("Error invoking " + invocation.getMethodName(), unwrapped);
			throw e;
		}
	}

	public void setEnableRequestCapturing(boolean enableRequestCapturing)
	{
		this.enableRequestCapturing = enableRequestCapturing;
	}

	@Inject
	public void setLoggingService(LoggingService loggingService)
	{
		logger = loggingService.getLogger(RemoteInterceptor.class);
	}

	@Override
	protected ObjectOutputStream createObjectOutputStream(OutputStream os) throws IOException
	{
		return new PluginAwareObjectOutputStream(os);
	}
}
