package com.tle.web.controls.resource;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.inject.Singleton;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.core.guice.Bind;
import com.tle.core.item.edit.ItemEditor;
import com.tle.core.item.serializer.AbstractAttachmentSerializer;
import com.tle.web.api.item.equella.interfaces.beans.EquellaAttachmentBean;
import com.tle.web.selection.SelectedResource;

@Bind
@Singleton
public class ResourceAttachmentSerializer extends AbstractAttachmentSerializer
{
	@Override
	public EquellaAttachmentBean serialize(Attachment attachment)
	{
		CustomAttachment cattach = (CustomAttachment) attachment;
		ResourceAttachmentBean rbean = new ResourceAttachmentBean();
		char type = ((String) cattach.getData(ResourceHandler.DATA_TYPE)).charAt(0);
		rbean.setResourceType(type);
		rbean.setItemUuid((String) cattach.getData(ResourceHandler.DATA_UUID));
		rbean.setItemVersion((Integer) cattach.getData(ResourceHandler.DATA_VERSION));
		if( type == SelectedResource.TYPE_ATTACHMENT )
		{
			rbean.setAttachmentUuid(cattach.getUrl());
		}
		else
		{
			rbean.setResourcePath(cattach.getUrl());
		}
		return rbean;
	}

	@SuppressWarnings({"nls"})
	@Override
	public Map<String, Class<? extends EquellaAttachmentBean>> getAttachmentBeanTypes()
	{
		Builder<String, Class<? extends EquellaAttachmentBean>> builder = ImmutableMap.builder();
		builder.put("linked-resource", ResourceAttachmentBean.class);
		return builder.build();
	}

	@Override
	public String deserialize(EquellaAttachmentBean bean, ItemEditor itemEditor)
	{
		String uuid = bean.getUuid();
		ResourceAttachmentEditor resEditor = itemEditor.getAttachmentEditor(uuid, ResourceAttachmentEditor.class);
		ResourceAttachmentBean resourceBean = (ResourceAttachmentBean) bean;
		resEditor.editItemId(new ItemId(resourceBean.getItemUuid(), resourceBean.getItemVersion()));
		char type = resourceBean.getResourceType();
		resEditor.editType(type);
		if( type == SelectedResource.TYPE_ATTACHMENT )
		{
			resEditor.editAttachmentUuid(resourceBean.getAttachmentUuid());
		}
		else
		{
			resEditor.editPath(resourceBean.getResourcePath());
		}
		editStandard(resEditor, resourceBean);
		return uuid;
	}

	@Override
	public boolean exportable(EquellaAttachmentBean bean)
	{
		return false;
	}
}
