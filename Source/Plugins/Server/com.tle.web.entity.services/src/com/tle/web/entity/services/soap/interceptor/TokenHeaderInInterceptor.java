package com.tle.web.entity.services.soap.interceptor;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.headers.Header;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import com.dytech.devlib.PropBagEx;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.services.user.UserService;

@SuppressWarnings("nls")
@Bind
@Singleton
public class TokenHeaderInInterceptor extends AbstractSoapInterceptor
{
	private static final Logger LOGGER = Logger.getLogger(TokenHeaderInInterceptor.class);

	@Inject
	private UserService userService;

	public TokenHeaderInInterceptor()
	{
		super(Phase.POST_PROTOCOL);
	}

	@Override
	public void handleMessage(SoapMessage message) throws Fault
	{
		Header header = message.getHeader(new QName("equella"));
		if( header != null )
		{
			Element e = (Element) header.getObject();
			PropBagEx equella = new PropBagEx(e);
			String token = equella.getNode("token");
			if( !Check.isEmpty(token) )
			{
				HttpServletRequest request = (HttpServletRequest) message.get(AbstractHTTPDestination.HTTP_REQUEST);
				try
				{
					userService.loginWithToken(token, userService.getWebAuthenticationDetails(request), true);
				}
				catch( RuntimeException ex )
				{
					LOGGER.error("Error initialising session with SOAP header token '" + token + "' for URL "
						+ request.getRequestURL().toString());
					throw ex;
				}
			}
		}
	}
}
