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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import com.google.common.base.Throwables;
import com.google.common.io.Closeables;
import com.tle.common.Pair;
import com.tle.core.harvester.old.dsoap.sax.DefaultSoapHandler;
import com.tle.core.harvester.old.dsoap.sax.ElementArrayResultSoapHandler;
import com.tle.core.harvester.old.dsoap.sax.ElementResultSoapHandler;
import com.tle.core.harvester.old.dsoap.sax.IntArrayResultSoapHandler;
import com.tle.core.harvester.old.dsoap.sax.StringArrayArrayResultSoapHandler;
import com.tle.core.harvester.old.dsoap.sax.StringArrayResultSoapHandler;
import com.tle.core.harvester.old.dsoap.sax.StringResultSoapHandler;

/**
 * Encapsulates a call to Soap server
 * 
 * @author gfrancis
 */
@SuppressWarnings("nls")
public class SoapCall
{
	private static final SAXParserFactory factory;

	protected String host = "";
	protected int port = 0;
	protected boolean isSecure = false;
	protected String endpoint = "";
	protected String methodName = null;
	protected List<RequestParameter> parameters = new ArrayList<RequestParameter>();
	protected String soapAction = "";
	protected String reqNamespace = "http://www.dytech.com.au/";
	protected String envelopeTag = "SOAP-ENV";
	/**
	 * Default is overwritten by SHEX, MEX
	 */
	protected String envelopeTagValue = "http://schemas.xmlsoap.org/soap/envelope/";
	protected boolean interpretExceptions = true;
	private boolean dotNetCompatability = false;
	private static final String DEFAULT_CONTENT_TYPE = "text/xml"; // not for
																	// everyone
																	// ...
	static
	{
		factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
	}

	/**
	 * SoapCall - Encapsulates a call to Soap server
	 * 
	 * @param host the IP address of the host
	 * @param port port for server
	 * @param url relative url of the Soap agent
	 */
	public SoapCall(String host, int port, String endpoint, String methodName)
	{
		this.host = host;
		this.port = port;
		this.endpoint = endpoint;
		this.methodName = methodName;
	}

	public SoapCall(String host, int port, String endpoint, String methodName, boolean isSecure)
	{
		this(host, port, endpoint, methodName);
		this.isSecure = isSecure;
	}

	/**
	 * @param url relative url of the Soap agent
	 */
	public SoapCall(String host, int port, String endpoint, String methodName, String reqNamespace)
	{
		this(host, port, endpoint, methodName);
		this.reqNamespace = reqNamespace;
	}

	/**
	 * @param url relative url of the Soap agent
	 */
	public SoapCall(String host, int port, String endpoint, String methodName, String reqNamespace, boolean isSecure)
	{
		this(host, port, endpoint, methodName, reqNamespace);
		this.isSecure = isSecure;
	}

	/**
	 * @param dotNetCompatability The dotNetCompatability to set.
	 */
	public void setDotNetCompatability(boolean dotNetCompatability)
	{
		this.dotNetCompatability = dotNetCompatability;
	}

	/**
	 * @return Returns the dotNetCompatability.
	 */
	public boolean isDotNetCompatability()
	{
		return dotNetCompatability;
	}

	public void setInterpretExceptions(boolean b)
	{
		interpretExceptions = b;
	}

	public void setSOAPAction(String soapAction)
	{
		this.soapAction = soapAction;
	}

	public void setEnvelopeTag(String envelopeTag)
	{
		this.envelopeTag = envelopeTag;
	}

	public void setEnvelopeTagValue(String envelopeTagValue)
	{
		this.envelopeTagValue = envelopeTagValue;
	}

	/**
	 * Specifies an argument to the soap method
	 */
	public void addParameter(RequestParameter parameter)
	{
		parameters.add(parameter);
	}

	/**
	 * Specifies an argument to the soap method
	 */
	public void setParameter(int index, RequestParameter parameter)
	{
		parameters.set(index, parameter);
	}

	public boolean isSecure()
	{
		return isSecure;
	}

	public void setSecure(boolean isSecure)
	{
		this.isSecure = isSecure;
	}

	public String getMethodName()
	{
		return methodName;
	}

	/**
	 * @param message
	 * @return
	 * @throws IOException
	 */
	private HttpURLConnection post(String message, String contentType) throws IOException
	{
		// Send data
		String urlString = isSecure ? "https" : "http";
		urlString += "://" + host;
		if( port > 0 )
		{
			urlString += ":" + port;
		}
		urlString += endpoint;
		URL url = new URL(urlString);

		HttpURLConnection conn = null;
		if( isSecure )
		{
			conn = (HttpsURLConnection) url.openConnection();
		}
		else
		{
			conn = (HttpURLConnection) url.openConnection();
		}

		conn.setRequestMethod("POST");
		conn.setDoInput(true);
		conn.setDoOutput(true);

		byte[] outmes = message.getBytes("UTF-8");
		conn.setRequestProperty("Content-Length", "" + outmes.length);
		String extendedContentTypeProperty = contentType;
		// LORAX (textXml) has the request property SOAPAction, being
		// "soapAction val - in quotes"
		// SHEX (application/soap+xml) has the action="soapAction val - in
		// quotes" as an extension to the contentType property, and no
		// SOAPAction
		// property as such
		if( DEFAULT_CONTENT_TYPE.equals(contentType) )
		{
			conn.setRequestProperty("SOAPAction", soapAction);
		}
		else
		{
			extendedContentTypeProperty += "; action=\"" + soapAction + '"';
		}
		conn.setRequestProperty("Content-Type", extendedContentTypeProperty);
		// Our friendly Shex server says: Cannot process the message because the
		// content type 'text/xml' was not the expected type
		// 'application/soap+xml; charset=utf-8'
		setConnectionProperties(conn);

		OutputStream out = null;
		try
		{
			out = conn.getOutputStream();
			out.write(outmes);
			out.flush();
		}
		finally
		{
			Closeables.close(out, true); // Quietly
		}

		return conn;
	}

	protected void setConnectionProperties(HttpURLConnection conn)
	{
		// Nothing to do by default
	}

	public int callWithSAX(DefaultHandler handler) throws SoapCallException
	{
		return callWithSAX(handler, DEFAULT_CONTENT_TYPE);
	}

	public int callWithSAX(DefaultHandler handler, String contentType) throws SoapCallException
	{
		// Read out soap packet.
		InputStream inp = null;
		try
		{
			HttpURLConnection conn = post(formulateRequestEnvelope(), contentType);
			int error = 0;

			// Get the response
			try
			{
				inp = new BufferedInputStream(conn.getInputStream());
			}
			catch( IOException ioe )
			{
				error = conn.getResponseCode();
			}

			Pair<String, Boolean> examinedResponse = examineSOAPResponse(conn, inp, handler, contentType);
			// First element is error message info, second is successful/failed
			// parse as xml
			if( !examinedResponse.getSecond().booleanValue() )
			{
				// If the Content-Type of the response is html (or any other) or
				// if the xml parse failed, buffer up the response (if there is
				// one) and throw an exception
				StringBuilder sbuf = new StringBuilder();
				if( inp != null )
				{
					try( BufferedReader buf = new BufferedReader(new InputStreamReader(inp, "UTF-8")) )
					{
						String line;
						while( (line = buf.readLine()) != null )
						{
							sbuf.append(line);
							sbuf.append('\n');
						}
					}
				}
				// We may have already collected some error info when attempting
				// to parse, so we're accumulating a message string rather than
				// creating from scratch
				String responseMessage = examinedResponse.getFirst();
				try
				{
					responseMessage += conn.getResponseMessage();
				}
				catch( IOException ignoreInnerIOE )
				{
					// otherwise ignore - probably 'stream closed' or something
					// equally unilluminating
					responseMessage += "<no response message>";
				}
				throw new SoapCallException(responseMessage, error, sbuf.toString());
			}

			return error;
		}
		catch( Exception e )
		{
			throw new SoapCallException(e);
		}
		finally
		{
			try
			{
				Closeables.close(inp, true);
			}
			catch( IOException e )
			{
				throw Throwables.propagate(e);
			}
		}
	}

	/**
	 * @param handler
	 * @throws SoapCallException
	 */
	public void callWithSoapSAX(DefaultSoapHandler handler) throws SoapCallException
	{
		callWithSoapSAX(handler, DEFAULT_CONTENT_TYPE);
	}

	/**
	 * @param handler
	 * @throws SoapCallException
	 */
	public void callWithSoapSAX(DefaultSoapHandler handler, String contentType) throws SoapCallException
	{
		handler.setInterpretExceptions(interpretExceptions);
		int code = this.callWithSAX(handler, contentType);
		if( !handler.isSuccessful() )
		{
			handler.throwException(code);
		}
	}

	/**
	 * Use if you know your Soap request returns a boolean
	 */
	public boolean getBooleanResult() throws SoapCallException
	{
		return Boolean.valueOf(getStringResult()).booleanValue();
	}

	/**
	 * Use if you know your Soap request returns an int
	 */
	public int getIntResult() throws SoapCallException
	{
		return Integer.parseInt(getStringResult());
	}

	public float getFloatResult() throws SoapCallException
	{
		return Float.parseFloat(getStringResult());
	}

	public double getDoubleResult() throws SoapCallException
	{
		return Double.parseDouble(getStringResult());
	}

	public short getShortResult() throws SoapCallException
	{
		return Short.parseShort(getStringResult());
	}

	public long getLongResult() throws SoapCallException
	{
		return Long.parseLong(getStringResult());
	}

	/**
	 * Use if you know your Soap request returns an array of ints
	 */
	public int[] getIntArrayResult() throws SoapCallException
	{
		IntArrayResultSoapHandler handler = new IntArrayResultSoapHandler();
		callWithSoapSAX(handler);
		return handler.getIntArrayResult();
	}

	/**
	 * Use if you know your Soap request returns a String
	 */
	public String getStringResult() throws SoapCallException
	{
		StringResultSoapHandler handler = new StringResultSoapHandler();
		callWithSoapSAX(handler);
		return handler.getStringResult();
	}

	/**
	 * Use if you know your Soap request returns an array of String's
	 */
	public String[] getStringArrayResult() throws SoapCallException
	{
		StringArrayResultSoapHandler handler = new StringArrayResultSoapHandler();
		callWithSoapSAX(handler);
		return handler.getStringArrayResult();
	}

	/**
	 * Use if you know your Soap request returns an array of String's
	 */
	public String[][] getStringArrayArrayResult() throws SoapCallException
	{
		StringArrayArrayResultSoapHandler handler = new StringArrayArrayResultSoapHandler();
		callWithSoapSAX(handler);
		return handler.getStringArrayArrayResult();
	}

	public Element getElementResult() throws SoapCallException
	{
		ElementResultSoapHandler handler = new ElementResultSoapHandler();
		callWithSoapSAX(handler);
		return handler.getElementResult();
	}

	public Element[] getElementArrayResult() throws SoapCallException
	{
		ElementArrayResultSoapHandler handler = new ElementArrayResultSoapHandler();
		callWithSoapSAX(handler);
		return handler.getElementArrayResult();
	}

	public String formulateRequestEnvelope()
	{
		StringBuilder envelope = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		envelope.append('<');
		envelope.append(envelopeTag);
		envelope.append(":Envelope ");
		envelope.append(envelopeTag);
		envelope.append(":encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" ");
		envelope.append("xmlns:");
		envelope.append(envelopeTag);
		envelope.append("=\"" + envelopeTagValue + "\" ");
		envelope.append("xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" ");
		envelope.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
		envelope.append("xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\"");
		envelope.append("><");
		envelope.append(envelopeTag);
		envelope.append(":Body><");
		if( reqNamespace == null )
		{
			envelope.append(methodName);
		}
		else if( !isDotNetCompatability() )
		{
			envelope.append("ns1:");
			envelope.append(methodName);
			envelope.append(" xmlns:ns1=\"");
			envelope.append(reqNamespace);
			envelope.append('"');
		}
		else
		{
			envelope.append(methodName);
			envelope.append(" xmlns=\"");
			envelope.append(reqNamespace);
			envelope.append('"');
		}
		envelope.append(">");

		// iterate over request params
		for( Iterator<RequestParameter> iter = parameters.iterator(); iter.hasNext(); )
		{
			RequestParameter parameter = iter.next();
			envelope.append(parameter.toString());
		}

		envelope.append("</");
		if( reqNamespace != null && !isDotNetCompatability() )
		{
			envelope.append("ns1:");
		}
		envelope.append(methodName);
		envelope.append("></");
		envelope.append(envelopeTag);
		envelope.append(":Body></");
		envelope.append(envelopeTag);
		envelope.append(":Envelope>");

		return envelope.toString();
	}

	/**
	 * Convenience method to examine and parse SOAP response. Caller handles
	 * whatever exception may be thrown. Note that the Content-Type from the
	 * SHEX server will be application/soap+xml, whereas the Content-Type from
	 * MEX will be "text/xml". Accordingly we seek to parse either.
	 * 
	 * @param conn
	 * @param inp
	 * @param handler
	 * @param contentType
	 * @return Pair of String, Boolean. The String is an accumulated message
	 *         (which may be blank) and the boolean is true (parsed), false
	 *         (failed parse)
	 * @throws Exception
	 */
	private Pair<String, Boolean> examineSOAPResponse(HttpURLConnection conn, InputStream inp, DefaultHandler handler,
		String contentType) throws Exception
	{
		String responseMessage = "";
		boolean parsedResponse = false;
		String responseContentType = conn.getContentType();
		// which ever xml variant the contentType is, "text/xml",
		// "application/soap+xml"
		if( responseContentType != null
			&& (responseContentType.toLowerCase().startsWith(contentType) || responseContentType.toLowerCase()
				.contains(DEFAULT_CONTENT_TYPE)) )
		{
			SAXParser saxParser = factory.newSAXParser();
			if( inp != null )
			{
				InputSource inpsrc = new InputSource(inp);
				inpsrc.setEncoding("UTF-8");
				try
				{
					saxParser.parse(inpsrc, handler);
					parsedResponse = true;
				}
				catch( Exception parsee )
				{
					// sigh - leave the boolean false and let the caller try as
					// plain text, but add what error info we have
					responseMessage += "saxParserException:\n";
					responseMessage += parsee.getLocalizedMessage();
					responseMessage += '\n';
				}
			}
		}
		return new Pair<String, Boolean>(responseMessage, parsedResponse);
	}
}
