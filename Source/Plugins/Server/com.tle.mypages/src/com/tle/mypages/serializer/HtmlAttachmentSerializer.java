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
