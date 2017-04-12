/*
 * Created on 4/11/2005
 */

package com.tle.common.applet.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;

import org.springframework.remoting.httpinvoker.AbstractHttpInvokerRequestExecutor;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;
import org.springframework.remoting.httpinvoker.SimpleHttpInvokerRequestExecutor;
import org.springframework.remoting.support.RemoteInvocation;

import com.tle.common.applet.SessionHolder;
import com.tle.core.plugins.PluginAwareObjectInputStream;
import com.tle.core.plugins.PluginAwareObjectOutputStream;

public final class ClientProxyFactory
{
	public static <T> T createSessionProxy(ClientService client, Class<T> api, String service)
	{
		return createSessionProxy(client.getSession(), api, client.getServerURL(), service);
	}

	public static <T> T createSessionProxy(SessionHolder sessionHolder, Class<T> api, URL url, String service)
	{
		try
		{
			url = new URL(url, url.getPath() + service);
		}
		catch( MalformedURLException e )
		{
			throw new RuntimeException(e);
		}
		return create(sessionHolder, api, createProxy(api, url));
	}

	@SuppressWarnings("unchecked")
	public static <T> T createProxy(Class<T> api, URL url)
	{
		HttpInvokerProxyFactoryBean factory = new HttpInvokerProxyFactoryBean();
		factory.setServiceUrl(url.toString());
		factory.setServiceInterface(api);
		AbstractHttpInvokerRequestExecutor requestExecutor = new PluginAwareSimpleHttpInvokerRequestExecutor();
		factory.setHttpInvokerRequestExecutor(requestExecutor);
		factory.setBeanClassLoader(api.getClassLoader());
		factory.afterPropertiesSet();
		return (T) factory.getObject();
	}

	public static class PluginAwareSimpleHttpInvokerRequestExecutor extends SimpleHttpInvokerRequestExecutor
	{
		@Override
		protected ObjectInputStream createObjectInputStream(InputStream is, String codebaseUrl) throws IOException
		{
			return new PluginAwareObjectInputStream(is);
		}

		@Override
		protected void writeRemoteInvocation(RemoteInvocation invocation, OutputStream os) throws IOException
		{
			ObjectOutputStream oos = new PluginAwareObjectOutputStream(decorateOutputStream(os));
			try
			{
				doWriteRemoteInvocation(invocation, oos);
				oos.flush();
			}
			finally
			{
				oos.close();
			}
		}

	}

	@SuppressWarnings("unchecked")
	private static <T> T create(SessionHolder session, Class<T> api, Object iface)
	{
		ClientProxy handler = new ClientProxy(session, iface);
		return (T) Proxy.newProxyInstance(api.getClassLoader(), new Class[]{api}, handler);
	}

	private ClientProxyFactory()
	{
		throw new Error();
	}
}
