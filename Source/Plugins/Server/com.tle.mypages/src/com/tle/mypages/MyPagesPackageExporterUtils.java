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

package com.tle.mypages;

import static com.tle.common.PathUtils.filePath;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ccil.cowan.tagsoup.AttributesImpl;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.dytech.edge.common.Constants;
import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.tle.annotation.Nullable;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.HtmlAttachment;
import com.tle.common.URLUtils;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.services.html.AbstractHtmlContentHandler;
import com.tle.mypages.parse.ConvertHtmlService;
import com.tle.mypages.parse.DefaultHrefCallback;
import com.tle.mypages.parse.conversion.HrefConversion;
import com.tle.mypages.service.MyPagesService;
import com.tle.mypages.web.attachments.MyPagesSummariser;
import com.tle.web.htmleditor.service.HtmlEditorService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.viewurl.ViewableResource;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@Bind
@Singleton
public class MyPagesPackageExporterUtils
{
	private static final String ITEM_REL_BASE = "../../";

	@Inject
	private MyPagesService myPagesService;
	@Inject
	private MyPagesSummariser myPagesSummariser;
	@Inject
	private ConvertHtmlService urlConverter;
	@Inject
	private InstitutionService institutionService;

	public Reader localiseHtmlForExport(SectionInfo info, Item item, HtmlAttachment page, FileHandle file)
	{
		// For every my pages page we need to "view" the XML (ie. add
		// html+body tags etc) and
		// convert all local item URLs into this-item-relative (instead of
		// institution relative)
		// and all non local URLs into absolute.
		//
		// Note that this is a special "viewing" since we don't want the
		// Base HREF and we don't want institution
		// relative URLs.

		final String thisItemBase = filePath("items", item.getItemId().toString());
		final String thisFileBase = filePath("file", item.getItemId().toString());
		final String thisPageBase = filePath(thisFileBase, HtmlEditorService.CONTENT_DIRECTORY);

		String newHtml = myPagesService.forFile(file, page.getFilename(), new Function<Reader, String>()
		{
			@Override
			@Nullable
			public String apply(@Nullable Reader input)
			{
				// convert all local item URLs into this-item-relative
				// (instead of institution relative)
				// and all non local URLs into absolute.
				return urlConverter.convert(input, false,
					new ItemsToLocalUrlConversion(thisPageBase, thisItemBase, thisFileBase));
			}
		});

		// view it, with no base HREF
		ViewableResource viewPage = myPagesSummariser.createMyPagesResourceFromHtml(info, page, newHtml);
		try
		{
			return new InputStreamReader(viewPage.getContentStream().getInputStream(), Constants.UTF8);
		}
		catch( IOException e )
		{
			throw Throwables.propagate(e);
		}
	}

	public Reader institutionaliseHtmlForImport(Item item, HtmlAttachment page, FileHandle file, String thisPackageBase)
	{
		// use thisItemBase since page folder has package path built into it
		final String thisFileBase = filePath("file", item.getItemId().toString());
		final String thisPageBase = filePath(thisFileBase, HtmlEditorService.CONTENT_DIRECTORY);

		String stripped = myPagesService.forFile(file, page.getFilename(), new Function<Reader, String>()
		{
			@Override
			@Nullable
			public String apply(@Nullable Reader input)
			{
				return urlConverter.modifyXml(input, new StrippingContentHandler());
			}
		});

		// convert all local item URLs into this-item-relative
		// (instead of institution relative)
		// and all non local URLs into absolute.
		final String newHtml = urlConverter.modifyXml(new StringReader(stripped), new DefaultHrefCallback(false, false,
			institutionService, new LocalToItemsUrlConversion(thisPageBase, thisPackageBase)));

		return new StringReader(newHtml);
	}

	public class ItemsToLocalUrlConversion implements HrefConversion
	{
		private final String pageBase;
		private final String itemBase;
		private final String fileBase;

		/**
		 * @param pageBase e.g. items/UUID/1/_mypages/PAGEUUID
		 * @param itemBase Base items URL for this item e.g. items/UUID/1
		 * @param fileBase Base file URL for this item e.g. file/UUID/1
		 */
		protected ItemsToLocalUrlConversion(String pageBase, String itemBase, String fileBase)
		{
			this.pageBase = pageBase;
			this.itemBase = itemBase;
			this.fileBase = fileBase;
		}

		@Override
		public String convert(String href, AttributesImpl attributes)
		{
			if( href.startsWith(pageBase) )
			{
				return filePath(ITEM_REL_BASE, HtmlEditorService.CONTENT_DIRECTORY, href.substring(pageBase.length()));
			}
			else if( href.startsWith(itemBase) )
			{
				// go past _mypages and the page UUID
				return filePath(ITEM_REL_BASE, href.substring(itemBase.length()));
			}
			else if( href.startsWith(fileBase) )
			{
				// go past _mypages and the page UUID
				return filePath(ITEM_REL_BASE, href.substring(itemBase.length()));
			}
			else if( !URLUtils.isAbsoluteUrl(href) )
			{
				// add the base href
				return institutionService.institutionalise(href);
			}
			return href;
		}
	}

	public class LocalToItemsUrlConversion implements HrefConversion
	{
		private final String pageBase;
		private final String packageBase;
		private final String localContentBase;

		/**
		 * @param pageBase e.g. items/UUID/1/_mypages/PAGEUUID
		 * @param packageBase e.g. items/UUID/1/package.zip
		 */
		protected LocalToItemsUrlConversion(String pageBase, String packageBase)
		{
			this.pageBase = pageBase;
			this.packageBase = packageBase;
			this.localContentBase = filePath(ITEM_REL_BASE, HtmlEditorService.CONTENT_DIRECTORY);
		}

		@Override
		public String convert(String href, AttributesImpl atts)
		{
			if( institutionService.isInstitutionUrl(href) )
			{
				// remove the base href
				return institutionService.removeInstitution(href);
			}
			else if( href.startsWith(localContentBase) )
			{
				return filePath(packageBase, HtmlEditorService.CONTENT_DIRECTORY,
					href.substring(localContentBase.length()));
			}
			else if( href.startsWith(ITEM_REL_BASE) )
			{
				// base of package
				return filePath(packageBase, href.substring(ITEM_REL_BASE.length()));
			}
			else if( !URLUtils.isAbsoluteUrl(href) )
			{
				return filePath(pageBase, href);
			}
			return href;
		}
	}

	private static class StrippingContentHandler extends AbstractHtmlContentHandler
	{
		private static final Set<String> IGNORE_DIRECT_CHILDREN_OF = new HashSet<String>(
			Arrays.asList(new String[]{"html", "head", "link", "meta", "base", "body", "title"}));
		private static final Set<String> IGNORE_TAGS = new HashSet<String>(
			Arrays.asList(new String[]{"html", "head", "link", "meta", "base", "body", "title"}));

		private final Stack<String> elementStack = new Stack<String>();

		public StrippingContentHandler()
		{
			super(new StringWriter());
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
		{
			if( !ignoreTag(localName) )
			{
				super.startElement(isOutputNamespaces() ? uri : "", localName, qName, atts);
			}
			elementStack.push(localName.toLowerCase());
		}

		@Override
		public void endElement(String uri, String localName, String name) throws SAXException
		{
			elementStack.pop();
			if( !ignoreTag(localName) )
			{
				super.endElement(uri, localName, name);
			}
		}

		@Override
		public void characters(char[] text, int start, int length) throws SAXException
		{
			// if we are ignoring the parent, then ignore the text (excluding
			// text of the first div)
			if( !ignoreTag("") )
			{
				super.characters(text, start, length);
			}
		}

		private boolean ignoreTag(String tagName)
		{
			final String lowerTagName = tagName.toLowerCase();
			if( (elementStack.size() > 0 && IGNORE_DIRECT_CHILDREN_OF.contains(elementStack.peek()))
				|| IGNORE_TAGS.contains(lowerTagName) )
			{
				return true;
			}
			return false;
		}
	}
}
