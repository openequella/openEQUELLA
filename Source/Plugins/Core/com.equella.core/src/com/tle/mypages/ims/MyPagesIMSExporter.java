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

package com.tle.mypages.ims;

import static com.tle.common.PathUtils.filePath;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ccil.cowan.tagsoup.AttributesImpl;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.Constants;
import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.tle.annotation.Nullable;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.HtmlAttachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.common.Check;
import com.tle.common.URLUtils;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.core.filesystem.ItemFile;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.item.service.ItemFileService;
import com.tle.core.services.FileSystemService;
import com.tle.core.services.html.AbstractHtmlContentHandler;
import com.tle.core.util.ims.beans.IMSCustomData;
import com.tle.core.util.ims.beans.IMSMetadata;
import com.tle.core.util.ims.beans.IMSResource;
import com.tle.core.util.ims.extension.IMSAttachmentExporter;
import com.tle.core.util.ims.extension.IMSFileExporter;
import com.tle.mypages.MyPagesConstants;
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
public class MyPagesIMSExporter implements IMSFileExporter, IMSAttachmentExporter
{
	private static final String ITEM_REL_BASE = "../../";

	@Inject
	private FileSystemService fileSystem;
	@Inject
	private ItemFileService itemFileService;
	@Inject
	private MyPagesService myPagesService;
	@Inject
	private HtmlEditorService htmlEditorService;
	@Inject
	private MyPagesSummariser myPagesSummariser;
	@Inject
	private ConvertHtmlService urlConverter;
	@Inject
	private InstitutionService institutionService;

	@Override
	public void exportFiles(Object info, Item item, StagingFile imsRoot)
	{
		final ItemFile itemFile = itemFileService.getItemFile(item);
		final SectionInfo sinfo = (SectionInfo) info;

		final List<HtmlAttachment> pages = new UnmodifiableAttachments(item).getList(AttachmentType.HTML);

		if( pages.size() > 0 )
		{
			fileSystem.copy(itemFile, MyPagesConstants.MYPAGES_DIRECTORY, imsRoot, MyPagesConstants.MYPAGES_DIRECTORY);

			// Directly embedded content
			if( fileSystem.fileExists(itemFile, HtmlEditorService.CONTENT_DIRECTORY) )
			{
				fileSystem.copy(itemFile, HtmlEditorService.CONTENT_DIRECTORY, imsRoot,
					HtmlEditorService.CONTENT_DIRECTORY);
			}

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

			for( HtmlAttachment page : pages )
			{
				final String thisPageBase = filePath(thisFileBase, HtmlEditorService.CONTENT_DIRECTORY);

				String newHtml = myPagesService.forFile(imsRoot, page.getFilename(), new Function<Reader, String>()
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
				ViewableResource viewPage = myPagesSummariser.createMyPagesResourceFromHtml(sinfo, page, newHtml);
				try( Reader viewedRdr = new InputStreamReader(viewPage.getContentStream().getInputStream(),
					Constants.UTF8) )
				{
					// write it out again
					myPagesService.saveHtml(imsRoot, page.getFilename(), viewedRdr);
				}
				catch( Exception io )
				{
					throw Throwables.propagate(io);
				}
			}
		}

		// Export the CSS file
		htmlEditorService.exportStylesheet(imsRoot, MyPagesConstants.MYPAGES_DIRECTORY);
	}

	@Override
	public void importFiles(Item item, StagingFile staging, String packageExtractedFolder, String packageName,
		Collection<Attachment> createdAttachments)
	{
		// TODO: should probably be a preview URL base?? In which case we would
		// need
		// to ensure that a ChangePreviewUrls operation gets added to the
		// wizard state....

		final String thisFileBase = filePath("file", item.getItemId().toString());
		final String thisPackageBase = filePath(thisFileBase, packageName);
		final List<HtmlAttachment> pages = new UnmodifiableAttachments(
			Lists.<IAttachment>newArrayList(createdAttachments)).getList(AttachmentType.HTML);
		for( HtmlAttachment page : pages )
		{
			// use thisItemBase since page folder has package path built into it
			final String thisPageBase = filePath(thisFileBase, HtmlEditorService.CONTENT_DIRECTORY);

			final String htmlFilePath = filePath(packageExtractedFolder,
				page.getFilename().substring(page.getParentFolder().length()));

			final String newHtml = myPagesService.forFile(staging, htmlFilePath, new Function<Reader, String>()
			{
				@Override
				@Nullable
				public String apply(@Nullable Reader input)
				{
					String stripped = urlConverter.modifyXml(input, new StrippingContentHandler());

					// convert all local item URLs into this-item-relative
					// (instead of institution relative) and all non local URLs
					// into absolute.
					return urlConverter.modifyXml(new StringReader(stripped), new DefaultHrefCallback(false, false,
						institutionService, new LocalToItemsUrlConversion(thisPageBase, thisPackageBase)));
				}
			});

			myPagesService.saveHtml(staging, htmlFilePath, newHtml);
		}
	}

	@Override
	public boolean exportAttachment(Item item, IAttachment attachment, List<IMSResource> resources, FileHandle imsRoot)
	{
		if( attachment.getAttachmentType() == AttachmentType.HTML )
		{
			final HtmlAttachment html = (HtmlAttachment) attachment;
			final IMSMetadata data = new IMSMetadata();
			final IMSCustomData customData = new IMSCustomData();

			PropBagEx xml = new PropBagEx("<equella/>");
			xml.setNode("type", MyPagesConstants.MYPAGES_CONTENT_TYPE);
			PropBagEx htmlData = xml.aquireSubtree("htmldata");
			htmlData.setNode("description", html.getDescription());
			customData.setXml(xml);

			data.setData(customData);

			final IMSResource res = new IMSResource();
			res.setMetadata(data);
			res.setIdentifier(attachment.getUuid());
			res.setHref(URLUtils.urlEncode(html.getFilename()));
			resources.add(res);
			return true;
		}
		return false;
	}

	@Override
	public Attachment importAttachment(Item item, IMSResource resource, FileHandle root, String packageFolder)
	{
		IMSMetadata metadata = resource.getMetadata();
		if( metadata != null )
		{
			IMSCustomData data = metadata.getData();
			if( data != null )
			{
				PropBagEx xml = data.getXml();
				String type = xml.getNode("type");
				if( !Check.isEmpty(type) && type.equals(MyPagesConstants.MYPAGES_CONTENT_TYPE) )
				{
					HtmlAttachment htmlAttachment = new HtmlAttachment();
					htmlAttachment.setUuid(resource.getIdentifier());
					htmlAttachment.setUrl(resource.getHref());
					htmlAttachment.setDescription(xml.getNode("htmldata/description"));
					htmlAttachment.setParentFolder(packageFolder);
					return htmlAttachment;
				}
			}
		}
		return null;
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