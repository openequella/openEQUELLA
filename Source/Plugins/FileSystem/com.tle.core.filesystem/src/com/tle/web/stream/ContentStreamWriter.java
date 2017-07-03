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

package com.tle.web.stream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.connector.ClientAbortException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.tle.annotation.Nullable;
import com.tle.core.guice.Bind;

@SuppressWarnings("nls")
@Bind
@Singleton
public class ContentStreamWriter
{
	private static final int RANGEBUFFER_SIZE = 16384;
	private static Pattern RANGE_PATTERN = Pattern.compile("^bytes=(\\d*)-(\\d*)$");
	private static final Log LOGGER = LogFactory.getLog(ContentStreamWriter.class);

	private static final long[] NOT_RANGE_REQUEST = new long[]{-1L, -1L,};
	private static final long[] INVALID_RANGE_REQUEST = new long[]{-2L, -2L,};

	@Inject(optional = true)
	@Named("files.useXSendfile")
	private boolean useXSendfile;

	public void outputStream(HttpServletRequest request, HttpServletResponse response, ContentStream contentStream)
	{
		outputStream(request, response, contentStream, null);
	}

	public void outputStream(HttpServletRequest request, HttpServletResponse response, ContentStream contentStream,
		@Nullable OutputStream outputStream)
	{
		try
		{
			if( !contentStream.exists() )
			{
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}

			if( checkModifiedSince(request, response, contentStream.getLastModified()) )
			{
				String disposition = contentStream.getContentDisposition();
				if( disposition != null )
				{
					response.setHeader("Content-Disposition",
						disposition + "; filename=\"" + contentStream.getFilenameWithoutPath() + "\"");
				}

				String cacheControl = contentStream.getCacheControl();
				if( cacheControl != null )
				{
					response.setHeader("Cache-Control", cacheControl);
				}

				response.setContentType(contentStream.getMimeType());
				final long contentLength = contentStream.getContentLength();
				response.setContentLengthLong(contentLength);
				response.setStatus(HttpServletResponse.SC_OK);

				if( request.getMethod().equals("HEAD") )
				{
					return;
				}

				if( contentStream.mustWrite() )
				{
					if( outputStream == null )
					{
						// Don't close the Tomcat stream
						final OutputStream out = response.getOutputStream();
						contentStream.write(out);
						out.flush();
					}
					else
					{
						try( OutputStream out = outputStream )
						{
							contentStream.write(out);
							out.flush();
						}
					}
				}
				else
				{
					// If we're using X-Sendfile, don't bother looking at
					// anything. We need to see in the future if there is a
					// spec emerging for specifying ranges too. Apparently
					// the standard Content-Range header is only supported
					// by some file servers.
					File directFile = contentStream.getDirectFile();
					if( useXSendfile && contentLength > 0 && directFile != null )
					{
						response.setHeader("X-Sendfile", directFile.getAbsolutePath());
						return;
					}

					// Check for a range request?
					final long[] range = getRange(request, contentStream);

					// Invalid range request? Setup headers and return
					// serving nothing.
					if( range == INVALID_RANGE_REQUEST )
					{
						response.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
						response.setHeader("Content-Range", "bytes */" + contentLength);
						response.setContentLength(-1);
						return;
					}

					// Valid range request. Modify headers for partial
					// content and continue to let the content be served.
					if( range != NOT_RANGE_REQUEST )
					{
						response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
						response.setContentLength((int) (range[1] - range[0]));
						response.setHeader("Content-Range", "bytes " + range[0] + "-" + (range[1] - 1) + "/"
							+ contentLength);
					}

					// Is Tomcat's SendFile support available?
					if( contentLength > 0
						&& Boolean.TRUE.equals(request.getAttribute("org.apache.tomcat.sendfile.support"))
						&& directFile != null )
					{
						final long start = range == NOT_RANGE_REQUEST ? 0L : range[0];
						final long end = range == NOT_RANGE_REQUEST ? contentLength : range[1];

						request.setAttribute("org.apache.tomcat.sendfile.filename", directFile.getCanonicalPath());
						request.setAttribute("org.apache.tomcat.sendfile.start", start);
						request.setAttribute("org.apache.tomcat.sendfile.end", end);
					}
					else
					{
						serveFromStream(response, contentStream, outputStream, range);
					}
				}
			}
		}
		catch( ClientAbortException abortAbort )
		{
			LOGGER.debug("Client aborted download");
		}
		catch( IOException e )
		{
			LOGGER.warn("IO Error during send", e);
		}
	}

	private long[] getRange(HttpServletRequest request, ContentStream contentStream)
	{
		final long contentLength = contentStream.getContentLength();
		if( contentLength < 0 )
		{
			return NOT_RANGE_REQUEST;
		}

		final String rangeHeader = request.getHeader("Range");
		if( rangeHeader == null )
		{
			return NOT_RANGE_REQUEST;
		}

		long[] range = parseRange(rangeHeader, contentLength);
		if( range == null )
		{
			return INVALID_RANGE_REQUEST;
		}

		return range;
	}

	private void serveFromStream(HttpServletResponse response, ContentStream contentStream,
		@Nullable OutputStream outputStream, long[] range) throws IOException
	{
		final OutputStream out = (outputStream == null ? response.getOutputStream() : outputStream);
		try( InputStream input = contentStream.getInputStream() )
		{
			if( range == NOT_RANGE_REQUEST )
			{
				// While we may be able to handle range requests when we don't
				// know the content length, it's only really going to get here
				// for dynamically generated content where we usually don't know
				// the content length anyway. Just copy it all out for the
				// understandability of the code.
				ByteStreams.copy(input, out);
				out.flush();
				return;
			}

			// We're here because it's a range request
			long startByte = range[0];
			long maxBytes = range[1] - startByte;

			if( input.skip(startByte) != startByte )
			{
				throw new IOException("Failed to skip to right part of file");
			}

			final byte[] rangeBuffer = new byte[RANGEBUFFER_SIZE];
			while( maxBytes > 0 )
			{
				int readBytes = input.read(rangeBuffer, 0, (int) Math.min(maxBytes, RANGEBUFFER_SIZE));
				if( readBytes == -1 )
				{
					break;
				}
				out.write(rangeBuffer, 0, readBytes);
				maxBytes -= readBytes;
			}
			out.flush();
		}
	}

	private long[] parseRange(String rangeHeader, long length)
	{
		Matcher matcher = RANGE_PATTERN.matcher(rangeHeader);
		if( !matcher.matches() )
		{
			return null;
		}

		long start;
		long end = length;
		String rangeStart = matcher.group(1);
		String rangeEnd = matcher.group(2);
		if( rangeStart.isEmpty() )
		{
			start = length - Long.parseLong(rangeEnd);
		}
		else
		{
			start = Long.parseLong(rangeStart);
			if( !rangeEnd.isEmpty() )
			{
				end = Long.parseLong(rangeEnd) + 1;
			}
		}

		if( start < 0 || start >= length || end > length || end <= start )
		{
			return null;
		}

		return new long[]{start, end};
	}

	public boolean checkModifiedSince(HttpServletRequest request, HttpServletResponse response, long lastModified)
	{
		long modifiedSince = request.getDateHeader("IF-MODIFIED-SINCE"); //$NON-NLS-1$
		boolean hasBeenModified = true;
		if( modifiedSince > 0 )
		{
			hasBeenModified = modifiedSince < (lastModified - (lastModified % 1000));
		}

		response.setDateHeader("Last-Modified", lastModified); //$NON-NLS-1$

		if( !hasBeenModified )
		{
			response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
		}

		return hasBeenModified;
	}
}
