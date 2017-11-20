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

package com.tle.web.controls.resource;

import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.web.controls.universal.UniversalAttachment;
import com.tle.web.selection.SelectedResourceDetails;

public class ResourceUniversalAttachment implements UniversalAttachment
{
	private final SelectedResourceDetails selection;
	private final CustomAttachment attachment;

	public ResourceUniversalAttachment(SelectedResourceDetails selection, CustomAttachment attachment)
	{
		this.attachment = attachment;
		this.selection = selection;
	}

	@Override
	public Attachment getAttachment()
	{
		return attachment;
	}

	public SelectedResourceDetails getSelection()
	{
		return selection;
	}

}
