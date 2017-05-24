/*
 * Created on Oct 25, 2005
 */
package com.tle.core.xstream;

import java.io.Reader;
import java.io.Writer;
import java.util.function.Function;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.mapper.MapperWrapper;

/**
 * @author Nicholas Read
 */
public interface XmlService
{
	String serialiseToXml(Object o);

	<T> T deserialiseFromXml(ClassLoader classLoader, String xml);

	<T> T deserialiseFromXml(ClassLoader classLoader, Reader reader);

	XStream createDefault(ClassLoader classLoader);

	void serialiseToWriter(Object o, Writer writer);
}
