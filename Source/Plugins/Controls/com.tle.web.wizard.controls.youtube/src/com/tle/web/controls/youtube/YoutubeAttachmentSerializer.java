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

package com.tle.web.controls.youtube;

import java.util.Date;
import java.util.Map;

import org.joda.time.Duration;

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
public class YoutubeAttachmentSerializer extends AbstractAttachmentSerializer
{

	@Override
	public EquellaAttachmentBean serialize(Attachment attachment)
	{
		CustomAttachment cattach = (CustomAttachment) attachment;
		YoutubeAttachmentBean ybean = new YoutubeAttachmentBean();
		ybean.setVideoId((String) cattach.getData(YoutubeUtils.PROPERTY_ID));
		ybean.setTitle((String) cattach.getData(YoutubeUtils.PROPERTY_TITLE));
		ybean.setTags((String) cattach.getData(YoutubeUtils.PROPERTY_TAGS));
		ybean.setThumbUrl((String) cattach.getData(YoutubeUtils.PROPERTY_THUMB_URL));
		ybean.setViewUrl((String) cattach.getData(YoutubeUtils.PROPERTY_PLAY_URL));
		ybean.setCustomParameters((String) cattach.getData(YoutubeUtils.PROPERTY_PARAMETERS));
		final Long date = (Long) cattach.getData(YoutubeUtils.PROPERTY_DATE);
		if( date != null )
		{
			ybean.setUploadedDate(new Date(date));
		}
		ybean.setUploader((String) cattach.getData(YoutubeUtils.PROPERTY_AUTHOR));
		final Object durationData = cattach.getData(YoutubeUtils.PROPERTY_DURATION);
		String duration = null;
		if( durationData instanceof String )
		{
			duration = (String) durationData;
		}
		else if( durationData instanceof Long )
		{
			long oldDuration = (long) durationData;
			duration = Duration.standardSeconds(oldDuration).toPeriod().toString();
		}

		if( duration != null )
		{
			ybean.setDuration(duration);
		}
		return ybean;
	}

	@SuppressWarnings({"nls"})
	@Override
	public Map<String, Class<? extends EquellaAttachmentBean>> getAttachmentBeanTypes()
	{
		Builder<String, Class<? extends EquellaAttachmentBean>> builder = ImmutableMap.builder();
		builder.put("youtube", YoutubeAttachmentBean.class);
		return builder.build();
	}

	@Override
	public String deserialize(EquellaAttachmentBean bean, ItemEditor itemEditor)
	{
		String uuid = bean.getUuid();
		YoutubeAttachmentEditor editor = itemEditor.getAttachmentEditor(uuid, YoutubeAttachmentEditor.class);
		YoutubeAttachmentBean youBean = (YoutubeAttachmentBean) bean;
		editStandard(editor, youBean);
		editor.editVideoId(youBean.getVideoId());
		editor.editUploader(youBean.getUploader());
		editor.editTags(youBean.getTags());
		editor.editDuration(youBean.getDuration());
		editor.editTitle(youBean.getTitle());
		editor.editThumbUrl(youBean.getThumbUrl());
		editor.editViewUrl(youBean.getViewUrl());
		editor.editUploadedDate(youBean.getUploadedDate());
		editor.editCustomParameters(youBean.getCustomParameters());
		return uuid;
	}

	@Override
	public boolean exportable(EquellaAttachmentBean bean)
	{
		return true;
	}
}
