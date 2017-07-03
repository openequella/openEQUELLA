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

package com.tle.mypages.workflow.operation;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.ccil.cowan.tagsoup.AttributesImpl;

import com.google.common.base.Throwables;
import com.google.common.io.CharStreams;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.HtmlAttachment;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.common.filesystem.FileEntry;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.core.guice.BindFactory;
import com.tle.core.item.operations.AbstractWorkflowOperation;
import com.tle.core.services.FileSystemService;
import com.tle.core.services.html.HrefCallback;
import com.tle.mypages.parse.ConvertHtmlService;
import com.tle.web.htmleditor.service.HtmlEditorService;

/**
 * @author aholland
 */
// Sonar maintains that 'Class cannot be instantiated and does not provide any
// static methods or fields', but methinks thats bunkum
public class UnusedContentCleanupOperation extends AbstractWorkflowOperation // NOSONAR
{
	private static final Logger LOGGR = Logger.getLogger(UnusedContentCleanupOperation.class);

	private final Collection<String> metadataHtmls;

	@Inject
	private ConvertHtmlService htmlService;

	@AssistedInject
	private UnusedContentCleanupOperation(@Assisted Collection<String> metadataHtmls)
	{
		this.metadataHtmls = metadataHtmls;
	}

	@AssistedInject
	private UnusedContentCleanupOperation()
	{
		this.metadataHtmls = null;
	}

	@Override
	public boolean execute()
	{
		// scan the HtmlAttachments file system and see if their UUID is
		// referred to in the HTML
		StagingFile staging = getStaging();
		if( staging != null )
		{
			if( metadataHtmls != null && metadataHtmls.size() > 0 )
			{
				try
				{
					scanHtml(metadataHtmls, staging, HtmlEditorService.CONTENT_DIRECTORY);
				}
				catch( IOException e )
				{
					Throwables.propagate(e);
				}
			}

			List<HtmlAttachment> pages = new UnmodifiableAttachments(getItem()).getList(AttachmentType.HTML);
			for( HtmlAttachment page : pages )
			{
				try
				{
					scanHtml(Collections.singletonList(getHtml(staging, page)), staging, page.getFolder());
				}
				catch( IOException e )
				{
					Throwables.propagate(e);
				}
			}
		}
		return false;
	}

	protected void scanHtml(Collection<String> htmls, StagingFile staging, String folder) throws IOException
	{
		ContentUrlDetectorCallback detector = new ContentUrlDetectorCallback(staging, folder, fileSystemService);
		for( String html : htmls )
		{
			htmlService.modifyXml(new StringReader(html), detector);
		}
		for( Entry<String, Boolean> entry : detector.getAllFiles().entrySet() )
		{
			if( !entry.getValue() )
			{
				// fileSystemService.removeFile(staging, entry.getKey());
				LOGGR.info("Would have deleted " + entry.getKey() + " if cleanup was working."); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}

	protected String getHtml(StagingFile staging, HtmlAttachment attachment)
	{
		try( InputStream htmlStream = fileSystemService.read(staging, attachment.getFilename()) )
		{
			StringWriter html = new StringWriter();
			CharStreams.copy(new InputStreamReader(htmlStream), html);
			return html.toString();
		}
		catch( IOException io )
		{
			throw new RuntimeException(io);
		}
	}

	protected static class ContentUrlDetectorCallback implements HrefCallback
	{
		private final Map<String, Boolean> allFiles;

		public ContentUrlDetectorCallback(final StagingFile staging, final String baseFolder,
			final FileSystemService fsys) throws IOException
		{
			this.allFiles = new HashMap<String, Boolean>();

			FileEntry file = fsys.enumerateTree(staging, baseFolder, null);
			addFiles(file.getFiles(), baseFolder + '/', true);

		}

		private void addFiles(List<FileEntry> files, String path, boolean foldersOnly)
		{
			for( FileEntry file : files )
			{
				if( !file.isFolder() && !foldersOnly )
				{
					allFiles.put(path + file.getName(), false);
				}
				else
				{
					addFiles(file.getFiles(), path + file.getName() + '/', false);
				}
			}
		}

		@Override
		public String hrefFound(String tag, String attribute, AttributesImpl atts)
		{
			for( Entry<String, Boolean> fileEntry : allFiles.entrySet() )
			{
				String filename = fileEntry.getKey();
				boolean referenced = fileEntry.getValue();
				if( !referenced )
				{
					// if a match on filename and tag, then keep
					if( attribute.contains(filename) )
					{
						fileEntry.setValue(true);
						return null;
					}
				}
			}
			return null;
		}

		@Override
		public String textFound(String text)
		{
			return text;
		}

		public Map<String, Boolean> getAllFiles()
		{
			return allFiles;
		}
	}

	@BindFactory
	public interface UnusedContentCleanupOperationFactory
	{
		UnusedContentCleanupOperation createWithContent(Collection<String> content);

		UnusedContentCleanupOperation create();
	}

}
