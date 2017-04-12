package com.tle.web.remoting.soap;

import javax.xml.ws.WebServiceContext;

import org.apache.cxf.jaxws.context.WebServiceContextImpl;

import com.google.inject.AbstractModule;

public class Module extends AbstractModule
{
	@Override
	protected void configure()
	{
		bind(WebServiceContext.class).to(WebServiceContextImpl.class);
	}

}
