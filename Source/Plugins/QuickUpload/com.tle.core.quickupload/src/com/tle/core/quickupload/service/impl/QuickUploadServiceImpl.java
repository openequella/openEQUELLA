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

package com.tle.core.quickupload.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.springframework.transaction.annotation.Transactional;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.FileInfo;
import com.dytech.edge.exceptions.RuntimeApplicationException;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.entity.Schema;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.FileAttachment;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.core.collection.service.ItemDefinitionService;
import com.tle.core.filesystem.staging.service.StagingService;
import com.tle.core.guice.Bind;
import com.tle.core.guice.BindFactory;
import com.tle.core.item.dao.AttachmentDao;
import com.tle.core.item.operations.AbstractWorkflowOperation;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.core.item.service.ItemService;
import com.tle.core.item.standard.ItemOperationFactory;
import com.tle.core.quickupload.service.QuickUploadService;
import com.tle.core.services.FileSystemService;
import com.tle.core.settings.service.ConfigurationService;

@Bind(QuickUploadService.class)
@Singleton
@SuppressWarnings("nls")
public class QuickUploadServiceImpl implements QuickUploadService
{
	public static final String ONE_CLICK_COLLECTION = "one.click.collection";

	@Inject
	private StagingService stagingService;
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private ItemDefinitionService itemDefinitionService;
	@Inject
	private ItemService itemService;
	@Inject
	private AttachmentDao attachmentDao;
	@Inject
	private ConfigurationService configService;
	@Inject
	private ItemOperationFactory itemOperationFactory;
	@Inject
	private QuickUploadFactory quickUploadFactory;

	@Override
	public ItemDefinition getOneClickItemDef()
	{
		String uuid = configService.getProperty(ONE_CLICK_COLLECTION);
		if( uuid != null )
		{
			List<ItemDefinition> ids = itemDefinitionService.getMatchingCreatableUuid(Collections.singleton(uuid));
			if( !ids.isEmpty() )
			{
				return ids.get(0);
			}
		}
		return null;
	}

	@Override
	@Transactional
	public Pair<ItemId, Attachment> createOrSelectExisting(InputStream inputStream, final String filename)
		throws IOException
	{
		return this.createOrSelectExisting(inputStream, filename, null);
	}

	@Override
	@Transactional
	public Pair<ItemId, Attachment> createOrSelectExisting(InputStream inputStream, final String filename,
		Map<String, List<String>> params) throws IOException
	{
		StagingFile staging = stagingService.createStagingArea();
		boolean clearStaging = true;
		ItemDefinition collection = getOneClickItemDef();
		if( collection != null )
		{
			try
			{
				FileInfo fileInfo = fileSystemService.write(staging, filename, inputStream, false, true);
				List<Attachment> attachs = attachmentDao.findByMd5Sum(fileInfo.getMd5CheckSum(), collection, true);
				Pair<ItemId, Attachment> attInfo;

				if( !attachs.isEmpty() )
				{
					Attachment att = attachs.get(0);
					attInfo = new Pair<ItemId, Attachment>(att.getItem().getItemId(), att);
				}
				else
				{
					PropBagEx root = new PropBagEx("<xml/>");
					List<WorkflowOperation> ops = new ArrayList<WorkflowOperation>();
					FileAttachment fa = new FileAttachment();
					fa.setFilename(filename);
					fa.setMd5sum(fileInfo.getMd5CheckSum());
					fa.setSize(fileInfo.getLength());
					fa.setDescription(filename);

					if( !Check.isEmpty(params) )
					{
						for( Map.Entry<String, List<String>> entry : params.entrySet() )
						{
							String name = entry.getKey();
							List<String> values = entry.getValue();

							for( String value : values )
							{
								root.createNode("integration/" + name, value);
							}
						}
					}
					ops.add(itemOperationFactory.create(root, collection, staging));
					ops.add(quickUploadFactory.create(fa, filename));
					ops.add(itemOperationFactory.submit());
					ops.add(itemOperationFactory.save());
					ItemPack<Item> itemPack = itemService.operation(null,
						ops.toArray(new WorkflowOperation[ops.size()]));
					attInfo = new Pair<ItemId, Attachment>(itemPack.getItem().getItemId(), fa);
					clearStaging = false;
				}

				return attInfo;
			}
			finally
			{
				if( clearStaging )
				{
					stagingService.removeStagingArea(staging, true);
				}

			}
		}

		throw new RuntimeApplicationException("No quick contribute collection configured");
	}

	static final class QuickUploadOperation extends AbstractWorkflowOperation
	{
		private final FileAttachment fattach;
		private final String filename;

		@AssistedInject
		private QuickUploadOperation(@Assisted FileAttachment fattach, @Assisted String filename)
		{
			this.fattach = fattach;
			this.filename = filename;
		}

		@Override
		public boolean execute()
		{
			getItem().getAttachments().add(fattach);
			final Schema schema = getSchema();
			final PropBagEx itemxml = getItemXml();
			itemxml.setNode(schema.getItemNamePath(), filename);
			itemxml.setNode(schema.getItemDescriptionPath(), filename);
			return true;
		}

	}

	@BindFactory
	interface QuickUploadFactory
	{
		QuickUploadOperation create(FileAttachment file, String filename);
	}
}
