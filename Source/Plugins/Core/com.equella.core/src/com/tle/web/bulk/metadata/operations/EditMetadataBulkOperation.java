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

package com.tle.web.bulk.metadata.operations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.dytech.devlib.PropBagEx;
import com.dytech.devlib.PropBagEx.PropBagThoroughIterator;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.item.ItemPack;
import com.tle.core.item.operations.AbstractWorkflowOperation;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.core.item.standard.ItemOperationFactory;
import com.tle.core.item.standard.operations.EditMetadataOperation;
import com.tle.core.security.impl.SecureOnCall;
import com.tle.web.bulk.metadata.model.Modification.ModificationKeys;

@SecureOnCall(priv = "EDIT_ITEM")
public class EditMetadataBulkOperation extends AbstractWorkflowOperation
{

	private final List<Map<ModificationKeys, String>> modifications;
	private final List<List<String>> nodes;
	@Inject
	private ItemOperationFactory editMetadataFactory;

	@AssistedInject
	public EditMetadataBulkOperation(@Assisted("nodes") List<List<String>> nodes,
		@Assisted("mods") List<Map<ModificationKeys, String>> mods)
	{
		modifications = mods;
		this.nodes = nodes;

	}

	@SuppressWarnings("nls")
	@Override
	public boolean execute()
	{
		PropBagEx itemXml = new PropBagEx(getItem().getItemXml().getXml());

		List<WorkflowOperation> metaList = new ArrayList<WorkflowOperation>();

		for( int x = 0; x < nodes.size(); x++ )
		{
			List<String> currentNodes = nodes.get(x);
			Map<ModificationKeys, String> op = modifications.get(x);
			for( String node : currentNodes )
			{
				String action = op.get(ModificationKeys.ACTION);
				if( !action.equalsIgnoreCase("set") )
				{
					PropBagThoroughIterator nodeIterator = itemXml.iterateAll(node);
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
						|| (setOption.equalsIgnoreCase("exists") && itemXml.nodeExists(node)) )
					{
						PropBagThoroughIterator nodeIterator = itemXml.iterateAll(node);
						while( nodeIterator.hasNext() )
						{
							PropBagEx currentNode = nodeIterator.next();
							currentNode.setNode("/", setText);
						}
					}
					if( setOption.equalsIgnoreCase("always")
						|| (setOption.equalsIgnoreCase("create") && !itemXml.nodeExists(node)) )
					{
						itemXml.setNode(node, setText);
					}
				}

			}

			ItemPack newItemPack = getItemPack();
			newItemPack.setXml(itemXml);
			EditMetadataOperation metaOp = editMetadataFactory.editMetadata(newItemPack);
			metaList.add(metaOp);
		}
		itemService.executeOperationsNow(params, metaList);
		return true;
	}

}
