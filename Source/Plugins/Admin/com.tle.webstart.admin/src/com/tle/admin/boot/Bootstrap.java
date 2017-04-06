package com.tle.admin.boot;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;
import org.springframework.remoting.httpinvoker.SimpleHttpInvokerRequestExecutor;
import org.springframework.remoting.support.RemoteInvocation;

import com.dytech.devlib.Base64;
import com.dytech.edge.common.Version;
import com.tle.admin.PluginServiceImpl;
import com.tle.client.harness.HarnessInterface;
import com.tle.common.Check;
import com.tle.core.plugins.PluginAwareObjectInputStream;
import com.tle.core.plugins.PluginAwareObjectOutputStream;
import com.tle.core.remoting.RemoteLoginService;
import com.tle.core.remoting.RemotePluginDownloadService;

@SuppressWarnings("nls")
public final class Bootstrap
{
	private static final Pattern LOCALE_REGEX = Pattern.compile("^([a-z][a-z])?(?:_([A-Z][A-Z])?(?:_(\\w+))?)?$");

	public static final String PROPERTY_PREFIX = "jnlp.";
	public static final String TOKEN_PARAMETER = PROPERTY_PREFIX + "SESSION";
	public static final String ENDPOINT_PARAMETER = PROPERTY_PREFIX + "ENDPOINT";
	public static final String LOCALE_PARAMETER = PROPERTY_PREFIX + "LOCALE";
	public static final String INSTITUTION_NAME_PARAMETER = PROPERTY_PREFIX + "INSTITUTIONNAME";

	public static void main(String[] args)
	{
		try
		{
			System.setSecurityManager(null);
			String token = new String(new Base64().decode(System.getProperty(TOKEN_PARAMETER)), "UTF-8");
			URL endpointUrl = new URL(System.getProperty(ENDPOINT_PARAMETER));
			Locale locale = parseLocale(System.getProperty(LOCALE_PARAMETER));
			RemoteLoginService loginService = createInvoker(RemoteLoginService.class, endpointUrl);
			loginService.loginWithToken(token);
			PluginServiceImpl pluginService = new PluginServiceImpl(endpointUrl, Version.load(Bootstrap.class)
				.getCommit(), createInvoker(RemotePluginDownloadService.class, endpointUrl));
			pluginService.registerPlugins();
			HarnessInterface client = (HarnessInterface) pluginService.getBean("com.tle.admin.common",
				"com.tle.admin.AdminConsole");
			client.setPluginService(pluginService);
			client.setLocale(locale);
			client.setEndpointURL(endpointUrl);
			client.start();
		}
		catch( Exception ex )
		{
			throw new RuntimeException(ex);
		}
	}

	public static Locale parseLocale(String localeString)
	{
		if( localeString != null )
		{
			Matcher m = LOCALE_REGEX.matcher(localeString.trim());
			if( m.matches() )
			{
				return new Locale(Check.nullToEmpty(m.group(1)), Check.nullToEmpty(m.group(2)), Check.nullToEmpty(m
					.group(3)));
			}
		}
		throw new RuntimeException("Error parsing locale: " + localeString);
	}

	@SuppressWarnings("unchecked")
	protected static <T> T createInvoker(Class<T> clazz, URL endpointUrl)
	{
		HttpInvokerProxyFactoryBean factory = new HttpInvokerProxyFactoryBean();
		try
		{
			factory.setServiceUrl(new URL(endpointUrl, "invoker/" + clazz.getName() + ".service").toString());
		}
		catch( MalformedURLException e )
		{
			throw new RuntimeException(e);
		}
		factory.setServiceInterface(clazz);
		factory.setHttpInvokerRequestExecutor(new PluginAwareSimpleHttpInvokerRequestExecutor());
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

	// Noli me tangere constructor, because Sonar likes it that way for
	// non-instantiated utility classes
	private Bootstrap()
	{
		throw new Error();
	}
}
