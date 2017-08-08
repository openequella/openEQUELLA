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
