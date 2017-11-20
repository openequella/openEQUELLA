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

package com.tle.core.item.serializer.impl;

import com.tle.core.guice.PluginTrackerModule;
import com.tle.core.item.edit.ItemAttachmentListener;
import com.tle.core.item.edit.ItemMetadataListener;
import com.tle.core.item.edit.attachment.AbstractAttachmentEditor;
import com.tle.core.item.serializer.AttachmentSerializer;
import com.tle.core.item.serializer.ItemDeserializerEditor;
import com.tle.core.item.serializer.ItemSerializerProvider;

public class ItemSerializerModule extends PluginTrackerModule
{
	@Override
	protected String getPluginId()
	{
		return "com.tle.web.api.item.equella.serializer";
	}

	@Override
	@SuppressWarnings("nls")
	protected void configure()
	{
		bindTracker(ItemSerializerProvider.class, "serializer", "bean");
		bindTracker(ItemDeserializerEditor.class, "deserializer", "bean");
		bindTracker(AttachmentSerializer.class, "attachmentSerializer", "bean").setIdParam("type");
		bindTracker(AbstractAttachmentEditor.class, "attachmentEditor", "bean").setIdParam("class");
		bindTracker(ItemMetadataListener.class, "metadataListener", "bean");
		bindTracker(ItemAttachmentListener.class, "attachmentListener", "bean");
	}
}
