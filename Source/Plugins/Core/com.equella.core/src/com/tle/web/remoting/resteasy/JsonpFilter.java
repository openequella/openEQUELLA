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

package com.tle.web.remoting.resteasy;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.inject.Singleton;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import com.google.common.base.Throwables;
import com.tle.core.guice.Bind;
import com.tle.web.dispatcher.FilterResult;
import com.tle.web.dispatcher.WebFilter;
import com.tle.web.dispatcher.WebFilterCallback;

@SuppressWarnings("nls")
@Bind
@Singleton
public class JsonpFilter implements WebFilter
{

	protected String jsonp = "jsonp";
	protected String[] jsonMimeTypes = new String[]{"application/json", "application/x-json", "text/json",
			"text/x-json"};

	protected boolean isJsonp(ServletRequest req)
	{
		if( req instanceof HttpServletRequest )
		{
			return req.getParameterMap().containsKey(jsonp);
		}
		else
		{
			return false;
		}
	}

	protected boolean isJson(ServletRequest req, ServletResponse resp)
	{
		String ctype = resp.getContentType();
		if( ctype == null || ctype.equals("") )
		{
			return false;
		}
		for( String jsonMimeType : jsonMimeTypes )
		{
			if( ctype.indexOf(jsonMimeType) >= 0 )
			{
				return true;
			}
		}
		return false;
	}

	protected String getCallback(ServletRequest req)
	{
		return req.getParameterValues(jsonp)[0];
	}

	@Override
	public FilterResult filterRequest(final HttpServletRequest req, HttpServletResponse resp) throws IOException,
		ServletException
	{
		if( isJsonp(req) )
		{

			final HttpServletResponseContentWrapper wrapper = new HttpServletResponseContentWrapper(resp)
			{
				@Override
				public byte[] wrap(byte[] content) throws UnsupportedEncodingException
				{
					String contentstr = new String(content, getCharacterEncoding());
					boolean isJson = isJson(req, super.getResponse());
					setContentType("text/javascript; charset=utf-8");
					return (getCallback(req) + "(" + (isJson ? contentstr : quote(contentstr)) + ");")
						.getBytes(getCharacterEncoding());
				}
			};

			wrapper.setCharacterEncoding("UTF-8");
			FilterResult result = new FilterResult(wrapper);
			result.setCallback(new WebFilterCallback()
			{
				@Override
				public void afterServlet(HttpServletRequest request, HttpServletResponse response)
				{
					try
					{
						wrapper.flushWrapper();
					}
					catch( IOException e )
					{
						throw Throwables.propagate(e);
					}
				}
			});
			return result;

		}
		return new FilterResult();
	}

	public void init(FilterConfig config)
	{
		String tempJsonp = config.getInitParameter("jsonp");
		String tempJsonMimeTypes = config.getInitParameter("json-mime-types");
		if( tempJsonp != null && !tempJsonp.equals("") )
		{
			this.jsonp = tempJsonp;
		}
		if( tempJsonMimeTypes != null )
		{
			if( tempJsonMimeTypes.equals("") )
			{
				this.jsonMimeTypes = new String[]{};
			}
			else
			{
				this.jsonMimeTypes = tempJsonMimeTypes.trim().split("\\s*,\\s*");
			}
		}
	}

	public void destroy()
	{
		// Nothing to do
	}

	/**
	 * copy from: org.json, org.json.JSONObject.quote(String string)
	 */
	private static String quote(String string)
	{
		if( string == null || string.length() == 0 )
		{
			return "\"\"";
		}

		char b;
		char c = 0;
		int i;
		int len = string.length();
		StringBuilder sb = new StringBuilder(len + 4);
		String t;

		sb.append('"');
		for( i = 0; i < len; i += 1 )
		{
			b = c;
			c = string.charAt(i);
			switch( c )
			{
				case '\\':
				case '"':
					sb.append('\\');
					sb.append(c);
					break;
				case '/':
					if( b == '<' )
					{
						sb.append('\\');
					}
					sb.append(c);
					break;
				case '\b':
					sb.append("\\b");
					break;
				case '\t':
					sb.append("\\t");
					break;
				case '\n':
					sb.append("\\n");
					break;
				case '\f':
					sb.append("\\f");
					break;
				case '\r':
					sb.append("\\r");
					break;
				default:
					if( c < ' ' || (c >= '\u0080' && c < '\u00a0') || (c >= '\u2000' && c < '\u2100') )
					{
						t = "000" + Integer.toHexString(c);
						sb.append("\\u" + t.substring(t.length() - 4));
					}
					else
					{
						sb.append(c);
					}
			}
		}
		sb.append('"');
		return sb.toString();
	}

	public static class HttpServletResponseContentWrapper extends HttpServletResponseWrapper
	{
		protected ByteArrayServletOutputStream buffer;
		protected PrintWriter bufferWriter;
		protected boolean committed = false;

		public HttpServletResponseContentWrapper(HttpServletResponse response)
		{
			super(response);
			buffer = new ByteArrayServletOutputStream();
		}

		public void flushWrapper() throws IOException
		{
			if( bufferWriter != null )
			{
				bufferWriter.close();
			}

			if( buffer != null )
			{
				buffer.close();

				byte[] content = wrap(buffer.toByteArray());
				getResponse().setContentLengthLong(content.length);
				getResponse().getOutputStream().write(content);
			}
			getResponse().flushBuffer();
			committed = true;
		}

		public byte[] wrap(byte[] content) throws IOException
		{
			return content;
		}

		@Override
		public ServletOutputStream getOutputStream() throws IOException
		{
			return buffer;
		}

		/**
		 * The default behaviour of this method is to return getWriter() on the
		 * wrapped response object.
		 */

		@Override
		public PrintWriter getWriter() throws IOException
		{
			if( bufferWriter == null )
			{
				bufferWriter = new PrintWriter(new OutputStreamWriter(buffer, this.getCharacterEncoding()));
			}
			return bufferWriter;
		}

		@Override
		public void setBufferSize(int size)
		{
			buffer.enlarge(size);
		}

		@Override
		public int getBufferSize()
		{
			return buffer.size();
		}

		@Override
		public void flushBuffer() throws IOException
		{
			// Nothing to do
		}

		@Override
		public boolean isCommitted()
		{
			return committed;
		}

		@Override
		public void reset()
		{
			getResponse().reset();
			buffer.reset();
		}

		@Override
		public void resetBuffer()
		{
			getResponse().resetBuffer();
			buffer.reset();
		}

	}

	public static class ByteArrayServletOutputStream extends ServletOutputStream
	{

		protected byte buf[];

		protected int count;

		public ByteArrayServletOutputStream()
		{
			this(32);
		}

		public ByteArrayServletOutputStream(int size)
		{
			if( size < 0 )
			{
				throw new IllegalArgumentException("Negative initial size: " + size);
			}
			buf = new byte[size];
		}

		public synchronized byte toByteArray()[]
		{
			return copyOf(buf, count);
		}

		public synchronized void reset()
		{
			count = 0;
		}

		public synchronized int size()
		{
			return count;
		}

		public void enlarge(int size)
		{
			if( size > buf.length )
			{
				buf = copyOf(buf, Math.max(buf.length << 1, size));
			}
		}

		@Override
		public synchronized void write(int b) throws IOException
		{
			int newcount = count + 1;
			enlarge(newcount);
			buf[count] = (byte) b;
			count = newcount;
		}

		/**
		 * copy from: jdk1.6, java.util.Arrays.copyOf(byte[] original, int
		 * newLength)
		 */
		private static byte[] copyOf(byte[] original, int newLength)
		{
			byte[] copy = new byte[newLength];
			System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));
			return copy;
		}

		@Override
		public boolean isReady()
		{
			return false;
		}

		@Override
		public void setWriteListener(WriteListener writeListener)
		{
			// nothing
		}
	}
}