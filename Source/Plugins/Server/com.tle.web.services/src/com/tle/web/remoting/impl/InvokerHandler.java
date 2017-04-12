package com.tle.web.remoting.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.java.plugin.registry.Extension;
import org.springframework.web.HttpRequestHandler;

import com.google.inject.Provider;
import com.tle.core.guice.Bind;

@Bind
@Singleton
public class InvokerHandler extends AbstractRemoteHandler<Object>
{
	@Inject
	private Provider<RemoteInterceptor> interceptorProvider;

	@SuppressWarnings("nls")
	@Override
	protected HttpRequestHandler createHandlerFromBean(Extension extension, Object handlerBean)
	{
		RemoteInterceptor interceptor = interceptorProvider.get();
		Class<?> serviceInterface = tracker.getClassForName(extension, extension.getParameter("class").valueAsString());
		interceptor.setServiceInterface(serviceInterface);
		interceptor.setService(handlerBean);
		interceptor.setAllowGuests(tracker.isParamTrue(extension, "allowGuests", false));
		interceptor.setEnableRequestCapturing(tracker.isParamTrue(extension, "enableRequestCapturing", false));
		interceptor.setBeanClassLoader(handlerBean.getClass().getClassLoader());
		interceptor.afterPropertiesSet();
		return interceptor;
	}

	@Override
	protected String getExtensionPointName()
	{
		return "invoker"; //$NON-NLS-1$
	}

}
