package com.tle.mypages.serializer;

import java.util.Map;

import javax.inject.Singleton;

import com.google.common.collect.ImmutableMap;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.HtmlAttachment;
import com.tle.core.guice.Bind;
import com.tle.core.item.edit.ItemEditor;
import com.tle.core.item.serializer.AbstractAttachmentSerializer;
import com.tle.web.api.item.equella.interfaces.beans.EquellaAttachmentBean;
import com.tle.web.api.item.equella.interfaces.beans.HtmlPageAttachmentBean;

/**
 * @author Aaron
 */
@Bind
@Singleton
public class HtmlAttachmentSerializer extends AbstractAttachmentSerializer
{
	@Override
	public EquellaAttachmentBean serialize(Attachment attachment)
	{
		HtmlAttachment hattach = (HtmlAttachment) attachment;
		HtmlPageAttachmentBean hbean = new HtmlPageAttachmentBean();
		hbean.setParentFolder(hattach.getParentFolder());
		hbean.setMd5(hattach.getMd5sum());
		hbean.setFilename(hattach.getFilename());
		hbean.setSize(hattach.getSize());
		return hbean;
	}

	@Override
	public String deserialize(EquellaAttachmentBean attachBean, ItemEditor itemEditor)
	{
		HtmlPageAttachmentBean hattach = (HtmlPageAttachmentBean) attachBean;
		HtmlPageAttachmentEditor heditor = itemEditor.getAttachmentEditor(attachBean.getUuid(),
			HtmlPageAttachmentEditor.class);
		heditor.editParentFolder(hattach.getParentFolder());
		editStandard(heditor, hattach);
		return heditor.getAttachmentUuid();
	}

	@Override
	public Map<String, Class<? extends EquellaAttachmentBean>> getAttachmentBeanTypes()
	{
		return ImmutableMap.<String, Class<? extends EquellaAttachmentBean>> of("htmlpage",
			HtmlPageAttachmentBean.class);
	}

	@Override
	public boolean exportable(EquellaAttachmentBean bean)
	{
		return true;
	}
}
