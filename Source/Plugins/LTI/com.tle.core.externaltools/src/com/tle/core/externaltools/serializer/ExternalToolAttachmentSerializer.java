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

package com.tle.core.externaltools.serializer;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.inject.Singleton;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.common.externaltools.constants.ExternalToolConstants;
import com.tle.core.externaltools.beans.ExternalToolAttachmentBean;
import com.tle.core.guice.Bind;
import com.tle.core.item.edit.ItemEditor;
import com.tle.core.item.serializer.AbstractAttachmentSerializer;
import com.tle.web.api.item.equella.interfaces.beans.EquellaAttachmentBean;

@Bind
@Singleton
@SuppressWarnings("nls")
public class ExternalToolAttachmentSerializer extends AbstractAttachmentSerializer
{
	@Override
	public EquellaAttachmentBean serialize(Attachment attachment)
	{
		final CustomAttachment cattach = (CustomAttachment) attachment;
		final ExternalToolAttachmentBean bean = new ExternalToolAttachmentBean();
		bean.setExternalToolProviderUuid(getData(cattach, ExternalToolConstants.EXTERNAL_TOOL_PROVIDER_UUID));
		bean.setLaunchUrl(getData(cattach, ExternalToolConstants.LAUNCH_URL));
		bean.setConsumerKey(getData(cattach, ExternalToolConstants.CONSUMER_KEY));
		bean.setConsumerSecret(getData(cattach, ExternalToolConstants.SHARED_SECRET));
		bean.setCustomParameters(getData(cattach, ExternalToolConstants.CUSTOM_PARAMS));
		bean.setIconUrl(getData(cattach, ExternalToolConstants.ICON_URL));
		bean.setShareUserNameDetails(getBooleanData(cattach, ExternalToolConstants.SHARE_NAME));
		bean.setShareUserEmailDetails(getBooleanData(cattach, ExternalToolConstants.SHARE_EMAIL));
		return bean;
	}

	private String getData(CustomAttachment cattach, String key)
	{
		return (String) cattach.getData(key);
	}

	private boolean getBooleanData(CustomAttachment cattach, String key)
	{
		final Boolean val = (Boolean) cattach.getData(key);
		return val == null ? false : val;
	}

	@Override
	public String deserialize(EquellaAttachmentBean bean, ItemEditor itemEditor)
	{
		final String uuid = bean.getUuid();
		final ExternalToolAttachmentEditor editor = itemEditor.getAttachmentEditor(uuid,
			ExternalToolAttachmentEditor.class);
		final ExternalToolAttachmentBean qbean = (ExternalToolAttachmentBean) bean;
		editStandard(editor, qbean);
		editor.editExternalToolProviderUuid(qbean.getExternalToolProviderUuid());
		editor.editLaunchUrl(qbean.getLaunchUrl());
		editor.editConsumerKey(qbean.getConsumerKey());
		editor.editConsumerSecret(qbean.getConsumerSecret());
		editor.editCustomParameters(qbean.getCustomParameters());
		editor.editIconUrl(qbean.getIconUrl());
		editor.editShareUserNameDetails(qbean.isShareUserNameDetails());
		editor.editShareUserEmailDetails(qbean.isShareUserEmailDetails());

		return uuid;
	}

	@Override
	public Map<String, Class<? extends EquellaAttachmentBean>> getAttachmentBeanTypes()
	{
		final Builder<String, Class<? extends EquellaAttachmentBean>> builder = ImmutableMap.builder();
		builder.put("lti", ExternalToolAttachmentBean.class);
		return builder.build();
	}

	@Override
	public boolean exportable(EquellaAttachmentBean bean)
	{
		return true;
	}
}