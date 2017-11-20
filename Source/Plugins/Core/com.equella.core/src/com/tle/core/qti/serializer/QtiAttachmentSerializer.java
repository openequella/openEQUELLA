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

package com.tle.core.qti.serializer;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.inject.Singleton;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.core.guice.Bind;
import com.tle.core.item.edit.ItemEditor;
import com.tle.core.item.serializer.AbstractAttachmentSerializer;
import com.tle.core.qti.QtiConstants;
import com.tle.core.qti.beans.QtiAttachmentBean;
import com.tle.web.api.item.equella.interfaces.beans.EquellaAttachmentBean;

@Bind
@Singleton
@SuppressWarnings("nls")
public class QtiAttachmentSerializer extends AbstractAttachmentSerializer
{
	@Override
	public EquellaAttachmentBean serialize(Attachment attachment)
	{
		CustomAttachment cattach = (CustomAttachment) attachment;
		QtiAttachmentBean qbean = new QtiAttachmentBean();

		qbean.setXmlFullPath((String) cattach.getData(QtiConstants.KEY_XML_PATH));
		qbean.setTestUuid((String) cattach.getData(QtiConstants.KEY_TEST_UUID));
		qbean.setToolName((String) cattach.getData(QtiConstants.KEY_TOOL_NAME));
		qbean.setToolVersion((String) cattach.getData(QtiConstants.KEY_TOOL_VERSION));
		qbean.setMaxTime((Long) cattach.getData(QtiConstants.KEY_MAX_TIME));
		qbean.setQuestionCount((Integer) cattach.getData(QtiConstants.KEY_QUESTION_COUNT));
		qbean.setSectionCount((Integer) cattach.getData(QtiConstants.KEY_SECTION_COUNT));
		qbean.setNavigationMode((String) cattach.getData(QtiConstants.KEY_NAVIGATION_MODE));
		qbean.setManifestPath(cattach.getUrl());

		return qbean;
	}

	@Override
	public String deserialize(EquellaAttachmentBean bean, ItemEditor itemEditor)
	{
		String uuid = bean.getUuid();
		QtiAttachmentEditor editor = itemEditor.getAttachmentEditor(uuid, QtiAttachmentEditor.class);
		QtiAttachmentBean qbean = (QtiAttachmentBean) bean;
		editStandard(editor, qbean);
		editor.editXmlFullPath(qbean.getXmlFullPath());
		editor.editTestUuid(qbean.getTestUuid());
		editor.editToolName(qbean.getToolName());
		editor.editToolVersion(qbean.getToolVersion());
		editor.editMaxTime(qbean.getMaxTime());
		editor.editQuestionCount(qbean.getQuestionCount());
		editor.editSectionCount(qbean.getSectionCount());
		editor.editNavigationMode(qbean.getNavigationMode());
		editor.editManifestPath(qbean.getManifestPath());

		return uuid;
	}

	@Override
	public Map<String, Class<? extends EquellaAttachmentBean>> getAttachmentBeanTypes()
	{
		Builder<String, Class<? extends EquellaAttachmentBean>> builder = ImmutableMap.builder();
		builder.put("qtitest", QtiAttachmentBean.class);
		return builder.build();
	}

	@Override
	public boolean exportable(EquellaAttachmentBean bean)
	{
		return true;
	}
}