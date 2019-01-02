/*
 * Copyright 2019 Apereo
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
