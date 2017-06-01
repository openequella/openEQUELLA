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

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.helpers.HttpHeaderHelper;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.staxutils.DepthXMLStreamReader;
import org.apache.cxf.staxutils.StaxUtils;

import com.tle.core.guice.Bind;

/**
 * Injects a LegacyPythonClientXMLStreamReader so that parameters get renamed to
 * in0, in1 etc
 * 
 * @author Aaron
 */
@Bind
@Singleton
public class LegacyPythonClientInInterceptor extends AbstractSoapInterceptor
{
	public LegacyPythonClientInInterceptor()
	{
		super(Phase.RECEIVE);
	}

	@Override
	public void handleMessage(SoapMessage message) throws Fault
	{
		String mappedNamespace = message.getExchange().getService().getName().getNamespaceURI();
		InputStream in = message.getContent(InputStream.class);
		if( in != null )
		{
			// ripped from StaxInInterceptor
			String contentType = (String) message.get(Message.CONTENT_TYPE);
			if( contentType == null )
			{
				// if contentType is null, this is likely a an empty
				// post/put/delete/similar, lets see if it's
				// detectable at all
				Map<String, List<String>> m = CastUtils.cast((Map<?, ?>) message.get(Message.PROTOCOL_HEADERS));
				if( m != null )
				{
					List<String> contentLen = HttpHeaderHelper.getHeader(m, HttpHeaderHelper.CONTENT_LENGTH);
					List<String> contentTE = HttpHeaderHelper.getHeader(m, HttpHeaderHelper.CONTENT_TRANSFER_ENCODING);
					if( (StringUtils.isEmpty(contentLen) || "0".equals(contentLen.get(0)))
						&& StringUtils.isEmpty(contentTE) )
					{
						return;
					}
				}
			}

			// Inject our LegacyPythonHack
			XMLStreamReader reader = StaxUtils.createXMLStreamReader(in);
			message.setContent(XMLStreamReader.class, new LegacyPythonClientXMLStreamReader(reader, mappedNamespace));
		}
	}

	private static class LegacyPythonClientXMLStreamReader extends DepthXMLStreamReader
	{
		private int paramIndex;
		private int bodyDepth = 0;
		private int methodDepth = 0;
		private final String mappedNamespace;

		public LegacyPythonClientXMLStreamReader(XMLStreamReader reader, String mappedNamespace)
		{
			super(reader);
			this.mappedNamespace = mappedNamespace;
		}

		@Override
		public int next() throws XMLStreamException
		{
			int ret = super.next();

			/*
			 * It looks like this: (for old Python clients) <s:Body> <ns1:login
			 * xmlns:ns1="http://www.thelearningedge.com.au"> <ns1:username
			 * xsi:type="xsd:string">someuser</ns1:username> <ns1:password
			 * xsi:type="xsd:string">password</ns1:password> </ns1:login>
			 * </s:Body> So when we are inside the Body we know we are at the
			 * method name. When inside the method name we know we are looking
			 * at the parameters and we need to munge them.
			 */

			if( ret == START_ELEMENT )
			{
				if( super.getLocalName().equals("Body") )
				{
					bodyDepth = getDepth();
				}
				else if( bodyDepth != 0 && methodDepth == 0 )
				{
					methodDepth = getDepth();
				}
				// Param level
				else if( methodDepth != 0 && methodDepth + 1 == getDepth() )
				{
					paramIndex++;
				}
			}
			else if( ret == END_ELEMENT )
			{
				if( super.getLocalName().equals("Body") )
				{
					bodyDepth = 0;
				}
				else if( getDepth() == methodDepth )
				{
					methodDepth = 0;
					paramIndex = 0;
				}
			}
			return ret;
		}

		@Override
		public QName getName()
		{
			QName superName = super.getName();
			String ns = superName.getNamespaceURI();

			if( ns.equals("http://www.thelearningedge.com.au") )
			{
				ns = mappedNamespace;
			}

			if( methodDepth != 0 && methodDepth + 1 == getDepth() )
			{
				if( !superName.getLocalPart().matches("in[0-9]+") )
				{
					return new QName(ns, "in" + (paramIndex - 1));
				}
			}

			return superName;
		}

		@Override
		public String getLocalName()
		{
			if( paramIndex > 0 )
			{
				return "in" + (paramIndex - 1);
			}
			return super.getLocalName();
		}
	}
}