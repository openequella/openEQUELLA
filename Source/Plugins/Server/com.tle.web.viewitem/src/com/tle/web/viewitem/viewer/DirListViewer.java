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

package com.tle.web.viewitem.viewer;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;

import javax.inject.Inject;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;

import com.tle.annotation.Nullable;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.common.Check;
import com.tle.common.URLUtils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.filesystem.FileEntry;
import com.tle.core.services.FileSystemService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.viewitem.section.AbstractParentViewItemSection;
import com.tle.web.viewitem.section.RootItemFileSection;
import com.tle.web.viewitem.service.ItemXsltService;
import com.tle.web.viewurl.ViewAuditEntry;
import com.tle.web.viewurl.ViewItemResource;
import com.tle.web.viewurl.ViewItemViewer;

@SuppressWarnings("nls")
public class DirListViewer extends AbstractPrototypeSection<Object> implements ViewItemViewer
{
	private static final Collection<String> RAW_VIEW_ITEM = Collections.singleton("RAW_VIEW_ITEM");
	private static final String LIST_NAME = "~";
	private static final String XML_NAME = "<XML>";
	private static final String ATTACHMENT_LIST_NAME = "^";

	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private ItemXsltService itemXsltService;
	@TreeLookup
	private RootItemFileSection rootSection;

	public boolean canView(SectionInfo info)
	{
		return true;
	}

	@Override
	public ViewAuditEntry getAuditEntry(SectionInfo info, ViewItemResource resource)
	{
		return null;
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		rootSection.addViewerMapping(com.tle.web.viewitem.section.PathMapper.Type.FILENAME, this, LIST_NAME,
			ATTACHMENT_LIST_NAME);
		rootSection.addViewerMapping(com.tle.web.viewitem.section.PathMapper.Type.FULL, this, XML_NAME);
	}

	@Override
	public Collection<String> ensureOnePrivilege()
	{
		return RAW_VIEW_ITEM;
	}

	@Override
	public SectionResult view(RenderContext info, ViewItemResource resource)
	{
		final FileHandle fileHandle = resource.getViewableItem().getFileHandle();
		final HttpServletResponse response = info.getResponse();
		final String filename = resource.getFilenameWithoutPath();

		try
		{
			if( filename.equals(XML_NAME) )
			{
				viewXml(info, response);
			}
			else if( filename.equals(LIST_NAME) )
			{
				viewDirList(resource, fileHandle, response);
			}
			else if( filename.equals(ATTACHMENT_LIST_NAME) )
			{
				viewAttachmentList(resource, fileHandle, response);
			}
		}
		catch( IOException e )
		{
			throw new SectionsRuntimeException(e);
		}

		info.setRendered();
		return null;
	}

	private void viewAttachmentList(ViewItemResource resource, FileHandle fileHandle, HttpServletResponse response)
		throws IOException
	{
		response.setContentType("text/plain");

		try (PrintWriter writer = new PrintWriter(new PrintStream(response.getOutputStream())))
		{
			final Collection<String> list = fileSystemService.grep(fileHandle, resource.getFileDirectoryPath(), "**");
			for( String file : list )
			{
				writer.println(file);
			}
		}
	}

	private void viewDirList(ViewItemResource resource, FileHandle fileHandle, HttpServletResponse response)
		throws IOException
	{
		final String dirPath = resource.getFileDirectoryPath();

		response.setContentType("text/html");

		try (PrintWriter writer = new PrintWriter(new PrintStream(response.getOutputStream())))
		{
			writer.print("<html><head></head><body><h1>");
			writer.print(CurrentLocale.get("viewitem.section.dirlistsection.tle"));
			writer.print("</h1><b>");
			writer.print(CurrentLocale.get("viewitem.section.dirlistsection.listing",
				StringEscapeUtils.escapeHtml(resource.getViewableItem().getItemdir() + dirPath)));
			writer.print("</b><ul id=\"listing\">\n");

			if( dirPath.length() == 0 )
			{
				String xml = StringEscapeUtils.escapeHtml(XML_NAME);
				writer.print("<li><a href=\"");
				writer.print(xml);
				writer.print("\">");
				writer.print(xml);
				writer.print("</a></li>\n");
			}

			FileEntry[] aszFileList = fileSystemService.enumerate(fileHandle, dirPath, null);
			if( !Check.isEmpty(aszFileList) )
			{
				for( FileEntry entry : aszFileList )
				{
					String szFile = entry.getName();
					szFile = URLUtils.urlEncode(szFile, false);
					String name = szFile;
					if( entry.isFolder() )
					{
						szFile += "/~";
					}
					writer.print("<li><a href=\"");
					writer.print(szFile);
					writer.print("\">");
					writer.print(name);
					writer.print("</a></li>");
				}
			}
			writer.print("</ul></body></html>");
		}
	}

	private void viewXml(RenderContext info, HttpServletResponse response) throws IOException
	{
		response.setContentType("text/xml");
		ServletOutputStream outputStream = response.getOutputStream();
		outputStream.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		outputStream.println(itemXsltService.getXmlForXslt(info, AbstractParentViewItemSection.getItemInfo(info))
			.toString());
		outputStream.flush();
		outputStream.close();
	}

	@Nullable
	@Override
	public IAttachment getAttachment(SectionInfo info, ViewItemResource resource)
	{
		return null;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "dir";
	}

	@Override
	public Class<Object> getModelClass()
	{
		return Object.class;
	}
}