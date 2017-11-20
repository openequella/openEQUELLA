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

package com.tle.web.copyright.service;

import java.util.Map;

import com.tle.common.filesystem.handle.FileHandle;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.Attachment;
import com.tle.core.copyright.Holding;
import com.tle.web.sections.SectionInfo;

/**
 * @author Aaron
 */
public interface CopyrightWebService<H extends Holding>
{
	Map<String, Attachment> getAttachmentMap(SectionInfo info, Item item);

	int getStatus(SectionInfo info, Item item, String attachmentUuid);

	H getHolding(SectionInfo info, Item item);

	String getAgreement(FileHandle agreementFile);
}
