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

package com.tle.core.harvester.old.dsoap;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.URL;

import org.w3c.dom.Element;

@SuppressWarnings("nls")
public class SoapClientHelper implements InvocationHandler
{
	final private static String PARAM_NAME = "Parameter";

	private final String host;
	private final int port;
	private final String endpoint;
	private final boolean secure;

	protected SoapClientHelper(URL url)
	{
		this.host = url.getHost();
		this.port = url.getPort();
		this.endpoint = url.getFile();
		this.secure = "https".equals(url.getProtocol());
	}

	final public static Object newInstance(URL url, Class<?> clazz)
	{
		SoapClientHelper pi = new SoapClientHelper(url);
		return (pi.initialize(new Class[]{clazz}));
	}

	private Object initialize(Class<?>[] interfaces)
	{
		return (java.lang.reflect.Proxy.newProxyInstance(getClass().getClassLoader(), interfaces, this));
	}

	// Implementation of the java.lang.reflect.InvocationHandler interface, in
	// which case we'll silence Sonar's objections about 'throws Throwable'
	@Override
	final public Object invoke(Object proxy, Method m, Object[] args) throws Throwable // NOSONAR
	{
		SoapCall call = new SoapCall(host, port, endpoint, m.getName(), secure);

		Class<?>[] params = m.getParameterTypes();
		for( int i = 0; i < args.length; i++ )
		{
			Class<?> param = params[i];
			RequestParameter p;
			if( String.class.isAssignableFrom(param) )
			{
				p = new RequestParameter(PARAM_NAME + (i + 1), (String) args[i]);
			}
			else if( boolean.class.isAssignableFrom(param) )
			{
				p = new RequestParameter(PARAM_NAME + (i + 1), ((Boolean) args[i]).booleanValue());
			}
			else if( int.class.isAssignableFrom(param) )
			{
				p = new RequestParameter(PARAM_NAME + (i + 1), ((Integer) args[i]).intValue());
			}
			else if( byte[].class.isAssignableFrom(param) )
			{
				p = new RequestParameter(PARAM_NAME + (i + 1), (byte[]) args[i]);
			}
			else if( String[].class.isAssignableFrom(param) )
			{
				p = new RequestParameter(PARAM_NAME + (i + 1), (String[]) args[i]);
			}
			else if( int[].class.isAssignableFrom(param) )
			{
				p = new RequestParameter(PARAM_NAME + (i + 1), (int[]) args[i]);
			}
			else if( Element.class.isAssignableFrom(param) )
			{
				p = new RequestParameter(PARAM_NAME + (i + 1), (Element) args[i]);
			}
			else if( Element[].class.isAssignableFrom(param) )
			{
				p = new RequestParameter(PARAM_NAME + (i + 1), (Element[]) args[i]);
			}
			else if( short.class.isAssignableFrom(param) )
			{
				p = new RequestParameter(PARAM_NAME + (i + 1), ((Short) args[i]).shortValue());
			}
			else if( long.class.isAssignableFrom(param) )
			{
				p = new RequestParameter(PARAM_NAME + (i + 1), ((Long) args[i]).longValue());
			}
			else if( float.class.isAssignableFrom(param) )
			{
				p = new RequestParameter(PARAM_NAME + (i + 1), ((Float) args[i]).floatValue());
			}
			else if( double.class.isAssignableFrom(param) )
			{
				p = new RequestParameter(PARAM_NAME + (i + 1), ((Double) args[i]).doubleValue());
			}
			else
			{
				p = new RequestParameter(PARAM_NAME + (i + 1), null, RequestParameter.RP_UNKNOWN);
			}
			call.addParameter(p);
		}

		Class<?> returnType = m.getReturnType();
		Object o;
		if( String.class.isAssignableFrom(returnType) )
		{
			o = call.getStringResult();
		}
		else if( int.class.isAssignableFrom(returnType) )
		{
			o = Integer.valueOf(call.getIntResult());
		}
		else if( float.class.isAssignableFrom(returnType) )
		{
			o = Float.valueOf(call.getFloatResult());
		}
		else if( double.class.isAssignableFrom(returnType) )
		{
			o = Double.valueOf(call.getDoubleResult());
		}
		else if( long.class.isAssignableFrom(returnType) )
		{
			o = Long.valueOf(call.getLongResult());
		}
		else if( String[].class.isAssignableFrom(returnType) )
		{
			o = call.getStringArrayResult();
		}
		else if( Element[].class.isAssignableFrom(returnType) )
		{
			o = call.getElementArrayResult();
		}
		else if( Element.class.isAssignableFrom(returnType) )
		{
			o = call.getElementResult();
		}
		else if( int[].class.isAssignableFrom(returnType) )
		{
			o = call.getIntArrayResult();
		}
		else if( String[][].class.isAssignableFrom(returnType) )
		{
			o = call.getStringArrayArrayResult();
		}
		else if( boolean.class.isAssignableFrom(returnType) )
		{
			o = Boolean.valueOf(call.getBooleanResult());
		}
		else
		{
			o = call.getStringResult();
		}
		return o;
	}
}
