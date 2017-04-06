/*
 * Created on 4/11/2005
 */

package com.tle.common.applet.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.tle.common.applet.SessionHolder;

public class ClientProxy implements InvocationHandler
{
	private final Object iface;
	final SessionHolder session;

	public ClientProxy(SessionHolder session, Object iface)
	{
		this.session = session;
		this.iface = iface;
	}

	// SOnar objects to 'throws Throwable' but here we're bound by external
	// invocations in CgLibProxy
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable // NOSONAR
	{
		return invoke(proxy, method, args, true);
	}

	private Object invoke(Object proxy, Method method, Object[] args, boolean retry) throws Throwable // NOSONAR
	{
		String methodName = method.getName();
		Method pMethod = iface.getClass().getMethod(methodName, method.getParameterTypes());
		try
		{
			return pMethod.invoke(iface, args);
		}
		catch( InvocationTargetException e )
		{
			throw e.getCause();
		}
	}
}
