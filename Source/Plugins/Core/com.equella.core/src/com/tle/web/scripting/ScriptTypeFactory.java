/*
 * Copyright 2019 Apereo
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

package com.tle.web.scripting;

import com.google.inject.assistedinject.Assisted;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.attachments.Attachment;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.searching.SearchResults;
import com.tle.core.guice.BindFactory;
import com.tle.web.scripting.types.AttachmentScriptTypeImpl;
import com.tle.web.scripting.types.CollectionScriptTypeImpl;
import com.tle.web.scripting.types.ItemScriptTypeImpl;
import com.tle.web.scripting.types.SearchResultsScriptTypeImpl;

@BindFactory
public interface ScriptTypeFactory
{
	CollectionScriptTypeImpl createCollection(@Assisted("collection") ItemDefinition collection);

	ItemScriptTypeImpl createItem(@Assisted("item") Item item);

	ItemScriptTypeImpl createItem(@Assisted("itemId") ItemId itemId);

	AttachmentScriptTypeImpl createAttachment(@Assisted("attachment") Attachment attachment, @Assisted("staging") FileHandle staging);

	SearchResultsScriptTypeImpl createSearchResults(@Assisted("results") SearchResults<Item> results);
}
