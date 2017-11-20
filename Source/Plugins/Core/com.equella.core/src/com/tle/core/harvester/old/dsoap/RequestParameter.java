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

package com.tle.core.harvester.old.dsoap;

import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Element;

import com.dytech.devlib.Base64;
import com.tle.common.Utils;

/**
 * @author gfrancis
 */
@SuppressWarnings("nls")
public class RequestParameter
{
	/**
	 * enumerate possible argument types
	 */
	public static final int RP_UNKNOWN = 0;
	public static final int RP_INT = 1;
	public static final int RP_STRING = 2;
	public static final int RP_BOOLEAN = 3;
	public static final int RP_INTARRAY = 4;
	public static final int RP_STRINGARRAY = 5;
	public static final int RP_COMPLEX = 6;
	public static final int RP_BYTES = 8;
	public static final int RP_ELEMENT = 9;
	public static final int RP_ELEMENTARRAY = 10;
	public static final int RP_FLOAT = 11;
	public static final int RP_LONG = 12;
	public static final int RP_SHORT = 13;
	public static final int RP_DOUBLE = 14;

	private String name = null;
	private Object value = null;
	private int type = RP_UNKNOWN;
	private String namespace = null;

	/**
	 * String array version
	 */
	public RequestParameter(String name, String[] value)
	{
		type = RP_STRINGARRAY;

		this.name = name;
		this.value = value;
	}

	/**
	 * String array version
	 */
	public RequestParameter(String name, int[] value)
	{
		type = RP_INTARRAY;

		if( value == null )
		{
			this.value = null;
		}
		else
		{

			Integer[] ints = new Integer[value.length];
			for( int i = 0; i < value.length; i++ )
			{
				ints[i] = Integer.valueOf(value[i]);
			}
			this.value = ints;
		}
		this.name = name;
	}

	/**
	 * Creates a new instance of RequestParameter
	 * 
	 * @param name the name of the parameter
	 * @param value the parameter value as a string
	 * @param type of this variable
	 */
	public RequestParameter(String name, String value, int type)
	{
		// encode string parameters
		if( type == RP_STRING )
		{
			value = Utils.ent(value);
		}

		this.name = name;
		this.value = value;
		this.type = type;
	}

	/**
	 * this constructor used if you want to specify a namespace
	 * 
	 * @param ns
	 */
	public RequestParameter(String name, String value, int type, String ns)
	{
		this(name, value, type);
		namespace = ns;
	}

	public RequestParameter(String name, boolean value)
	{
		this(name, "" + value, RP_BOOLEAN);
	}

	public RequestParameter(String name, String value)
	{
		this(name, value, RP_STRING);
	}

	public RequestParameter(String name, int value)
	{
		this(name, "" + value, RP_INT);
	}

	public RequestParameter(String name, float value)
	{
		this(name, Float.toString(value), RP_FLOAT);
	}

	public RequestParameter(String name, double value)
	{
		this(name, Double.toString(value), RP_DOUBLE);
	}

	public RequestParameter(String name, long value)
	{
		this(name, Long.toString(value), RP_LONG);
	}

	public RequestParameter(String name, short value)
	{
		this(name, Short.toString(value), RP_SHORT);
	}

	public RequestParameter(String name, byte[] bytes)
	{
		this.name = name;
		this.type = RP_BYTES;

		Base64 encoder = new Base64();
		value = encoder.encode(bytes);
	}

	public RequestParameter(String name, Element element)
	{
		this(name, element2String(element), RP_ELEMENT);
	}

	public RequestParameter(String name, Element[] elements)
	{
		this.name = name;
		this.value = elements;
		this.type = RP_ELEMENTARRAY;
	}

	@Override
	public String toString()
	{
		// We should really be using a SAX XML writer for this.
		StringBuilder requestString = new StringBuilder("<");
		requestString.append(name);

		if( namespace != null )
		{
			requestString.append(" xmlns=\"");
			requestString.append(namespace);
			requestString.append("\" ");
		}

		requestString.append(" xsi:type=\"");
		switch( type )
		{
			case RP_INT:
				requestString.append("xsd:int");
				break;
			case RP_FLOAT:
				requestString.append("xsd:float");
				break;
			case RP_LONG:
				requestString.append("xsd:long");
				break;
			case RP_SHORT:
				requestString.append("xsd:short");
				break;
			case RP_DOUBLE:
				requestString.append("xsd:double");
				break;
			case RP_STRING:
				requestString.append("xsd:string");
				break;
			case RP_BOOLEAN:
				requestString.append("xsd:boolean");
				break;
			case RP_BYTES:
				requestString.append("xsd:base64Binary");
				break;
			case RP_ELEMENT:
				requestString.append("soapenc:Element");
				break;
			case RP_STRINGARRAY:
			case RP_INTARRAY:
			case RP_ELEMENTARRAY:
				requestString.append("ns2:Array");
				break;

			default:
				// else RP_COMPLEX, RP_UNKNOWN ...
				break;
		}
		requestString.append("\"");

		if( value == null )
		{
			requestString.append(" xsi:null=\"1\"");
		}

		// Add the encoding namespace: ns2
		if( type == RP_STRINGARRAY || type == RP_ELEMENTARRAY || type == RP_INTARRAY )
		{
			requestString.append(" xmlns:ns2=\"http://schemas.xmlsoap.org/soap/encoding/\"");
		}

		// Add the apache namespace: ns3
		if( type == RP_ELEMENT || type == RP_ELEMENTARRAY )
		{
			requestString.append(" xmlns:ns3=\"http://xml.apache.org/xml-soap\"");
		}

		// Add the array type
		if( type == RP_STRINGARRAY )
		{
			String[] szArray = (String[]) value;
			requestString.append(" ns2:arrayType=\"xsd:string[" + szArray.length + "]\"");
		}
		else if( type == RP_INTARRAY )
		{
			Integer[] szArray = (Integer[]) value;
			int len = 0;
			if( szArray != null )
			{
				len = szArray.length;
			}
			requestString.append(" ns2:arrayType=\"xsd:int[" + len + "]\"");
		}
		else if( type == RP_ELEMENTARRAY )
		{
			Element[] szArray = (Element[]) value;
			requestString.append(" ns2:arrayType=\"ns3:Element[" + szArray.length + "]\"");
		}

		// Finish off the tag.
		requestString.append(">");

		// Add the values;
		if( value != null )
		{
			if( type == RP_STRINGARRAY || type == RP_ELEMENTARRAY || type == RP_INTARRAY )
			{
				Object[] szArray = (Object[]) value;
				for( int i = 0; i < szArray.length; i++ )
				{
					requestString.append("<item");
					if( szArray[i] == null )
					{
						requestString.append(" xsi:null=\"1\">");
					}
					else
					{
						requestString.append('>');
						if( type == RP_STRINGARRAY )
						{
							requestString.append(Utils.ent((String) szArray[i]));
						}
						if( type == RP_INTARRAY )
						{
							requestString.append(((Integer) szArray[i]).intValue());
						}
						else if( type == RP_ELEMENTARRAY )
						{
							requestString.append(element2String((Element) szArray[i]));
						}
					}
					requestString.append("</item>");
				}
			}
			else
			{
				requestString.append(value);
			}
		}

		requestString.append("</");
		requestString.append(name);
		requestString.append(">");

		return requestString.toString();
	}

	public int getType()
	{
		return type;
	}

	public Object getValue()
	{
		return value;
	}

	public String getName()
	{
		return name;
	}

	private static String element2String(Element element)
	{
		StringWriter result = new StringWriter();

		DOMSource source = new DOMSource(element);
		StreamResult destination = new StreamResult(result);

		TransformerFactory factory = TransformerFactory.newInstance();
		try
		{
			Transformer transformer = factory.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.transform(source, destination);
		}
		catch( TransformerException ex )
		{
			throw new RuntimeException("Error transforming element to xml string", ex);
		}

		return result.toString();
	}
}