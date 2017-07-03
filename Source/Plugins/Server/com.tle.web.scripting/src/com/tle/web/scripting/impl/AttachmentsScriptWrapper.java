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

package com.tle.web.scripting.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.dytech.common.io.UnicodeReader;
import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.Constants;
import com.dytech.edge.common.FileInfo;
import com.dytech.edge.common.PropBagWrapper;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.beans.item.attachments.FileAttachment;
import com.tle.beans.item.attachments.HtmlAttachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.item.attachments.LinkAttachment;
import com.tle.beans.item.attachments.ModifiableAttachments;
import com.tle.common.Check;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.item.AttachmentUtils;
import com.tle.common.scripting.objects.AttachmentsScriptObject;
import com.tle.common.scripting.types.AttachmentScriptType;
import com.tle.common.scripting.types.BinaryDataScriptType;
import com.tle.common.scripting.types.ItemScriptType;
import com.tle.common.scripting.types.XmlScriptType;
import com.tle.core.item.helper.ItemHelper;
import com.tle.core.item.service.ItemService;
import com.tle.core.services.FileSystemService;
import com.tle.web.scripting.impl.ItemScriptWrapper.ItemScriptTypeImpl;
import com.tle.web.scripting.impl.UtilsScriptWrapper.BinaryDataScriptTypeImpl;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
public class AttachmentsScriptWrapper extends AbstractScriptWrapper implements AttachmentsScriptObject
{
	private static final long serialVersionUID = 1L;

	private final ModifiableAttachments attachments;
	private final FileSystemService fileSystem;
	private final ItemService itemService;
	private final ItemHelper itemHelper;
	private final FileHandle staging;

	public AttachmentsScriptWrapper(ModifiableAttachments attachments, FileSystemService fileSystem,
		ItemService itemService, ItemHelper itemHelper, FileHandle staging)
	{
		this.attachments = attachments;
		this.fileSystem = fileSystem;
		this.staging = staging;
		this.itemService = itemService;
		this.itemHelper = itemHelper;
	}

	@Override
	public AttachmentScriptType add(AttachmentScriptType attachment)
	{
		if( attachment instanceof AttachmentScriptTypeImpl )
		{
			attachments.addAttachment(((AttachmentScriptTypeImpl) attachment).wrapped);
			return attachment;
		}
		return null;
	}

	@Override
	public List<AttachmentScriptType> list()
	{
		List<AttachmentScriptType> all = new ArrayList<AttachmentScriptType>();
		for( IAttachment attach : attachments )
		{
			all.add(new AttachmentScriptTypeImpl((Attachment) attach, staging));
		}
		return all;
	}

	@Override
	public List<AttachmentScriptType> listForItem(ItemScriptType item)
	{
		Item realItem = ((ItemScriptTypeImpl) item).getItem();
		return Lists
			.newArrayList(Lists.transform(realItem.getAttachments(), new Function<Attachment, AttachmentScriptType>()
			{
				@Override
				public AttachmentScriptType apply(Attachment o)
				{
					return new AttachmentScriptTypeImpl(o, staging);
				}
			}));
	}

	@Override
	public void remove(AttachmentScriptType attachment)
	{
		if( attachment instanceof AttachmentScriptTypeImpl )
		{
			attachments.removeAttachment(((AttachmentScriptTypeImpl) attachment).wrapped);
		}
	}

	@Override
	public void clear()
	{
		for( AttachmentScriptType attachment : list() )
		{
			remove(attachment);
		}
	}

	@Override
	public AttachmentScriptType getByUuid(String uuid)
	{
		Attachment attach = (Attachment) attachments.getAttachmentByUuid(uuid);
		if( attach != null )
		{
			return new AttachmentScriptTypeImpl(attach, staging);
		}
		return null;
	}

	@Override
	public AttachmentScriptType getByFilename(String filename)
	{
		Attachment attach = (Attachment) attachments.getAttachmentByFilename(filename);
		if( attach != null )
		{
			return new AttachmentScriptTypeImpl(attach, staging);
		}
		return null;
	}

	@Override
	public AttachmentScriptType createLinkAttachment(String url, String description)
	{
		LinkAttachment link = new LinkAttachment();
		link.setUrl(url);
		link.setDescription(description);
		return new AttachmentScriptTypeImpl(link, staging);
	}

	@Override
	public AttachmentScriptType createCustomAttachment(String customType, String description)
	{
		CustomAttachment custom = new CustomAttachment();
		custom.setType(customType);
		custom.setDescription(description);
		return new AttachmentScriptTypeImpl(custom, staging);
	}

	@Override
	public AttachmentScriptType createResourceAttachment(String itemUuid, int itemVersion, String attachmentUuid,
		String description)
	{
		if( Check.isEmpty(itemUuid) )
		{
			throw new RuntimeException("Error creating resource attachment, you must supply an item UUID");
		}

		CustomAttachment custom = new CustomAttachment();
		custom.setType("resource");
		custom.setData("uuid", itemUuid);
		custom.setData("version", itemVersion);
		if( !Check.isEmpty(attachmentUuid) )
		{
			custom.setData("type", "a");
			custom.setUrl(attachmentUuid);
		}
		else
		{
			custom.setData("type", "p");
		}
		if( !Check.isEmpty(description) )
		{
			custom.setDescription(description);
		}
		return new AttachmentScriptTypeImpl(custom, staging);
	}

	@Override
	public AttachmentScriptType createTextFileAttachment(String filename, String description, String contents)
	{
		try
		{
			StringReader reader = new StringReader(contents);
			FileInfo file = fileSystem.write(staging, filename, reader, false);
			if( file != null )
			{
				FileAttachment attach = new FileAttachment();
				attach.setFilename(file.getFilename());
				attach.setDescription(description);
				attach.setSize(file.getLength());
				return new AttachmentScriptTypeImpl(attach, staging);
			}
			return null;
		}
		catch( IOException io )
		{
			throw new RuntimeException("Error creating text file attachment " + filename, io);
		}
	}

	@Override
	public AttachmentScriptType createHtmlAttachment(String filename, String description, String contents)
	{
		try
		{
			StringReader reader = new StringReader(contents);
			FileInfo file = fileSystem.write(staging, filename, reader, false);
			if( file != null )
			{
				HtmlAttachment attach = new HtmlAttachment();
				attach.setFilename(file.getFilename());
				attach.setDescription(description);
				attach.setSize(file.getLength());
				return new AttachmentScriptTypeImpl(attach, staging);
			}
			return null;
		}
		catch( IOException io )
		{
			throw new RuntimeException("Error creating HTML attachment " + filename, io);
		}
	}

	@Override
	public AttachmentScriptType createBinaryFileAttachment(String filename, String description,
		BinaryDataScriptType contents)
	{
		try
		{
			ByteArrayInputStream is = new ByteArrayInputStream(((BinaryDataScriptTypeImpl) contents).getData());
			FileInfo file = fileSystem.write(staging, filename, is, false);
			if( file != null )
			{
				FileAttachment attach = new FileAttachment();
				attach.setFilename(file.getFilename());
				attach.setDescription(description);
				attach.setSize(file.getLength());
				return new AttachmentScriptTypeImpl(attach, staging);
			}
			return null;
		}
		catch( IOException io )
		{
			throw new RuntimeException("Error creating binary file attachment " + filename, io);
		}
	}

	@Override
	public AttachmentScriptType addExistingFileAsAttachment(String filename, String description)
	{
		if( !fileSystem.fileExists(staging, filename) )
		{
			throw new RuntimeException("File " + filename + " not found");
		}

		FileInfo file = fileSystem.getFileInfo(staging, filename);
		FileAttachment attach = new FileAttachment();
		attach.setFilename(filename);
		attach.setDescription(description);
		attach.setSize(file.getLength());

		attachments.addAttachment(attach);
		return new AttachmentScriptTypeImpl(attach, staging);
	}

	@Override
	public AttachmentScriptType editTextFileAttachment(AttachmentScriptType attachment, String newContents)
	{
		if( attachment != null )
		{
			String type = attachment.getType();
			if( type.equals(AttachmentType.FILE.toString()) || type.equals(AttachmentType.IMSRES.toString())
				|| type.equals(AttachmentType.HTML.toString()) )
			{
				try
				{
					StringReader reader = new StringReader(newContents);
					FileInfo file = fileSystem.write(staging, attachment.getFilename(), reader, false);

					if( attachment instanceof AttachmentScriptTypeImpl )
					{
						Attachment wrapped = ((AttachmentScriptTypeImpl) attachment).getWrapped();
						if( wrapped instanceof FileAttachment )
						{
							((FileAttachment) wrapped).setSize(file.getLength());
						}
					}
					return attachment;
				}
				catch( IOException io )
				{
					throw new RuntimeException("Error editing text file attachment " + attachment.getFilename(), io);
				}
			}
		}
		return null;
	}

	@Override
	public String readTextFileAttachment(AttachmentScriptType attachment)
	{
		return readTextFileAttachmentWithEncoding(attachment, Constants.UTF8);
	}

	@Override
	public String readTextFileAttachmentWithEncoding(AttachmentScriptType attachment, String encoding)
	{
		if( attachment != null )
		{
			String type = attachment.getType();
			if( type.equals(AttachmentType.FILE.toString()) || type.equals(AttachmentType.IMSRES.toString())
				|| type.equals(AttachmentType.HTML.toString()) )
			{
				final String filename = attachment.getFilename();
				try( Reader reader = new UnicodeReader(fileSystem.read(staging, filename), encoding) )
				{
					StringWriter writer = new StringWriter();
					CharStreams.copy(reader, writer);
					return writer.toString();
				}
				catch( IOException io )
				{
					throw new RuntimeException("Error reading text file attachment " + filename, io);
				}
			}

			throw new RuntimeException("Cannot read an attachment of this type as text");
		}
		return null;
	}

	@Override
	public XmlScriptType readXmlFileAttachment(AttachmentScriptType attachment)
	{
		return new PropBagWrapper(new PropBagEx(readTextFileAttachment(attachment)));
	}

	@Override
	public List<ItemScriptType> getAttachedItemResources()
	{
		List<ItemScriptType> items = new ArrayList<ItemScriptType>();

		List<CustomAttachment> customList = attachments.getCustomList("resource");
		for( CustomAttachment attachment : customList )
		{
			ItemId itemId = new ItemId((String) attachment.getData("uuid"), (Integer) attachment.getData("version"));

			items.add(new ItemScriptTypeImpl(itemService, itemHelper, itemId));
		}
		return items;
	}

	public static class AttachmentScriptTypeImpl implements AttachmentScriptType
	{
		private static final long serialVersionUID = 1L;

		private final Attachment wrapped;
		private final FileHandle staging;

		public AttachmentScriptTypeImpl(Attachment wrapped, FileHandle staging)
		{
			this.wrapped = wrapped;
			this.staging = staging;

		}

		@Override
		public String getFilename()
		{
			return wrapped.getUrl();
		}

		@Override
		public String getUrl()
		{
			return wrapped.getUrl();
		}

		@Override
		public void setUrl(String url)
		{
			wrapped.setUrl(url);
		}

		@Override
		public String getUuid()
		{
			return wrapped.getUuid();
		}

		@Override
		public String getDescription()
		{
			return wrapped.getDescription();
		}

		@Override
		public void setDescription(String description)
		{
			wrapped.setDescription(description);
		}

		@Override
		public String getType()
		{
			return wrapped.getAttachmentType().toString();
		}

		@Override
		public long getSize()
		{
			if( wrapped instanceof FileAttachment )
			{
				return ((FileAttachment) wrapped).getSize();
			}
			return 0;
		}

		@Override
		public String getThumbnail()
		{
			return wrapped.getThumbnail();
		}

		@Override
		public void setThumbnail(String path)
		{
			wrapped.setThumbnail(path);
		}

		@Override
		public String getCustomType()
		{
			CustomAttachment custom = ensureCustomAttachment();
			return custom.getType();
		}

		@Override
		public Object getCustomProperty(String propertyName)
		{
			CustomAttachment custom = ensureCustomAttachment();
			return custom.getData(propertyName);
		}

		@Override
		public void setCustomIntegerProperty(String propertyName, int propertyValue)
		{
			setCustomProperty(propertyName, propertyValue);
		}

		@Override
		public void setCustomProperty(String propertyName, Object propertyValue)
		{
			CustomAttachment custom = ensureCustomAttachment();
			custom.setData(propertyName, propertyValue);
		}

		/**
		 * Internal use only! Do not use in scripts
		 * 
		 * @return
		 */
		public Attachment getWrapped()
		{
			return wrapped;
		}

		/**
		 * Internal use only! Do not use in scripts
		 * 
		 * @return
		 */
		public FileHandle getStagingFile()
		{
			return this.staging;
		}

		private CustomAttachment ensureCustomAttachment()
		{
			AttachmentType type = wrapped.getAttachmentType();
			if( type != AttachmentType.CUSTOM )
			{
				throw new RuntimeException(
					"This method requires an attachment of type CUSTOM, but it is of type " + type.toString());
			}
			return (CustomAttachment) wrapped;
		}

		@Override
		public void setCustomDisplayProperty(String key, Object value)
		{
			LinkedHashMap<String, Object> map = getCustomDisplayMap();
			if( value != null )
			{
				map.put(key, value);
			}
			else
			{
				map.remove(key);
			}
			wrapped.setData(AttachmentUtils.CUSTOM_DISPLAY_KEY, map);
		}

		@Override
		public Object getCustomDisplayProperty(String key)
		{
			return getCustomDisplayMap().get(key);
		}

		@Override
		public List<String> getAllCustomDisplayProperties()
		{
			return ImmutableList.copyOf(getCustomDisplayMap().keySet());
		}

		@SuppressWarnings("unchecked")
		private LinkedHashMap<String, Object> getCustomDisplayMap()
		{
			Object map = wrapped.getData(AttachmentUtils.CUSTOM_DISPLAY_KEY);
			return map != null ? Maps.newLinkedHashMap((LinkedHashMap<String, Object>) map)
				: new LinkedHashMap<String, Object>();
		}
	}
}
