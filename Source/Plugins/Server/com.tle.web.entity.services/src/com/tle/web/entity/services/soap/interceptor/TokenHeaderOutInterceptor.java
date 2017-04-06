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
import com.tle.core.user.CurrentUser;
import com.tle.core.user.UserState;

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
