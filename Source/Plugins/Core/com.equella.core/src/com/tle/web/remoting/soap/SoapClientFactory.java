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

package com.tle.web.remoting.soap;

import java.net.URL;

import javax.inject.Singleton;
import javax.xml.namespace.QName;

import org.apache.cxf.Bus;
import org.apache.cxf.aegis.databinding.AegisDatabinding;
import org.apache.cxf.bus.extension.ExtensionManagerBus;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import com.tle.core.guice.Bind;
import com.tle.web.remoting.soap.cxf.XFireCompatabilityConfiguration;

@Bind
@Singleton
public class SoapClientFactory
{

	public <T> T createSoapClient(Class<T> serviceClass, URL endpoint, String namespace)
	{
		ClientProxyFactoryBean factory = new ClientProxyFactoryBean();
		Bus bus = new ExtensionManagerBus(null, null, Bus.class.getClassLoader());
		factory.setBus(bus);
		factory.setServiceClass(serviceClass);
		factory.setServiceName(new QName(namespace, serviceClass.getSimpleName()));
		factory.setAddress(endpoint.toString());
		factory.getServiceFactory().getServiceConfigurations().add(0, new XFireCompatabilityConfiguration());
		factory.setDataBinding(new AegisDatabinding());
		@SuppressWarnings("unchecked")
		T soapClient = (T) factory.create();
		Client client = ClientProxy.getClient(soapClient);
		client.getRequestContext().put(Message.MAINTAIN_SESSION, true);
		HTTPClientPolicy policy = new HTTPClientPolicy();
		policy.setReceiveTimeout(600000);
		policy.setAllowChunking(false);
		HTTPConduit conduit = (HTTPConduit) client.getConduit();
		conduit.setClient(policy);
		return soapClient;
	}
}
