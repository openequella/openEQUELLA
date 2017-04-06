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
