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

package com.tle.mypages.mets;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.devlib.Base64;
import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.Constants;
import com.dytech.edge.common.FileInfo;
import com.google.common.io.CharStreams;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.HtmlAttachment;
import com.tle.beans.item.attachments.ItemNavigationNode;
import com.tle.common.PathUtils;
import com.tle.common.filesystem.FileEntry;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.core.filesystem.ItemFile;
import com.tle.core.guice.Bind;
import com.tle.core.item.service.ItemFileService;
import com.tle.core.services.FileSystemService;
import com.tle.mets.MetsIDElementInfo;
import com.tle.mets.importerexporters.AbstractMetsAttachmentImportExporter;
import com.tle.mets.importerexporters.AttachmentAdder;
import com.tle.mypages.MyPagesPackageExporterUtils;
import com.tle.mypages.service.MyPagesService;
import com.tle.web.sections.SectionInfo;

import edu.harvard.hul.ois.mets.BinData;
import edu.harvard.hul.ois.mets.FContent;
import edu.harvard.hul.ois.mets.File;
import edu.harvard.hul.ois.mets.helper.MetsElement;
import edu.harvard.hul.ois.mets.helper.MetsIDElement;
import edu.harvard.hul.ois.mets.helper.PCData;

/**
 * @author Aaron
 */
@Bind
@Singleton
public class MyPagesMetsAttachmentImporterExporter extends AbstractMetsAttachmentImportExporter
{
	@Inject
	private MyPagesPackageExporterUtils exporterUtil;

	@Inject
	private MyPagesService myPagesService;
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private ItemFileService itemFileService;

	@Override
	public boolean canExport(Item item, Attachment attachment)
	{
		return attachment.getAttachmentType() == AttachmentType.HTML;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<MetsIDElementInfo<? extends MetsIDElement>> export(SectionInfo info, Item item, Attachment attachment)
	{
		try
		{
			final HtmlAttachment page = (HtmlAttachment) attachment;
			final ItemFile itemFile = itemFileService.getItemFile(item);
			final List<MetsIDElementInfo<? extends MetsIDElement>> res = new ArrayList<MetsIDElementInfo<? extends MetsIDElement>>();
			final FContent content = new FContent();
			content.setID("html:" + attachment.getUuid());
			content.getContent().add(getHtmlAsBinData(info, item, itemFile, page));

			PropBagEx xml = new PropBagEx();
			xml.setNode("url", page.getFilename());
			xml.setNode("description", page.getDescription());
			xml.setNode("uuid", page.getUuid());
			res.add(new MetsIDElementInfo<FContent>(content, "text/html", xml));

			// Add all embedded content.
			final FileEntry[] files = fileSystemService.enumerate(itemFile, page.getFolder(), null);
			for( FileEntry file : files )
			{
				final String filename = file.getName();
				if( !filename.equals("page.html") )
				{
					final String uuid = UUID.randomUUID().toString();
					res.add(exportBinaryFile(itemFileService.getItemFile(item),
						PathUtils.filePath(page.getFolder(), filename), -1, filename, "htmlres:" + uuid, uuid,
						new XmlCallback()
						{
							@Override
							public void addXml(PropBagEx resXml)
							{
								resXml.createNode("parent", page.getUuid());
							}
						}));
				}
			}

			return res;
		}
		catch( IOException io )
		{
			throw new RuntimeException(io);
		}
	}

	@SuppressWarnings("unchecked")
	private BinData getHtmlAsBinData(SectionInfo info, Item item, FileHandle file, HtmlAttachment page)
		throws IOException
	{
		try( Reader rdr = exporterUtil.localiseHtmlForExport(info, item, page, file) )
		{
			final StringWriter wrt = new StringWriter();
			CharStreams.copy(rdr, wrt);

			final BinData data = new BinData();
			data.getContent().add(new PCData(new Base64().encode(wrt.toString().getBytes(Constants.UTF8))));
			return data;
		}
	}

	@Override
	public boolean canImport(File parentElem, MetsElement elem, PropBagEx xmlData, ItemNavigationNode parentNode)
	{
		return idPrefixMatch(elem, "html:") || idPrefixMatch(elem, "htmlres:");
	}

	private Reader getBinDataAsHtml(Item item, FileHandle file, String packageFolder, HtmlAttachment page)
	{
		String thisPackageBase = PathUtils.filePath("items", item.getItemId().toString(), packageFolder);
		return exporterUtil.institutionaliseHtmlForImport(item, page, file, thisPackageBase);
	}

	@Override
	public void doImport(Item item, FileHandle staging, String packageFolder, File parentElem, MetsElement elem,
		PropBagEx xmlData, ItemNavigationNode parentNode, AttachmentAdder attachmentAdder)
	{
		final FContent content = (FContent) elem;

		if( idPrefixMatch(elem, "html:") )
		{
			final BinData data = getFirst(content.getContent(), BinData.class);
			if( data != null )
			{
				// write the bin data directly first and then convert
				// that
				final ImportInfo loc = importBinaryFile(data, staging, packageFolder, null, xmlData);
				final String importedFilepath = loc.getUrl();

				final HtmlAttachment page = new HtmlAttachment();
				page.setUuid(xmlData.getNode("uuid"));
				page.setDescription(xmlData.getNode("description"));
				page.setParentFolder(packageFolder);

				try( Reader rdr = getBinDataAsHtml(item, staging, packageFolder, page) )
				{
					myPagesService.saveHtml(staging, importedFilepath, rdr);
				}
				catch( IOException e )
				{
					throw new RuntimeException(e);
				}

				final FileInfo fileInfo = fileSystemService.getFileInfo(staging, importedFilepath);
				page.setSize(fileInfo.getLength());
				attachmentAdder.addAttachment(parentNode, page, fileInfo.getFilename());
				return;
			}
		}
		else if( idPrefixMatch(elem, "htmlres:") )
		{
			final BinData data = getFirst(content.getContent(), BinData.class);
			if( data != null )
			{
				// it's just a binary file. create it but don't make an
				// attachment
				importBinaryFile(data, staging, packageFolder, null, xmlData);
				return;
			}
		}
	}
}
