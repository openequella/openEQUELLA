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

package com.tle.web.viewitem.filter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.ssi.SSIProcessor;

import com.tle.common.filesystem.handle.FileHandle;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.core.guice.Bind;
import com.tle.core.services.FileSystemService;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.viewitem.FilestoreContentFilter;
import com.tle.web.viewitem.FilestoreContentStream;
import com.tle.web.viewitem.WrappedFilestoreContentStream;

@Bind
@Singleton
public class SSIResourceFilter implements FilestoreContentFilter
{
	@Inject
	private FileSystemService fileSystemService;

	@Override
	public FilestoreContentStream filter(FilestoreContentStream contentStream, HttpServletRequest request,
		HttpServletResponse response)
	{
		if( contentStream.getFilenameWithoutPath().toLowerCase().endsWith(".shtml") ) //$NON-NLS-1$
		{
			return new WrappedFilestoreContentStream(contentStream)
			{
				private byte[] ssiBytes;

				@Override
				public long getContentLength()
				{
					return getSsiBytes().length;
				}

				@Override
				public String getMimeType()
				{
					return "text/html"; //$NON-NLS-1$
				}

				@Override
				public long getLastModified()
				{
					return -1;
				}

				@Override
				public InputStream getInputStream() throws IOException
				{
					return new ByteArrayInputStream(getSsiBytes());
				}

				private byte[] getSsiBytes()
				{
					if( ssiBytes == null )
					{
						FileHandle fileHandle = innerFile.getFileHandle();
						SSIResolver resolver = new SSIResolver(fileSystemService, fileHandle,
							innerFile.getFileDirectoryPath());
						int debug = 0;
						SSIProcessor ssiProcessor = new SSIProcessor(resolver, debug, false);

						try( InputStream in = inner.getInputStream() )
						{
							BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, "UTF-8")); //$NON-NLS-1$
							Date lastModifiedDate = new Date(getLastModified());
							StringWriter writer = new StringWriter();
							PrintWriter printwriter = new PrintWriter(writer);
							ssiProcessor.process(bufferedReader, lastModifiedDate.getTime(), printwriter);
							printwriter.flush();
							String finishedProcessing = writer.toString();
							ssiBytes = finishedProcessing.getBytes("UTF-8"); //$NON-NLS-1$
						}
						catch( Exception e )
						{
							throw new SectionsRuntimeException(e);
						}
					}
					return ssiBytes;
				}

				@Override
				public File getDirectFile()
				{
					return null;
				}

			};
		}
		return contentStream;
	}

	@Override
	public boolean canView(Item item, IAttachment a)
	{
		return true;
	}
}
