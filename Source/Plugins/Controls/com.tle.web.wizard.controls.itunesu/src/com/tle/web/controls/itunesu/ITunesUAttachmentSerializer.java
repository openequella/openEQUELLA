package com.tle.web.controls.itunesu;

import java.util.Map;

import javax.inject.Singleton;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.core.guice.Bind;
import com.tle.core.item.edit.ItemEditor;
import com.tle.core.item.serializer.AbstractAttachmentSerializer;
import com.tle.web.api.item.equella.interfaces.beans.EquellaAttachmentBean;

@Bind
@Singleton
public class ITunesUAttachmentSerializer extends AbstractAttachmentSerializer
{
	@Override
	public EquellaAttachmentBean serialize(Attachment attachment)
	{
		CustomAttachment cattach = (CustomAttachment) attachment;
		ITunesUAttachmentBean ibean = new ITunesUAttachmentBean();
		ibean.setPlayUrl((String) cattach.getData(ITunesUHandler.ITUNESU_URL));
		ibean.setTrackName((String) cattach.getData(ITunesUHandler.ITUNESU_TRACK));
		return ibean;
	}

	@SuppressWarnings({"nls"})
	@Override
	public Map<String, Class<? extends EquellaAttachmentBean>> getAttachmentBeanTypes()
	{
		Builder<String, Class<? extends EquellaAttachmentBean>> builder = ImmutableMap.builder();
		builder.put("itunesu", ITunesUAttachmentBean.class);
		return builder.build();
	}

	@Override
	public String deserialize(EquellaAttachmentBean bean, ItemEditor itemEditor)
	{
		String uuid = bean.getUuid();
		ITunesUAttachmentEditor editor = itemEditor.getAttachmentEditor(uuid, ITunesUAttachmentEditor.class);
		ITunesUAttachmentBean ibean = (ITunesUAttachmentBean) bean;
		editStandard(editor, ibean);
		editor.editPlayUrl(ibean.getPlayUrl());
		editor.editTrackName(ibean.getTrackName());
		return uuid;
	}

	@Override
	public boolean exportable(EquellaAttachmentBean bean)
	{
		return true;
	}
}
