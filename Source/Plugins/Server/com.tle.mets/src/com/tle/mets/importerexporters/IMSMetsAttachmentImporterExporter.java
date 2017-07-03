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

package com.tle.mets.importerexporters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.IMSResourceAttachment;
import com.tle.beans.item.attachments.ImsAttachment;
import com.tle.beans.item.attachments.ItemNavigationNode;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.core.filesystem.ItemFile;
import com.tle.core.guice.Bind;
import com.tle.core.item.service.ItemFileService;
import com.tle.core.services.FileSystemService;
import com.tle.mets.MetsIDElementInfo;
import com.tle.web.sections.SectionInfo;

import edu.harvard.hul.ois.mets.BinData;
import edu.harvard.hul.ois.mets.File;
import edu.harvard.hul.ois.mets.helper.MetsElement;
import edu.harvard.hul.ois.mets.helper.MetsIDElement;

/**
 * @author Aaron
 */
// TODO: should this file live here?
@Bind
@Singleton
@SuppressWarnings("nls")
public class IMSMetsAttachmentImporterExporter extends AbstractMetsAttachmentImportExporter
{
	public static final String HIDE_RESOURCE_KEY = "$HIDE_RESOURCE$";

	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private ItemFileService itemFileService;

	@Override
	public boolean canExport(Item item, Attachment attachment)
	{
		return attachment.getAttachmentType() == AttachmentType.IMS;
	}

	@Override
	public List<MetsIDElementInfo<? extends MetsIDElement>> export(SectionInfo info, Item item, Attachment attachment)
	{
		final List<MetsIDElementInfo<? extends MetsIDElement>> res = new ArrayList<MetsIDElementInfo<? extends MetsIDElement>>();

		final ImsAttachment ims = (ImsAttachment) attachment;

		final ItemFile fileHandle = itemFileService.getItemFile(item);

		// don't export IMS manifest, we already have a METS manifest for this
		/*
		 * res.add(exportBinaryFile(fileHandle,
		 * Utils.filePath(FileSystemConstants.IMS_FOLDER, "imsmanifest.xml"),
		 * ims.getSize(), ims.getDescription(), "ims:" + attachment.getUuid(),
		 * attachment.getUuid()));
		 */

		final Set<String> linkedResources = new HashSet<String>();

		// export IMS resources
		final List<IMSResourceAttachment> resources = new UnmodifiableAttachments(item).getList(AttachmentType.IMSRES);
		for( IMSResourceAttachment resource : resources )
		{
			linkedResources.add(resource.getUrl());
			res.add(exportBinaryFile(itemFileService.getItemFile(item), resource.getUrl(), -1,
				resource.getDescription(), "imsres:" + resource.getUuid(), resource.getUuid()));
		}

		// export files contained within IMS zip but not actually resources
		final List<String> files = fileSystemService.grep(fileHandle, "", "**/*");
		files.removeAll(linkedResources);
		for( String file : files )
		{
			if( file.startsWith(ims.getUrl()) )
			{
				final String uuid = UUID.randomUUID().toString();
				res.add(exportBinaryFile(fileHandle, file, -1, "IMS File", "imsfile:" + uuid, uuid));
			}
		}

		return res;
	}

	@Override
	public boolean canImport(File parentElem, MetsElement elem, PropBagEx xmlData, ItemNavigationNode parentNode)
	{
		return idPrefixMatch(elem, "ims:") || idPrefixMatch(elem, "imsres:") || idPrefixMatch(elem, "imsfile:");
	}

	@Override
	public void doImport(Item item, FileHandle staging, String targetFolder, File parentElem, MetsElement elem,
		PropBagEx xmlData, ItemNavigationNode parentNode, AttachmentAdder attachmentAdder)
	{
		final BinData data = getFirst(elem.getContent(), BinData.class);
		if( data != null )
		{
			final ImportInfo importInfo = importBinaryFile(data, staging, targetFolder, null, xmlData);

			// FIXME: there can only be one package file. we already have a
			// Mets ImsAttachment.
			//
			// if( idPrefixMatch(elem, "ims:") )
			// {
			// final ImsAttachment ims = new ImsAttachment();
			// populateStandardProperties(ims, importInfo);
			// ims.setUrl(Utils.filePath(packageFile,
			// xmlData.getNode("url")));
			//
			// attachmentAdder.addAttachment(null, ims, null);
			//
			// FIXME: delete the METS manifest
			// }
			// else
			if( idPrefixMatch(elem, "imsres:") )
			{
				final IMSResourceAttachment imsres = new IMSResourceAttachment();
				populateStandardProperties(imsres, importInfo);
				imsres.setData(HIDE_RESOURCE_KEY, true);
				imsres.setUrl(importInfo.getUrl());

				attachmentAdder.addAttachment(parentNode, imsres, imsres.getDescription());
			}
		}
	}
}
