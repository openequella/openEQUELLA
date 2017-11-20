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

package com.tle.web.entity.services.soap.interceptor;

import javax.inject.Singleton;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.databinding.stax.StaxDataBinding;
import org.apache.cxf.databinding.stax.XMLStreamWriterCallback;
import org.apache.cxf.headers.Header;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;

import com.tle.core.guice.Bind;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.common.usermanagement.user.UserState;

@Bind
@Singleton
public class TokenHeaderOutInterceptor extends AbstractSoapInterceptor
{
	public TokenHeaderOutInterceptor()
	{
		super(Phase.PRE_PROTOCOL);
	}

	@SuppressWarnings("nls")
	@Override
	public void handleMessage(SoapMessage message) throws Fault
	{
		final UserState us = CurrentUser.getUserState();
		XMLStreamWriterCallback obj = new XMLStreamWriterCallback()
		{
			@Override
			public void write(XMLStreamWriter writer) throws Fault, XMLStreamException
			{
				writer.writeEmptyElement("equella");
				writer.writeAttribute("session", us.getSessionID());
				writer.writeAttribute("id", us.getUserBean().getUniqueID());
				writer.writeAttribute("username", us.getUserBean().getUsername());

			}
		};

		message.getHeaders().add(new Header(new QName("equella"), obj, new StaxDataBinding()));
	}

}
