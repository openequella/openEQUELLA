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

package com.tle.web.controls.googlebook;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.inject.Singleton;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.core.guice.Bind;
import com.tle.core.item.edit.ItemEditor;
import com.tle.core.item.serializer.AbstractAttachmentSerializer;
import com.tle.web.api.item.equella.interfaces.beans.EquellaAttachmentBean;

@Bind
@Singleton
public class GoogleBookAttachmentSerializer extends AbstractAttachmentSerializer
{

	@Override
	public EquellaAttachmentBean serialize(Attachment attachment)
	{
		CustomAttachment custom = (CustomAttachment) attachment;
		GoogleBookAttachmentBean gattach = new GoogleBookAttachmentBean();
		gattach.setViewUrl((String) custom.getData(GoogleBookConstants.PROPERTY_URL));
		gattach.setThumbUrl((String) custom.getData(GoogleBookConstants.PROPERTY_THUMB_URL));
		gattach.setPublishedDate((String) custom.getData(GoogleBookConstants.PROPERTY_PUBLISHED));
		gattach.setPages((String) custom.getData(GoogleBookConstants.PROPERTY_FORMATS));
		gattach.setBookId((String) custom.getData(GoogleBookConstants.PROPERTY_ID));
		return gattach;
	}

	@SuppressWarnings({"nls"})
	@Override
	public Map<String, Class<? extends EquellaAttachmentBean>> getAttachmentBeanTypes()
	{
		Builder<String, Class<? extends EquellaAttachmentBean>> builder = ImmutableMap.builder();
		builder.put("googlebook", GoogleBookAttachmentBean.class);
		return builder.build();
	}

	@Override
	public String deserialize(EquellaAttachmentBean bean, ItemEditor itemEditor)
	{
		String uuid = bean.getUuid();
		GoogleBookAttachmentEditor attachEditor = itemEditor
			.getAttachmentEditor(uuid, GoogleBookAttachmentEditor.class);
		GoogleBookAttachmentBean bookBean = (GoogleBookAttachmentBean) bean;
		editStandard(attachEditor, bookBean);
		attachEditor.editBookId(bookBean.getBookId());
		attachEditor.editThumbUrl(bookBean.getThumbUrl());
		attachEditor.editViewUrl(bookBean.getViewUrl());
		attachEditor.editPages(bookBean.getPages());
		attachEditor.editPublishedDate(bookBean.getPublishedDate());
		return uuid;
	}

	@Override
	public boolean exportable(EquellaAttachmentBean bean)
	{
		return true;
	}
}
