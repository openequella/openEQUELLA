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

package com.tle.web.myresource.converter;

import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.Constants;
import com.tle.beans.item.ItemStatus;
import com.tle.common.Check;
import com.tle.common.filesystem.handle.BucketFile;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.AbstractItemXmlMigrator;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.services.FileSystemService;
import com.tle.mycontent.MyContentConstants;

@Bind
@Singleton
public class MyResourceConverter extends AbstractItemXmlMigrator
{
	private static final String FOLDER_UUID = "2f6bd1b8-6ddb-3b7c-554c-646617b1dad7"; //$NON-NLS-1$

	@SuppressWarnings("nls")
	private static final Set<String> ASSEM_COLLECTIONS = new HashSet<String>(Arrays.asList(
		"d243936d-67ba-0a6f-6f4c-4a2a6b676d54", "855f6055-5271-1e13-ceae-336e70cf5110",
		"16dda617-1829-8555-1510-4348c162c592", "5ac082d2-3015-aba1-a749-cd928a5c6e9c",
		"77279582-ce3f-97ee-84c3-66de5af5a4c5", "01d4757e-b10e-788d-a713-176427d4f90c",
		"e8f050dd-f6c0-4cec-559f-e54d7ef19836", "5eafc9ff-cad1-7290-2bd5-bd0cb7c193ee",
		"16815372-d700-0aa8-83d6-cf9906f5a0ef"));

	@Inject
	private FileSystemService fileSystemService;

	@Override
	public void beforeMigrate(ConverterParams params, TemporaryFileHandle staging, SubTemporaryFile file)
		throws Exception
	{
		// this bean does not get recreated a second time,
		// which means you'll have to empty out the data structures:
		params.setAttribute(MyResourceConverter.class, new ConvertInfo());
	}

	@SuppressWarnings("nls")
	@Override
	public boolean migrate(ConverterParams params, PropBagEx xml, SubTemporaryFile handle, String filename)
		throws Exception
	{
		ConvertInfo info = params.getAttribute(MyResourceConverter.class);
		final String uuid = xml.getNode("itemDefinition/@uuid");

		// if item is something of these types
		if( ASSEM_COLLECTIONS.contains(uuid) )
		{
			addItemXml(info, handle, filename, xml);
		}
		else if( uuid.equals(FOLDER_UUID) )
		{
			info.folders.put(xml.getNode("folder"),
				xml.getNode("name/strings/entry/com.tle.beans.entity.LanguageString/text"));
		}
		return false;
	}

	@SuppressWarnings("nls")
	private void addItemXml(ConvertInfo info, SubTemporaryFile handle, String filename, PropBagEx xml) throws Exception
	{
		final String dataFolderName = filename.replace(".xml", Constants.BLANK);
		final SubTemporaryFile dataFolder = new SubTemporaryFile(handle, dataFolderName);

		// Only valid if the attachment exists
		final String attUrl = xml.getNode("attachments/com.tle.beans.item.attachments.FileAttachment/url");
		if( Check.isEmpty(attUrl) || !fileSystemService.fileExists(dataFolder, attUrl) )
		{
			return;
		}

		try( InputStream in = fileSystemService.read(dataFolder, "_ITEM/item.xml") )
		{
			final PropBagEx metadata = new PropBagEx(in);

			// Only valid if it is not global
			if( metadata.isNodeTrue("item/@global") )
			{
				return;
			}

			info.items.add(new ItemXml(xml, metadata, dataFolderName));
		}
	}

	@SuppressWarnings("nls")
	@Override
	public void afterMigrate(ConverterParams params, SubTemporaryFile staging) throws Exception
	{
		ConvertInfo info = params.getAttribute(MyResourceConverter.class);
		int i = 0;
		for( ItemXml item : info.items )
		{
			i++;

			final String newId = "myresource" + i;

			final PropBagEx itemXml = item.getItem();
			itemXml.setNode("id", "0");
			itemXml.setNode("itemDefinition/@uuid", MyContentConstants.MY_CONTENT_UUID);
			itemXml.setNode("uuid", UUID.randomUUID().toString());
			itemXml.setNode("status", ItemStatus.PERSONAL.name());

			final PropBagEx metadataXml = item.getMetadata();
			metadataXml.createNode("content_type", "myresource");
			metadataXml.deleteNode("item/@global");
			metadataXml.deleteNode("item/@myitem");

			final String folderName = info.folders.get(item.getItem().getNode("folder"));
			if( !Check.isEmpty(folderName) )
			{
				metadataXml.setNode("keywords", metadataXml.getNode("keywords") + " " + folderName);
			}

			final SubTemporaryFile originalDataFolder = new SubTemporaryFile(staging, item.getDataFolderPath());
			final BucketFile bucketFolder = new BucketFile(staging, newId);
			final SubTemporaryFile newDataFolder = new SubTemporaryFile(bucketFolder, newId);

			fileSystemService.copy(originalDataFolder, newDataFolder);
			fileSystemService.write(newDataFolder, "/_ITEM/item.xml", new StringReader(item.getMetadata().toString()),
				false);
			fileSystemService.write(bucketFolder, newId + ".xml", new StringReader(item.getItem().toString()), false);
		}
	}

	public static class ConvertInfo
	{
		Map<String, String> folders = new HashMap<String, String>();
		List<ItemXml> items = new ArrayList<ItemXml>();
	}

	public static class ItemXml
	{
		private final PropBagEx item;
		private final PropBagEx metadata;
		private final String dataFolderPath;

		public ItemXml(PropBagEx item, PropBagEx metadata, String dataFolderPath)
		{
			this.item = item;
			this.metadata = metadata;
			this.dataFolderPath = dataFolderPath;
		}

		public PropBagEx getItem()
		{
			return item;
		}

		public PropBagEx getMetadata()
		{
			return metadata;
		}

		public String getDataFolderPath()
		{
			return dataFolderPath;
		}
	}
}
