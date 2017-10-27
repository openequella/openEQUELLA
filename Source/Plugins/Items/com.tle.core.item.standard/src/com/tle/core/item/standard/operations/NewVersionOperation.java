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

package com.tle.core.item.standard.operations;

import java.util.HashMap;
import java.util.Map;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.ScriptContext;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.item.HistoryEvent;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemStatus;
import com.tle.common.Check;
import com.tle.core.item.scripting.WorkflowScriptConstants;
import com.tle.core.security.impl.SecureOnCall;

/**
 * @author jmaginnis
 */
@SecureOnCall(priv = "NEWVERSION_ITEM")
public class NewVersionOperation extends AbstractCloneOperation
{
	private static final String NEW_VERSION_SCRIPT_NAME = "newVersion"; //$NON-NLS-1$

	@AssistedInject
	protected NewVersionOperation()
	{
		this(true);
	}

	@AssistedInject
	protected NewVersionOperation(@Assisted boolean copyAttachments)
	{
		super(copyAttachments);
	}

	@Override
	protected Item initItemUuidAndVersion(Item newItem, Item oldItem)
	{
		int latestVersion = itemService.getLatestVersion(getUuid());
		newItem.setUuid(oldItem.getUuid());
		newItem.setVersion(latestVersion + 1);
		return newItem;
	}

	@Override
	protected void finalProcessing(Item origItem, Item item)
	{
		super.finalProcessing(origItem, item);

		setState(ItemStatus.DRAFT);
		// new version script
		PropBagEx newxml = (PropBagEx) getItemPack().getXml().clone();

		final String script = getCollection().getWizard().getRedraftScript();
		if( !Check.isEmpty(script) )
		{
			Map<String, Object> attributes = new HashMap<String, Object>();
			attributes.put(WorkflowScriptConstants.NEW_XML, newxml);
			ScriptContext context = createScriptContext(attributes);

			itemService.executeScript(script, NEW_VERSION_SCRIPT_NAME, context, true);
		}
		getItemPack().setXml(newxml);
	}

	@Override
	protected void doHistory()
	{
		createHistory(HistoryEvent.Type.newversion);
	}
}
