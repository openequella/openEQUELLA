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

package com.tle.web.bulk.metadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.dytech.devlib.PropBagEx;
import com.dytech.devlib.PropBagEx.PropBagThoroughIterator;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemPack;
import com.tle.core.guice.Bind;
import com.tle.core.guice.BindFactory;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.core.item.service.ItemService;
import com.tle.core.item.standard.ItemOperationFactory;
import com.tle.core.plugins.BeanLocator;
import com.tle.core.plugins.FactoryMethodLocator;
import com.tle.web.bulk.metadata.model.BulkEditMetadataModel;
import com.tle.web.bulk.metadata.model.Modification;
import com.tle.web.bulk.metadata.model.Modification.ModificationKeys;
import com.tle.web.bulk.metadata.operations.EditMetadataBulkFactory;
import com.tle.web.bulk.metadata.section.BulkEditMetadataSection;
import com.tle.web.bulk.operation.BulkOperationExecutor;
import com.tle.web.bulk.operation.BulkOperationExtension;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.utils.KeyOption;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.model.Option;

@Bind
public class BulkEditMetadataOperation extends BulkEditMetadataSection implements BulkOperationExtension
{
	private static final String BULK_METADATA_VAL = "editmetadata"; //$NON-NLS-1$

	@PlugKey("bulkop.editmetadata")
	private static String LABEL_METADATA;
	@PlugKey("bulkop.editmetadata.title")
	private static Label LABEL_METADATA_TITLE;

	@Inject
	private ItemService itemService;

	@BindFactory
	public interface EditMetadataBulkExecutorFactory
	{
		EditMetadataBulkExecutor create(@Assisted("mods") List<Modification> mods);
	}

	@Override
	public void register(SectionTree tree, String parentId)
	{
		tree.registerInnerSection(this, parentId);

	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
	}

	@Override
	public void addOptions(SectionInfo info, List<Option<OperationInfo>> options)
	{
		options.add(new KeyOption<BulkOperationExtension.OperationInfo>(LABEL_METADATA, BULK_METADATA_VAL,
			new OperationInfo(this, BULK_METADATA_VAL)));

	}

	@Override
	public BeanLocator<EditMetadataBulkExecutor> getExecutor(SectionInfo info, String operationId)
	{
		return new FactoryMethodLocator<EditMetadataBulkExecutor>(EditMetadataBulkExecutorFactory.class, "create",
			Lists.newArrayList(getModel(info).getModifications()));
	}

	@Override
	public void prepareDefaultOptions(SectionInfo info, String operationId)
	{
		// nada
	}

	@Override
	public SectionRenderable renderOptions(RenderContext context, String operationId)
	{
		return renderSection(context, this);
	}

	@Override
	public Label getStatusTitleLabel(SectionInfo info, String operationId)
	{

		return LABEL_METADATA_TITLE;
	}

	@Override
	public boolean validateOptions(SectionInfo info, String operationId)
	{
		BulkEditMetadataModel model = getModel(info);
		return (model.getModifications().size() > 0) && !(model.isSchemaSelection() || model.isActionSelection());
	}

	@Override
	public boolean areOptionsFinished(SectionInfo info, String operationId)
	{
		return validateOptions(info, operationId);
	}

	@Override
	public boolean hasExtraOptions(SectionInfo info, String operationId)
	{
		return true;
	}

	public static class EditMetadataBulkExecutor implements BulkOperationExecutor
	{
		List<List<String>> nodes = new ArrayList<List<String>>();
		List<Map<ModificationKeys, String>> params = new ArrayList<Map<ModificationKeys, String>>();

		@Inject
		private EditMetadataBulkFactory metadataFactory;
		@Inject
		private ItemOperationFactory workflowFactory;

		@Inject
		public EditMetadataBulkExecutor(@Assisted("mods") List<Modification> mods)
		{
			for( Modification mod : mods )
			{
				nodes.add(mod.getNodes());
				params.add(mod.getParmas());
			}
		}

		@Override
		public WorkflowOperation[] getOperations()
		{
			return new WorkflowOperation[]{workflowFactory.startEdit(true), metadataFactory.editMetada(nodes, params),
					workflowFactory.save()};
		}

		@Override
		public String getTitleKey()
		{
			return "com.tle.web.bulk.metadata.bulk.editmetadata.title";
		}
	}

	@Override
	public boolean hasExtraNavigation(SectionInfo info, String operationId)
	{
		return true;
	}

	@Override
	public Collection<Button> getExtraNavigation(SectionInfo info, String operationId)
	{
		return Lists.newArrayList(getModificationReturnButton(), getChooseActionButton(), getEditNodesButton(),
			getSaveActionButton());
	}

	@Override
	public boolean hasPreview(SectionInfo info, String operationId)
	{
		return true;
	}

	@SuppressWarnings("nls")
	@Override
	public ItemPack runPreview(SectionInfo info, String operationId, long itemId) throws Exception
	{
		ItemKey itemKey = itemService.getItemIdKey(itemId);
		PropBagEx itemXml = itemService.getItemXmlPropBag(itemKey);
		List<Modification> mods = getModel(info).getModifications();

		for( int x = 0; x < mods.size(); x++ )
		{
			Modification currentMod = mods.get(x);
			Map<ModificationKeys, String> op = currentMod.getParmas();
			for( String nodePath : currentMod.getNodes() )
			{
				String action = op.get(ModificationKeys.ACTION);
				if( !action.equalsIgnoreCase("set") )
				{
					PropBagThoroughIterator nodeIterator = itemXml.iterateAll(nodePath);
					while( nodeIterator.hasNext() )
					{
						PropBagEx currentNode = nodeIterator.next();
						if( action.equalsIgnoreCase("replace") )
						{
							String nodeText = currentNode.getNode();
							nodeText = nodeText.replace(op.get(ModificationKeys.REPLACE_FIND),
								op.get(ModificationKeys.REPLACE_WITH));
							currentNode.setNode("/", nodeText);
						}
						else if( action.equalsIgnoreCase("add") )
						{
							PropBagEx addedXml = new PropBagEx(op.get(ModificationKeys.ADD_XML));
							currentNode.append("/", addedXml);
						}
					}
				}
				else
				{
					// SET
					String setOption = op.get(ModificationKeys.SET_TEXT_OPTION);
					String setText = op.get(ModificationKeys.SET_TEXT);
					if( setOption.equalsIgnoreCase("always")
						|| (setOption.equalsIgnoreCase("exists") && itemXml.nodeExists(nodePath)) )
					{
						PropBagThoroughIterator nodeIterator = itemXml.iterateAll(nodePath);
						while( nodeIterator.hasNext() )
						{
							PropBagEx currentNode = nodeIterator.next();
							currentNode.setNode("/", setText);
						}
					}
					if( setOption.equalsIgnoreCase("always")
						|| (setOption.equalsIgnoreCase("create") && !itemXml.nodeExists(nodePath)) )
					{
						itemXml.setNode(nodePath, setText);
					}
				}
			}
		}
		ItemPack itemPack = itemService.getItemPack(itemKey);
		itemPack.setXml(itemXml);
		return itemPack;
	}

	@Override
	public boolean showPreviousButton(SectionInfo info, String opererationId)
	{
		BulkEditMetadataModel model = getModel(info);
		return !(model.isSchemaSelection() || model.isActionSelection());
	}

}
