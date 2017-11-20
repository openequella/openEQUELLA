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

package com.tle.web.scorm.treeviewer;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import com.tle.annotation.Nullable;
import com.tle.beans.item.IItem;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.item.attachments.ItemNavigationNode;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.mimetypes.MimeTypeConstants;
import com.tle.web.scorm.ScormUtils;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewitem.AbstractAttachmentViewItemResource;
import com.tle.web.viewitem.section.RootItemFileSection;
import com.tle.web.viewitem.treeviewer.TreeNavigationSection;
import com.tle.web.viewurl.UseViewer;
import com.tle.web.viewurl.ViewItemFilter;
import com.tle.web.viewurl.ViewItemResource;
import com.tle.web.viewurl.ViewItemService;
import com.tle.web.viewurl.ViewItemViewer;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceService;

@SuppressWarnings("nls")
@Bind
public class ScormTreeNavigationSection extends TreeNavigationSection implements ViewItemFilter
{
	public static final String VIEWSCORM_JSP = "viewscorm.jsp";

	@TreeLookup
	private RootItemFileSection rootSection;

	@Inject
	private ViewItemService viewItemService;

	@Inject
	private AttachmentResourceService attachmentResourceService;

	@Override
	public String getDefaultPropertyName()
	{
		return "scormtree";
	}

	@Override
	protected List<ItemNavigationNode> getTreeNodes(SectionInfo info)
	{
		return getTreeNodes(info, Collections.singletonList(getAttachment(info, getModel(info).getResource())), true);
	}

	@Nullable
	@Override
	public IAttachment getAttachment(SectionInfo info, ViewItemResource resource)
	{
		final ViewableItem<?> vitem = resource.getViewableItem();
		final IItem<?> item = vitem.getItem();
		final List<CustomAttachment> scorms = new UnmodifiableAttachments(item).getCustomList("scorm");
		if( scorms.size() > 0 )
		{
			return scorms.get(0);
		}
		return null;
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		rootSection.addFilterViewer(this);
	}

	@Override
	public int getOrder()
	{
		return 0;
	}

	@Override
	public ViewItemResource filter(SectionInfo info, ViewItemResource resource)
	{
		String rvid = resource.getDefaultViewerId();
		if( Objects.equals(ScormTreeNavigationSection.VIEWSCORM_JSP, resource.getFilepath()) )
		{
			if( Check.isEmpty(rvid) )
			{
				ViewableItem<?> vi = resource.getViewableItem();
				CustomAttachment att = new UnmodifiableAttachments(vi.getItem())
					.getFirstCustomOfType(ScormUtils.ATTACHMENT_TYPE);

				if( att != null )
				{
					if( "pssViewer".equals(viewItemService.getDefaultViewerId(MimeTypeConstants.MIME_SCORM)) )
					{
						return new ScormTreeAttachmentViewItemResource(resource,
							attachmentResourceService.getViewableResource(info, vi, att), false);
					}
					return new UseViewer(resource, this);
				}
			}
			else if( Objects.equals(rvid, "downloadIms") || Objects.equals("file", rvid) )
			{
				return new UseViewer(resource, this);
			}
		}

		return resource;
	}

	public class ScormTreeAttachmentViewItemResource extends AbstractAttachmentViewItemResource
	{
		public ScormTreeAttachmentViewItemResource(ViewItemResource inner, ViewableResource viewableResource,
			boolean forcedStream)
		{
			super(inner, viewableResource, forcedStream);
		}

		@Override
		public ViewItemViewer getViewer()
		{
			return forcedStream ? ScormTreeNavigationSection.this : null;
		}
	}
}
