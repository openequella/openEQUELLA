package com.tle.core.item.serializer;

import java.util.Map;

import com.tle.beans.item.attachments.Attachment;
import com.tle.core.item.edit.ItemEditor;
import com.tle.web.api.item.equella.interfaces.beans.EquellaAttachmentBean;

public interface AttachmentSerializer
{
	EquellaAttachmentBean serialize(Attachment attachment);

	String deserialize(EquellaAttachmentBean bean, ItemEditor itemEditor);

	Map<String, Class<? extends EquellaAttachmentBean>> getAttachmentBeanTypes();

	/**
	 * Attachment can be imported into another system. E.g Equella resource
	 * attachments wouldn't work,so are not exportable. Only the relevant
	 * serializer (based on AttachmentBean.getRawAttachmentType()) will be asked
	 * if the attachment is exportable
	 * 
	 * @param bean
	 * @return
	 */
	boolean exportable(EquellaAttachmentBean bean);
}
