/*
 * Created on 12/01/2006
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

import org.springframework.core.NestedRuntimeException;
import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;
import org.springframework.remoting.support.RemoteInvocation;

import com.dytech.edge.exceptions.RuntimeApplicationException;
import com.tle.common.util.Logger;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginAwareObjectInputStream;
import com.tle.core.plugins.PluginAwareObjectOutputStream;
import com.tle.core.services.InitialiserService;
import com.tle.core.services.LoggingService;
import com.tle.core.user.CurrentUser;
import com.tle.exceptions.InvalidSessionException;

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
	private boolean allowGuests;

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
	public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException,
		IOException
	{
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

	@SuppressWarnings("nls")
	protected void checkAuthentication(RemoteInvocation invocation, final Object targetObject)
	{
		if( !allowGuests && CurrentUser.isGuest() )
		{
			logger.error("The user is not logged in: trying to invoke " + targetObject.getClass().getName() + "::"
				+ invocation.getMethodName());
			throw new InvalidSessionException("User is not logged in");
		}
	}

	@Override
	protected ObjectInputStream createObjectInputStream(InputStream is) throws IOException
	{
		return new PluginAwareObjectInputStream(is);
	}

	@Override
	protected Object invoke(final RemoteInvocation invocation, final Object targetObject) throws NoSuchMethodException,
		IllegalAccessException, InvocationTargetException
	{
		Throwable unwrapped = null;

		checkAuthentication(invocation, targetObject);
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

	/**
	 * Invokable by Spring framework.
	 */
	public void setAllowGuests(boolean allowGuests)
	{
		this.allowGuests = allowGuests;
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
