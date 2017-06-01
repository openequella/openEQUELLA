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

package com.tle.core.harvester.old.dsoap.sax;

import java.lang.reflect.Constructor;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import com.tle.core.harvester.old.dsoap.SoapCallException;

/**
 * @author adame
 */
public abstract class DefaultSoapHandler extends DefaultHandler
{

	private StringBuffer accumulator;
	private int depth;
	private boolean foundError;
	private String faultString;
	private boolean interpretExceptions;

	public void setInterpretExceptions(boolean b)
	{
		interpretExceptions = b;
	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#startDocument()
	 */
	@Override
	public void startDocument()
	{
		accumulator = new StringBuffer();
		foundError = false;
		depth = 0;
		hookStartDocument();
	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#characters(char[], int, int)
	 */
	@Override
	public void characters(char[] buffer, int start, int length)
	{
		accumulator.append(buffer, start, length);
	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#startElement(java.lang.String,
	 * java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	public void startElement(String namespaceURL, String localName, String qname, Attributes attributes)
	{
		accumulator.setLength(0);
		if( (depth == 2) && localName.equals("Fault") ) //$NON-NLS-1$
		{
			foundError = true;
		}
		else if( !foundError )
		{
			hookStartElement(namespaceURL, localName, qname, attributes);
		}
		++depth;
	}

	/*
	 * (non-Javadoc)
	 * @see org.xml.sax.ContentHandler#endElement(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public void endElement(String namespaceURL, String localName, String qname)
	{
		--depth;
		if( foundError && localName.equals("faultstring") ) //$NON-NLS-1$
		{
			faultString = getAcculumulator();
		}
		else if( !foundError )
		{
			hookEndElement(namespaceURL, localName, qname);
		}
	}

	protected int getDepth()
	{
		return depth;
	}

	protected String getAcculumulator()
	{
		return accumulator.toString();
	}

	/**
	 * Is there a fault, or was this call successful?
	 * 
	 * @return
	 */
	public boolean isSuccessful()
	{
		return !foundError;
	}

	/**
	 * Throws the appropriate exception if call failed.
	 * 
	 * @param soapResponseCode
	 * @throws SoapCallException
	 */
	public void throwException(int soapResponseCode) throws SoapCallException
	{
		if( foundError )
		{
			if( !interpretExceptions )
			{
				throw new SoapCallException(faultString);
			}
			else
			{
				int start = faultString.indexOf('{');
				int colon = faultString.indexOf(':', start);
				int end = faultString.indexOf('}', start);
				String exceptionClass = null;
				String message = faultString;

				// The fault should be divided into a class name and an error
				// message:
				if( start >= 0 && colon > start && end > colon )
				{
					exceptionClass = faultString.substring(start + 1, colon);
					message = faultString.substring(colon + 1, end);
				}

				// try to get an instance of the exception class
				// using reflection
				try
				{
					Constructor<?> con = Class.forName(exceptionClass).getConstructor(new Class[]{String.class});
					SoapCallException sce = new SoapCallException((Exception) con.newInstance(new Object[]{message}),
						soapResponseCode);
					throw sce;
				}
				catch( SoapCallException sce ) // NOSONAR want to catch all but
												// this one
				{
					throw sce;
				}
				// see Jira Defect TLE-1448 :
				// http://apps.dytech.com.au/jira/browse/TLE-1448
				catch( Exception e )
				{
					// could not create an instance of a class with the name
					// given
					SoapCallException sce = new SoapCallException(faultString, soapResponseCode);
					throw sce;
				}
			}
		}
	}

	/**
	 * Intended to be overridden.
	 */
	protected void hookStartDocument()
	{
		// Nothing to do here
	}

	/**
	 * Intended to be overridden.
	 * 
	 * @param namespaceURL
	 * @param localName
	 * @param qname
	 * @param attributes
	 */
	protected void hookStartElement(String namespaceURL, String localName, String qname, Attributes attributes)
	{
		// Nothing to do here
	}

	/**
	 * Intended to be overridden.
	 * 
	 * @param namespaceURL
	 * @param localName
	 * @param qname
	 */
	protected void hookEndElement(String namespaceURL, String localName, String qname)
	{
		// Nothing to do here
	}
}