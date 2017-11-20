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

package com.tle.core.item.serializer;

import java.io.IOException;
import java.io.Writer;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

@SuppressWarnings("nls")
public class XMLStreamer
{
	private Writer output;
	private XMLStreamWriter xml;

	public XMLStreamer(Writer writer)
	{
		this.output = writer;
		try
		{
			xml = XMLOutputFactory.newInstance().createXMLStreamWriter(output);
		}
		catch( Exception ex )
		{
			throw new RuntimeException(ex);
		}
	}

	public void startElement(String elemName)
	{
		try
		{
			xml.writeStartElement(elemName);
		}
		catch( XMLStreamException ex )
		{
			throw new RuntimeException(ex);
		}
	}

	public void endElement()
	{
		try
		{
			xml.writeEndElement();
		}
		catch( XMLStreamException ex )
		{
			throw new RuntimeException(ex);
		}
	}

	public void addAttribute(String name, String value)
	{
		try
		{
			xml.writeAttribute(name, value);
		}
		catch( XMLStreamException ex )
		{
			throw new RuntimeException(ex);
		}
	}

	public void writeData(String data)
	{
		try
		{
			xml.writeCharacters(data);
		}
		catch( XMLStreamException ex )
		{
			throw new RuntimeException(ex);
		}
	}

	public void addAttribute(String name, int value)
	{
		addAttribute(name, Integer.toString(value));
	}

	public void writeRawXmlString(String rawXml)
	{
		try
		{
			xml.writeCharacters("");
			xml.flush();
			output.write(rawXml);
		}
		catch( XMLStreamException ex )
		{
			throw new RuntimeException(ex);
		}
		catch( IOException e )
		{
			throw new RuntimeException(e);
		}
	}

	public void finished()
	{
		try
		{
			xml.flush();
			xml.close();
		}
		catch( XMLStreamException ex )
		{
			throw new RuntimeException(ex);
		}
	}

}
