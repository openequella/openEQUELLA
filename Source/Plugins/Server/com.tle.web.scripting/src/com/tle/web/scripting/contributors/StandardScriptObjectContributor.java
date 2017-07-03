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

package com.tle.web.scripting.contributors;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.edge.common.Constants;
import com.dytech.edge.common.PropBagWrapper;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.ItemStatus;
import com.tle.beans.item.attachments.ModifiableAttachments;
import com.tle.common.scripting.objects.AttachmentsScriptObject;
import com.tle.common.scripting.objects.FileScriptObject;
import com.tle.common.scripting.objects.ItemScriptObject;
import com.tle.common.scripting.objects.LoggingScriptObject;
import com.tle.common.scripting.objects.SystemScriptObject;
import com.tle.common.scripting.objects.UserScriptObject;
import com.tle.common.scripting.objects.UtilsScriptObject;
import com.tle.common.scripting.service.ScriptContextCreationParams;
import com.tle.common.scripting.types.ItemScriptType;
import com.tle.common.util.Logger;
import com.tle.core.guice.Bind;
import com.tle.core.item.helper.ItemHelper;
import com.tle.core.item.service.ItemFileService;
import com.tle.core.item.service.ItemService;
import com.tle.core.scripting.service.ScriptObjectContributor;
import com.tle.core.services.FileSystemService;
import com.tle.core.services.user.UserService;
import com.tle.core.util.script.SearchScriptWrapper;
import com.tle.web.scripting.impl.AttachmentsScriptWrapper;
import com.tle.web.scripting.impl.FileScriptingObjectImpl;
import com.tle.web.scripting.impl.ItemScriptWrapper;
import com.tle.web.scripting.impl.ItemScriptWrapper.ItemScriptTypeImpl;
import com.tle.web.scripting.impl.LoggingScriptWrapper;
import com.tle.web.scripting.impl.SystemScriptWrapper;
import com.tle.web.scripting.impl.UserScriptWrapper;
import com.tle.web.scripting.impl.UtilsScriptWrapper;

/**
 * @author aholland
 */
@Bind
@Singleton
public class StandardScriptObjectContributor implements ScriptObjectContributor
{
	private static final String ITEM = "item"; //$NON-NLS-1$
	private static final String XML = "xml"; //$NON-NLS-1$
	private static final String ITEM_STATUS = "status"; //$NON-NLS-1$

	@Deprecated
	private static final String SEARCH_WRAPPER = "search"; //$NON-NLS-1$

	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private ItemService itemService;
	@Inject
	private ItemFileService itemFileService;
	@Inject
	private ItemHelper itemHelper;
	@Inject
	private UserService userService;

	// Context insensitive objects
	@Inject
	@Deprecated
	private SearchScriptWrapper search;
	@Inject
	private UtilsScriptWrapper utils;
	@Inject
	private SystemScriptWrapper system;
	@Inject
	private ItemScriptWrapper items;

	@Override
	public void addScriptObjects(Map<String, Object> objects, ScriptContextCreationParams params)
	{
		// search is legacy. Should be using utils.search instead
		objects.put(SEARCH_WRAPPER, search);

		objects.put(UtilsScriptObject.DEFAULT_VARIABLE, utils);
		objects.put(ItemScriptObject.DEFAULT_VARIABLE, items);
		if( params.isAllowSystemCalls() )
		{
			objects.put(SystemScriptObject.DEFAULT_VARIABLE, system);
		}

		objects.put(UserScriptObject.DEFAULT_VARIABLE, new UserScriptWrapper(userService));

		if( params.getFileHandle() != null )
		{
			objects.put(FileScriptObject.DEFAULT_VARIABLE,
				new FileScriptingObjectImpl(fileSystemService, itemFileService, params.getFileHandle()));
		}

		final PropBagWrapper wrapper = new PropBagWrapper();
		final ItemPack<Item> pack = params.getItemPack();
		if( pack != null )
		{
			wrapper.setPropBag(pack.getXml());

			final Item item = pack.getItem();
			if( item != null )
			{
				// for backwards compatability ONLY. Undocumented 'feature'. Use
				// the proper item wrapping script objects such as 'attachments'
				// or 'items'
				objects.put(ITEM, item);

				final ItemStatus status = item.getStatus();
				String s = Constants.BLANK;
				if( status != null )
				{
					// Needs to be lower case
					s = status.toString();
				}
				objects.put(ITEM_STATUS, s);

				objects.put(AttachmentsScriptObject.DEFAULT_VARIABLE,
					new AttachmentsScriptWrapper(new ModifiableAttachments(item), fileSystemService, itemService,
						itemHelper, params.getFileHandle()));

				objects.put(ItemScriptType.CURRENT_ITEM, new ItemScriptTypeImpl(itemService, itemHelper, item));
			}
		}

		objects.put(XML, wrapper);

		// logger is an attribute - not important enough
		final Map<String, Object> attributes = params.getAttributes();
		final Logger logger = (Logger) attributes.get(LoggingScriptObject.DEFAULT_VARIABLE);
		if( logger != null )
		{
			objects.put(LoggingScriptObject.DEFAULT_VARIABLE, new LoggingScriptWrapper(logger));
		}
	}
}
