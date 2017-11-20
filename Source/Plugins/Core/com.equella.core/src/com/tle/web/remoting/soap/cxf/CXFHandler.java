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

package com.tle.web.remoting.soap.cxf;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext.Scope;

import org.apache.cxf.Bus;
import org.apache.cxf.aegis.databinding.AegisDatabinding;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.bus.extension.ExtensionManagerBus;
import org.apache.cxf.frontend.ServerFactoryBean;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.helpers.MethodComparator;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.jaxws.context.WebServiceContextImpl;
import org.apache.cxf.jaxws.context.WrappedMessageContext;
import org.apache.cxf.logging.FaultListener;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageContentsList;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.Service;
import org.apache.cxf.service.ServiceImpl;
import org.apache.cxf.service.factory.ReflectionServiceFactoryBean;
import org.apache.cxf.service.invoker.AbstractInvoker;
import org.apache.cxf.service.invoker.MethodDispatcher;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.FaultInfo;
import org.apache.cxf.service.model.InterfaceInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.Extension.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dytech.edge.exceptions.QuietlyLoggable;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;

/**
 * TODO: should probably extends AbstractRemoteHandler
 * 
 * @author jolz
 */
@Bind
@Singleton
@SuppressWarnings("nls")
public class CXFHandler extends CXFNonSpringServlet
{
	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(CXFHandler.class.getName());

	private PluginTracker<Object> endpointTracker;
	private PluginTracker<AbstractPhaseInterceptor<? extends Message>> interceptorTracker;
	private PluginTracker<Object> endpointIntTracker;

	private final Set<String> registeredServices = Collections.synchronizedSet(new HashSet<String>());

	@Override
	protected void handleRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException
	{
		String pathInfo = request.getPathInfo();
		Extension extension = endpointTracker.getExtension(pathInfo);
		if( extension != null )
		{
			synchronized( extension )
			{
				if( !registeredServices.contains(pathInfo) )
				{
					createService(extension, pathInfo);
					registeredServices.add(pathInfo);
				}
			}
		}
		super.handleRequest(request, response);
	}

	@Override
	protected void loadBus(ServletConfig sc)
	{
		Thread currentThread = Thread.currentThread();
		ClassLoader oldLoader = currentThread.getContextClassLoader();
		try
		{
			currentThread.setContextClassLoader(getClass().getClassLoader());
			Bus bus = new ExtensionManagerBus();
			setBus(bus);
			List<Extension> exts = interceptorTracker.getExtensions();
			List<Interceptor<? extends Message>> inInterceptors = bus.getInInterceptors();
			for( Extension extension : exts )
			{
				Collection<Parameter> inParams = extension.getParameters("inBean");
				for( Parameter inParam : inParams )
				{
					inInterceptors.add(interceptorTracker.getBeanByParameter(extension, inParam));
				}
				Collection<Parameter> outParams = extension.getParameters("outBean");
				for( Parameter outParam : outParams )
				{
					bus.getOutInterceptors().add(interceptorTracker.getBeanByParameter(extension, outParam));
				}
			}
			inInterceptors.add(new WebServiceContextIntercept());
			bus.setProperty("org.apache.cxf.logging.FaultListener", new SoapFaultListener());
		}
		finally
		{
			currentThread.setContextClassLoader(oldLoader);
		}
	}

	private void createService(Extension extension, String pathInfo)
	{
		Bus bus = getBus();

		Parameter beanParam = extension.getParameter("bean");
		Parameter namespaceParam = extension.getParameter("serviceNamespace");
		if( beanParam != null )
		{
			// JaxWsServerFactoryBean factoryBean = new
			// JaxWsServerFactoryBean();
			ServerFactoryBean factoryBean = new ServerFactoryBean();
			ReflectionServiceFactoryBean serviceFactory = factoryBean.getServiceFactory();
			serviceFactory.getConfigurations().add(0, new XFireCompatabilityConfiguration());
			factoryBean.setBus(bus);
			Class<?> serviceInterface = endpointTracker.getClassForName(extension,
				extension.getParameter("serviceInterface").valueAsString());
			factoryBean.setServiceClass(serviceInterface);
			factoryBean.setServiceBean(endpointTracker.getBeanByExtension(extension));
			if( namespaceParam != null )
			{
				factoryBean.setServiceName(new QName(namespaceParam.valueAsString(), serviceInterface.getSimpleName()));
			}
			factoryBean.setAddress(pathInfo);
			serviceFactory.setDataBinding(new AegisDatabinding());
			factoryBean.create();
		}
		else
		{
			String serviceName = extension.getParameter("serviceName").valueAsString();
			String serviceNamespace = namespaceParam.valueAsString();
			List<Extension> matchingExtensions = new ArrayList<Extension>();
			Map<Method, Extension> methodMap = new HashMap<Method, Extension>();
			List<Extension> extensions = endpointIntTracker.getExtensions();
			for( Extension intExt : extensions )
			{
				Collection<Parameter> paths = intExt.getParameters("path");
				for( Parameter pathParam : paths )
				{
					String intPath = pathParam.valueAsString();
					if( intPath.equals(pathInfo) )
					{
						matchingExtensions.add(intExt);
						break;
					}
				}
			}

			// FIXME: use a bastardisation of
			// ServerFactoryBean/ExtendedReflection
			// and JaxWsServerFactoryBean/ExtendedJax

			ExtendedReflection reflection = new ExtendedReflection(serviceNamespace, serviceName, matchingExtensions,
				methodMap);
			// ExtendedJax reflection = new ExtendedJax(serviceNamespace,
			// serviceName,
			// matchingExtensions, methodMap);
			reflection.getConfigurations().add(0, new XFireCompatabilityConfiguration());
			// JaxWsServerFactoryBean factoryBean = new
			// JaxWsServerFactoryBean(reflection);
			ServerFactoryBean factoryBean = new ServerFactoryBean(reflection);
			factoryBean.setBus(bus);
			factoryBean.setInvoker(new MultiInvoker(methodMap));
			factoryBean.setAddress(pathInfo);
			reflection.setDataBinding(new AegisDatabinding());
			factoryBean.create();
		}
	}

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		endpointTracker = new PluginTracker<Object>(pluginService, "com.tle.web.remoting.soap", "endpoint", "path").setBeanKey("bean");
		interceptorTracker = new PluginTracker<AbstractPhaseInterceptor<? extends Message>>(pluginService, "com.tle.web.remoting.soap",
			"interceptor", null);
		endpointIntTracker = new PluginTracker<Object>(pluginService, "com.tle.web.remoting.soap", "endpoint-interface", null)
			.setBeanKey("bean");
	}

	public static class FakeConfig implements ServletConfig
	{
		private final ServletContext servletContext;

		public FakeConfig(ServletContext servletContext)
		{
			this.servletContext = servletContext;
		}

		@Override
		public String getInitParameter(String arg0)
		{
			return null;
		}

		@Override
		public Enumeration<String> getInitParameterNames()
		{
			return null;
		}

		@Override
		public ServletContext getServletContext()
		{
			return servletContext;
		}

		@Override
		public String getServletName()
		{
			return "CXF";
		}
	}

	public class ExtendedReflection extends ReflectionServiceFactoryBean
	{
		private final QName serviceName;
		private final QName endpointName;
		private final QName interfaceName;
		private final Map<Method, Extension> methodMap;
		private final List<Extension> extensions;

		public ExtendedReflection(String namespace, String serviceName, List<Extension> extensions,
			Map<Method, Extension> methodMap)
		{
			this.interfaceName = new QName(namespace, serviceName);
			this.serviceName = new QName(namespace, serviceName + "Service");
			this.endpointName = new QName(namespace, serviceName + "Endpoint");
			this.extensions = extensions;
			this.methodMap = methodMap;
		}

		@Override
		public void setServiceClass(Class<?> serviceClass)
		{
			this.serviceClass = serviceClass;
		}

		@Override
		protected String getServiceName()
		{
			return serviceName.getLocalPart();
		}

		@Override
		public QName getInterfaceName()
		{
			return interfaceName;
		}

		@Override
		public QName getServiceQName()
		{
			return serviceName;
		}

		@Override
		public QName getEndpointName()
		{
			return endpointName;
		}

		@Override
		protected void initializeServiceModel()
		{
			ServiceInfo serviceInfo = new ServiceInfo();
			serviceInfo.setName(getServiceQName());
			serviceInfo.setTargetNamespace(getServiceQName().getNamespaceURI());
			InterfaceInfo info = new InterfaceInfo(serviceInfo, getInterfaceName());

			List<Method> allMethods = new ArrayList<Method>();
			for( Extension ext : extensions )
			{
				Class<?> clazz = endpointIntTracker.getClassForName(ext, ext.getParameter("serviceInterface")
					.valueAsString());

				Method[] methods = clazz.getMethods();
				for( Method method : methods )
				{
					methodMap.put(method, ext);
					allMethods.add(method);
				}
			}
			Collections.sort(allMethods, new MethodComparator());
			for( Method method : allMethods )
			{
				createOperation(serviceInfo, info, method);
			}
			setService(new ServiceImpl(serviceInfo));
			setPopulateFromClass(true);
			initializeDataBindings();

			boolean isWrapped = isWrapped() || hasWrappedMethods(serviceInfo.getInterface());
			if( isWrapped )
			{
				initializeWrappedSchema(serviceInfo);
			}
			for( OperationInfo opInfo : serviceInfo.getInterface().getOperations() )
			{
				Method m = (Method) opInfo.getProperty(METHOD);

				if( !isWrapped(m) && !isRPC(m) && opInfo.getInput() != null )
				{
					createBareMessage(serviceInfo, opInfo, false);
				}

				if( !isWrapped(m) && !isRPC(m) && opInfo.getOutput() != null )
				{
					createBareMessage(serviceInfo, opInfo, true);
				}

				if( opInfo.hasFaults() )
				{
					// check to make sure the faults are elements
					for( FaultInfo fault : opInfo.getFaults() )
					{
						QName qn = (QName) fault.getProperty("elementName");
						MessagePartInfo part = fault.getMessagePart(0);
						if( !part.isElement() )
						{
							part.setElement(true);
							part.setElementQName(qn);
							checkForElement(serviceInfo, part);
						}
					}
				}
			}
		}
	}

	// public class ExtendedJax extends JaxWsServiceFactoryBean
	// {
	//
	// }

	public class MultiInvoker extends AbstractInvoker
	{
		private final Map<Method, Extension> methodMap;

		public MultiInvoker(Map<Method, Extension> methodMap)
		{
			this.methodMap = methodMap;
		}

		@Override
		public Object getServiceObject(Exchange context)
		{
			return null;
		}

		@Override
		public Object invoke(Exchange exchange, Object o)
		{
			BindingOperationInfo bop = exchange.get(BindingOperationInfo.class);
			MethodDispatcher md = (MethodDispatcher) exchange.get(Service.class).get(MethodDispatcher.class.getName());
			Method m = md.getMethod(bop);
			List<Object> params = null;
			if( o instanceof List )
			{
				params = CastUtils.cast((List<?>) o);
			}
			else if( o != null )
			{
				params = new MessageContentsList(o);
			}
			Object serviceObject = endpointIntTracker.getBeanByExtension(methodMap.get(m));
			return invoke(exchange, serviceObject, m, params);
		}

	}

	public static class WebServiceContextIntercept extends AbstractSoapInterceptor
	{

		public WebServiceContextIntercept()
		{
			super(Phase.USER_PROTOCOL);
		}

		@Override
		public void handleMessage(SoapMessage message) throws Fault
		{
			WrappedMessageContext ctx = new WrappedMessageContext(message, Scope.APPLICATION);
			WebServiceContextImpl.setMessageContext(ctx);
		}
	}

	public class SoapFaultListener implements FaultListener
	{
		@Override
		public boolean faultOccurred(Exception exception, String description, Message message)
		{
			Throwable ex = exception;
			if( exception instanceof Fault )
			{
				ex = exception.getCause();
			}

			if( ex instanceof QuietlyLoggable )
			{
				final QuietlyLoggable ql = (QuietlyLoggable) ex;
				if( ql.isSilent() )
				{
					//Nada
				}
				else if( !ql.isShowStackTrace() )
				{
					if( ql.isWarnOnly() )
					{
						LOGGER.warn(description + ": " + ex.getMessage());
					}
					else
					{
						LOGGER.error(description + ": " + ex.getMessage());
					}
				}
				else
				{
					if( ql.isWarnOnly() )
					{
						LOGGER.warn(description, ex);
					}
					else
					{
						LOGGER.error(description, ex);
					}
				}
			}
			else if( ex instanceof IllegalArgumentException )
			{
				LOGGER.warn(ex.getMessage());
				//				HttpServletResponse response = (HttpServletResponse) message.getExchange().getInMessage()
				//					.get(AbstractHTTPDestination.HTTP_RESPONSE);
				//				if( response != null )
				//				{
				//					response.setStatus(400);
				//				}
			}
			else
			{
				LOGGER.error(description, ex);
			}
			return false;
		}
	}
}
