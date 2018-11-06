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

package com.tle.web.remoting.resteasy;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.fasterxml.jackson.module.scala.DefaultScalaModule;
import com.tle.common.interfaces.equella.I18NSerializer;
import com.tle.common.interfaces.equella.RestStringsModule;
import com.tle.core.guice.Bind;
import com.tle.core.jackson.MapperExtension;
import com.tle.core.jackson.ObjectMapperService;
import com.tle.core.plugins.PluginBeanLocator;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.services.user.UserSessionService;
import com.tle.web.DebugSettings;
import com.tle.web.api.LegacyContentApi;
import com.tle.web.api.institution.AclResource;
import com.tle.web.api.institution.GdprResource;
import com.tle.web.api.item.SelectionApi;
import com.tle.web.api.language.LanguageResource;
import com.tle.web.api.searches.SearchConfigApi;
import com.tle.web.api.users.UserQueryResource;
import com.tle.web.remoting.rest.resource.InstitutionSecurityFilter;
import com.tle.web.api.settings.SettingsResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.Extension.Parameter;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.jboss.resteasy.spi.*;
import org.jboss.resteasy.util.GetRestful;
import scala.collection.GenTraversableOnce;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Application;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.*;

@Bind
@Singleton
@SuppressWarnings("nls")
public class RestEasyServlet extends HttpServletDispatcher implements MapperExtension
{
	private static final long serialVersionUID = 1L;

	@Inject
	private UserSessionService userSessionService;
	@Inject
	private PluginTracker<Object> tracker;
	@Inject
	private PluginService pluginService;
	@Inject
	private ObjectMapperService objectMapperService;
	@Inject
	private InstitutionSecurityFilter institutionSecurityFilter;

	@Override
	protected void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
		throws ServletException, IOException
	{
		userSessionService.preventSessionUse();
		try
		{
			super.service(httpServletRequest, httpServletResponse);
		}
		catch( Exception e )
		{
			if( e instanceof org.jboss.resteasy.spi.UnhandledException
				&& e.getCause() instanceof org.apache.catalina.connector.ClientAbortException )
			{
				// do nothing
				return;
			}
			throw e;
		}
	}

	@Override
	public void init(final ServletConfig servletConfig) throws ServletException
	{
		Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
		super.init(servletConfig);

		Dispatcher dispatcher = getDispatcher();
		Registry registry = dispatcher.getRegistry();
		RestEasyApplication application = (RestEasyApplication) dispatcher.getDefaultContextObjects()
			.get(Application.class);

		PluginBeanLocator coreLocator = pluginService.getBeanLocator("com.equella.core");
		Set<Class<?>> classes = application.getClasses();

		registry.addSingletonResource(new SettingsResource());
		classes.add(SettingsResource.class);
		registry.addSingletonResource(new LanguageResource());
		classes.add(LanguageResource.class);
		registry.addSingletonResource(new UserQueryResource());
		classes.add(UserQueryResource.class);
		registry.addSingletonResource(new AclResource());
		classes.add(AclResource.class);
		registry.addSingletonResource(new GdprResource());
		classes.add(GdprResource.class);
		registry.addSingletonResource(new LegacyContentApi());
		classes.add(LegacyContentApi.class);
		registry.addSingletonResource(new SelectionApi());
		classes.add(SelectionApi.class);
		registry.addSingletonResource(new SearchConfigApi());
		classes.add(SearchConfigApi.class);

		ResteasyProviderFactory providerFactory = dispatcher.getProviderFactory();
		providerFactory.registerProvider(SwaggerSerializers.class);
		providerFactory.registerProviderInstance(new JsonContextResolver());
		providerFactory.registerProvider(DefaultOptionsExceptionMapper.class);
		providerFactory.registerProvider(RestEasyExceptionMapper.class);
		providerFactory.registerProvider(CorsInterceptor.class);
		providerFactory.registerProvider(JacksonJsonProvider.class);
		providerFactory.registerProvider(CharsetInterceptor.class);
		providerFactory.registerProviderInstance(institutionSecurityFilter);
		List<Extension> extensions = tracker.getExtensions();
		for( Extension extension : extensions )
		{
			String pluginId = extension.getDeclaringPluginDescriptor().getId();
			PluginBeanLocator beanLocator = pluginService.getBeanLocator(pluginId);
			Collection<Parameter> clazzParams = extension.getParameters("class");

			for( Parameter parameter : clazzParams )
			{
				Parameter subParameter = parameter.getSubParameter("doc-listing");
				if( subParameter != null )
				{
					Class<?> clazz = tracker.getClassForName(extension, subParameter.valueAsString());
					registry.addResourceFactory(new BeanLocatorResource(clazz, beanLocator));
					if( GetRestful.isRootResource(clazz) )
					{
						classes.add(clazz);
					}
				}

				Class<?> clazz = tracker.getClassForName(extension, parameter.valueAsString());
				registry.addResourceFactory(new BeanLocatorResource(clazz, beanLocator));
				if( GetRestful.isRootResource(clazz) )
				{
					classes.add(clazz);
				}
			}
		}
	}

	@Provider
	public class JsonContextResolver implements ContextResolver<ObjectMapper>
	{
		private final ObjectMapper objectMapper;
		private final ObjectMapper scalaObjectMapper;

		public JsonContextResolver()
		{
			objectMapper = objectMapperService.createObjectMapper("rest");
			scalaObjectMapper = objectMapperService.createObjectMapper("rest");
			scalaObjectMapper.registerModule(new DefaultScalaModule());
			scalaObjectMapper.configure(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES, true);
		}

		@Override
		public ObjectMapper getContext(Class<?> beanClass)
		{
			if (scala.Product.class.isAssignableFrom(beanClass) || GenTraversableOnce.class.isAssignableFrom(beanClass))
			{
				return scalaObjectMapper;
			}
			return objectMapper;
		}
	}

	public static class BeanLocatorResource implements ResourceFactory
	{
		private final Class<?> clazz;
		private final PluginBeanLocator locator;

		// private Object cachedObject;

		public BeanLocatorResource(Class<?> clazz, PluginBeanLocator locator)
		{
			this.clazz = clazz;
			this.locator = locator;
		}

		@Override
		public void unregistered()
		{
			// nothing
		}

		@Override
		public void requestFinished(HttpRequest request, HttpResponse response, Object resource)
		{
			// nothing
		}

		@Override
		public void registered(ResteasyProviderFactory factory)
		{
			// nothing
		}

		@Override
		public Class<?> getScannableClass()
		{
			return clazz;
		}

		private synchronized Object getCachedObject()
		{
			// if( cachedObject == null )
			// {
			// cachedObject = locator.getBeanForType(clazz);
			// }
			// return cachedObject;
			return locator.getBeanForType(clazz);
		}

		@Override
		public Object createResource(HttpRequest request, HttpResponse response, ResteasyProviderFactory factory)
		{
			return getCachedObject();
		}
	}

	@Override
	public void extendMapper(ObjectMapper mapper)
	{
		SimpleModule restModule = new SimpleModule("RestModule");
		restModule.addSerializer(new I18NSerializer());
		mapper.registerModule(restModule);
		mapper.registerModule(new JavaTypesModule());

		mapper.registerModule(new RestStringsModule());
		mapper.setSerializationInclusion(Include.NON_ABSENT);

		// dev mode!
		if( DebugSettings.isDebuggingMode() )
		{
			mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		}
		mapper.setDateFormat(new ISO8061DateFormatWithTZ());

	}

	public static class JavaTypesModule extends SimpleModule
	{
		public JavaTypesModule()
		{
			super("JavaTypesModule");
			addAbstractTypeMapping(Map.class, HashMap.class);
			addAbstractTypeMapping(Set.class, HashSet.class);
			addAbstractTypeMapping(List.class, ArrayList.class);
		}
	}
}
