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
