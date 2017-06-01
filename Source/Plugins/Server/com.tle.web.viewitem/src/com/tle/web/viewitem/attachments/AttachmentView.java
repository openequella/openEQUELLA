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

package com.tle.web.viewitem.attachments;

import com.tle.beans.item.attachments.IAttachment;
import com.tle.web.sections.equella.utils.AbstractCombinedRenderer;
import com.tle.web.viewurl.ViewableResource;

public class AttachmentView extends AbstractCombinedRenderer
{
	private final IAttachment attachment;
	private final ViewableResource viewableResource;
	private String overrideViewer;

	public AttachmentView(IAttachment attachment, ViewableResource resource)
	{
		this.attachment = attachment;
		this.viewableResource = resource;
	}

	public IAttachment getAttachment()
	{
		return attachment;
	}

	public ViewableResource getViewableResource()
	{
		return viewableResource;
	}

	public String getOverrideViewer()
	{
		return overrideViewer;
	}

	public void setOverrideViewer(String overrideViewer)
	{
		this.overrideViewer = overrideViewer;
	}
}
