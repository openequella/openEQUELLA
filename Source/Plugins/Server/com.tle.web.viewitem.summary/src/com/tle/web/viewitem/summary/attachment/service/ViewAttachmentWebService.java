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

package com.tle.web.viewitem.summary.attachment.service;

import java.util.List;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.ItemKey;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TagRenderer;
import com.tle.web.sections.render.TagState;
import com.tle.web.sections.standard.renderers.LinkTagRenderer;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewitem.AttachmentViewFilter;
import com.tle.web.viewitem.attachments.AttachmentView;
import com.tle.web.viewurl.AttachmentDetail;

@NonNullByDefault
public interface ViewAttachmentWebService
{
	List<AttachmentRowDisplay> createViewsForItem(SectionInfo info, ViewableItem viewableItem, ElementId baseElement,
		boolean renderSelection, boolean details, boolean thumbnail, boolean filtered);

	void filterAttachmentDisplays(SectionInfo info, List<AttachmentRowDisplay> attachmentDisplays);

	void filterAttachmentDisplays(SectionInfo info, List<AttachmentRowDisplay> attachmentDisplays,
		@Nullable AttachmentViewFilter customFilter);

	JSStatements createShowDetailsFunction(ItemKey itemId, String attachmentsJquerySelector);

	JSStatements setupSelectButtonsFunction(JSCallable selectAttachmentFunction, ItemKey itemId,
		@Nullable String itemExtensionType, String attachmentsJquerySelector);

	TagRenderer makeShowDetailsLinkRenderer();

	TagRenderer makeRowRenderer(TagState state, int level, boolean folder);

	SectionRenderable makeAttachmentDetailsRenderer(SectionInfo info, ElementId baseElement, String uuid, int index,
		@Nullable SectionRenderable thumbnail, @Nullable List<AttachmentDetail> details,
		@Nullable List<LinkTagRenderer> alternateViewerLinks);

	@NonNullByDefault(false)
	public static class AttachmentRowDisplay
	{
		private TagRenderer row; // Actually a LI
		private AttachmentView attachmentView;
		private int level;

		public int getLevel()
		{
			return level;
		}

		public void setLevel(int level)
		{
			this.level = level;
		}

		public TagRenderer getRow()
		{
			return row;
		}

		public void setRow(TagRenderer row)
		{
			this.row = row;
		}

		public AttachmentView getAttachmentView()
		{
			return attachmentView;
		}

		public void setAttachmentView(AttachmentView attachmentView)
		{
			this.attachmentView = attachmentView;
		}
	}
}
