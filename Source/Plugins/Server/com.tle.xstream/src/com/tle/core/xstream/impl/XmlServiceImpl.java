package com.tle.core.xstream.impl;

import java.io.Reader;
import java.io.Writer;

import javax.inject.Singleton;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.util.ClassLoaderReference;
import com.thoughtworks.xstream.core.util.CompositeClassLoader;
import com.thoughtworks.xstream.io.xml.XppDriver;
import com.tle.core.guice.Bind;
import com.tle.core.xstream.XmlService;

/**
 * @author Nicholas Read
 */
@Bind(XmlService.class)
@Singleton
public final class XmlServiceImpl implements XmlService
{
	private final XStream xstream;

	public XmlServiceImpl()
	{
		xstream = new ExtXStream(null);
	}

	@Override
	public String serialiseToXml(Object o)
	{
		if( o == null )
		{
			return null;
		}
		else
		{
			return xstream.toXML(o);
		}
	}

	@Override
	public XStream createDefault(ClassLoader loader)
	{
		return new ExtXStream(loader);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T deserialiseFromXml(ClassLoader loader, String xml)
	{
		Thread currentThread = Thread.currentThread();
		ClassLoader oldLoader = currentThread.getContextClassLoader();
		try
		{
			currentThread.setContextClassLoader(loader);
			if( xml == null )
			{
				return null;
			}
			else
			{
				return (T) xstream.fromXML(xml);
			}
		}
		finally
		{
			currentThread.setContextClassLoader(oldLoader);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T deserialiseFromXml(ClassLoader loader, Reader reader)
	{
		Thread currentThread = Thread.currentThread();
		ClassLoader oldLoader = currentThread.getContextClassLoader();
		try
		{
			currentThread.setContextClassLoader(loader);
			return (T) xstream.fromXML(reader);
		}
		finally
		{
			currentThread.setContextClassLoader(oldLoader);
		}
	}

	@Override
	public void serialiseToWriter(Object o, Writer writer)
	{
		xstream.toXML(o, writer);
	}

	public static class ExtXStream extends XStream
	{
		public ExtXStream(ClassLoader loader)
		{
			super(null, new XppDriver(), loader != null ? loader : new ClassLoaderReference(new CompositeClassLoader()));
			autodetectAnnotations(true);
			registerConverter(new OldSingletonMapConverter(getMapper(), getReflectionProvider()));
		}

		@Override
		protected boolean useXStream11XmlFriendlyMapper()
		{
			return true;
		}
	}
}
