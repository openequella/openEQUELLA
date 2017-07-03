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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import com.dytech.devlib.Base64;
import com.dytech.devlib.PropBagEx;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.io.ByteStreams;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.FileAttachment;
import com.tle.beans.item.attachments.ImsAttachment;
import com.tle.common.PathUtils;
import com.tle.common.Utils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.core.services.FileSystemService;
import com.tle.mets.MetsIDElementInfo;

import edu.harvard.hul.ois.mets.BinData;
import edu.harvard.hul.ois.mets.FContent;
import edu.harvard.hul.ois.mets.helper.MetsElement;
import edu.harvard.hul.ois.mets.helper.MetsException;
import edu.harvard.hul.ois.mets.helper.MetsIDElement;
import edu.harvard.hul.ois.mets.helper.PCData;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
public abstract class AbstractMetsAttachmentImportExporter implements MetsAttachmentImporterExporter
{
	@Inject
	private MimeTypeService mimeTypeService;
	@Inject
	private FileSystemService fileSystemService;

	protected MetsIDElementInfo<? extends MetsIDElement> exportBinaryFile(FileHandle file, String filename,
		long fileSize, String description, String id, String uuid)
	{
		return exportBinaryFile(file, filename, fileSize, description, id, uuid, null);
	}

	@SuppressWarnings("unchecked")
	protected MetsIDElementInfo<? extends MetsIDElement> exportBinaryFile(FileHandle file, String filename,
		long fileSize, String description, String id, String uuid, XmlCallback xml)
	{
		final FContent content = new FContent();
		content.setID(id);
		content.getContent().add(getFileAsBinData(file, filename));
		if( fileSize < 0 )
		{
			try
			{
				fileSize = fileSystemService.fileLength(file, filename);
			}
			catch( FileNotFoundException e )
			{
				throw new RuntimeException(e);
			}
		}

		return new MetsIDElementInfo<FContent>(content, mimeTypeService.getMimeTypeForFilename(filename), getXml(
			filename, fileSize, description, uuid, xml));
	}

	@SuppressWarnings("unchecked")
	protected <T> T getFirst(List<?> l, Class<T> clazz)
	{
		for( Object o : l )
		{
			if( clazz.isAssignableFrom(o.getClass()) )
			{
				return (T) o;
			}
		}
		return null;
	}

	protected PropBagEx getXml(String filename, long size, String description, String uuid, XmlCallback xmlCb)
	{
		PropBagEx xml = new PropBagEx();
		xml.setNode("url", filename);
		xml.setNode("description", description);
		xml.setNode("size", size);
		if( uuid != null )
		{
			xml.setNode("uuid", uuid);
		}
		if( xmlCb != null )
		{
			xmlCb.addXml(xml);
		}

		return xml;
	}

	@SuppressWarnings("unchecked")
	private BinData getFileAsBinData(FileHandle file, String filename)
	{
		try( InputStream in = fileSystemService.read(file, filename) )
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ByteStreams.copy(in, baos);

			BinData data = new BinData();
			data.getContent().add(new PCData(new Base64().encode(baos.toByteArray())));
			return data;
		}
		catch( IOException io )
		{
			throw Throwables.propagate(io);
		}
	}

	/**
	 * Only reads the first PCData content of bin. Returns null if none found.
	 * 
	 * @param bin
	 * @param staging
	 * @param targetFolder where the file will be written
	 * @param filename Can be null in which case it uses the "url" node in
	 *            xmlData
	 * @return
	 * @throws MetsException
	 */
	protected ImportInfo importBinaryFile(BinData bin, FileHandle staging, String targetFolder, String filename,
		PropBagEx xmlData)
	{
		final PCData pc = getFirst(bin.getContent(), PCData.class);
		if( pc != null )
		{
			final String resultFilename = Utils
				.coalesce(xmlData.getNode("url"), filename, UUID.randomUUID().toString());
			final String resultFilepath = PathUtils.filePath(targetFolder, resultFilename);
			final StringBuilder base64data = new StringBuilder();
			for( Object data : pc.getContent() )
			{
				base64data.append(data);
			}
			final byte[] bytes = new Base64().decode(base64data.toString());

			try
			{
				fileSystemService.write(staging, resultFilepath, new ByteArrayInputStream(bytes), true);
				int size = 0;
				final String sizeNode = xmlData.getNode("size");
				if( !Strings.isNullOrEmpty(sizeNode) )
				{
					size = Integer.parseInt(sizeNode);
				}
				if( size == 0 )
				{
					size = bytes.length;
				}
				final String description = Utils.coalesce(xmlData.getNode("description"), resultFilename);
				final String attachmentUuid = Utils.coalesce(xmlData.getNode("uuid"), UUID.randomUUID().toString());
				return new ImportInfo(size, description, resultFilepath, attachmentUuid);
			}
			catch( IOException io )
			{
				throw new RuntimeException(CurrentLocale.get("com.tle.mets.treebuilder.couldntwrite"));
			}
		}
		return null;
	}

	protected void populateStandardProperties(Attachment attachment, ImportInfo info)
	{
		attachment.setUuid(info.getUuid());
		attachment.setUrl(info.getUrl());
		attachment.setDescription(info.getDescription());
		if( attachment instanceof ImsAttachment )
		{
			((ImsAttachment) attachment).setSize(info.getSize());
		}
		else if( attachment instanceof FileAttachment )
		{
			((FileAttachment) attachment).setSize(info.getSize());
		}
	}

	protected boolean idPrefixMatch(MetsElement elem, String id)
	{
		return elem instanceof MetsIDElement && Utils.safeStartsWith(((MetsIDElement) elem).getID(), id, true);
	}

	protected static class ImportInfo
	{
		private final long size;
		private final String description;
		private final String url;
		private final String uuid;

		protected ImportInfo(long size, String description, String url, String uuid)
		{
			this.size = size;
			this.description = description;
			this.url = url;
			this.uuid = uuid;
		}

		public long getSize()
		{
			return size;
		}

		public String getDescription()
		{
			return description;
		}

		public String getUrl()
		{
			return url;
		}

		public String getUuid()
		{
			return uuid;
		}
	}

	protected interface XmlCallback
	{
		void addXml(PropBagEx xml);
	}
}
