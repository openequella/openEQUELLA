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

package com.tle.web.cloud.view.section;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import com.google.common.base.Throwables;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.common.Check;
import com.tle.common.beans.exception.NotFoundException;
import com.tle.web.cloud.view.CloudViewItemAuditor;
import com.tle.web.cloud.view.CloudViewableItem;
import com.tle.web.cloud.view.section.CloudItemSectionInfo.CloudItemSectionInfoFactory;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.annotations.DirectEvent;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.layout.TwoColumnLayout;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.SectionEvent;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.selection.section.RootSelectionSection;
import com.tle.web.stream.ContentStream;
import com.tle.web.template.RenderTemplate;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewurl.ResourceViewer;
import com.tle.web.viewurl.UseViewer;
import com.tle.web.viewurl.ViewAuditEntry;
import com.tle.web.viewurl.ViewItemResource;
import com.tle.web.viewurl.ViewItemService;
import com.tle.web.viewurl.ViewItemUrl;
import com.tle.web.viewurl.ViewItemUrlProcessor;
import com.tle.web.viewurl.ViewItemViewer;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.WrappedViewItemResource;
import com.tle.web.viewurl.attachments.AttachmentResourceService;

// TODO: so much of this shit is copy pasta'd from RootItemFileSection
@SuppressWarnings("nls")
@NonNullByDefault
@TreeIndexed
public class RootCloudViewItemSection extends AbstractPrototypeSection<RootCloudViewItemSection.RootCloudViewItemModel>
	implements
		HtmlRenderer,
		CloudItemSectionInfoFactory,
		ViewItemUrlProcessor
{
	@PlugKey("viewitem.error.notfound.remoteserver")
	private static Label LABEL_ITEM_NOT_FOUND;

	@Inject
	private CloudViewItemAuditor auditor;
	@Inject
	private AttachmentResourceService attachmentResourceService;
	@Inject
	private ViewItemService viewItemService;

	@TreeLookup
	private CloudItemSummarySection summarySection;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		RootCloudViewItemModel model = getModel(context);
		ViewItemResource resource = model.getResource();
		ViewItemViewer viewer = model.getActualViewer();
		ViewableItem<?> viewableItem = resource.getViewableItem();

		if( viewer == null )
		{
			ViewAuditEntry vae = resource.getViewAuditEntry();
			if( viewableItem.isItemForReal() && vae != null )
			{
				auditor.audit(vae, viewableItem.getItemId());
			}
			context.forwardToUrl(resource.createCanonicalURL().getHref(), resource.getForwardCode());
			return null;
		}

		if( viewableItem.isItemForReal() )
		{
			auditor.audit(viewer.getAuditEntry(context, resource), viewableItem.getItemId());
		}
		try
		{
			return viewer.view(context, resource);
		}
		catch( IOException io )
		{
			throw Throwables.propagate(io);
		}
	}

	@DirectEvent(priority = SectionEvent.PRIORITY_LOW)
	public void ensureResourceBeforeRender(SectionInfo info)
	{
		getViewItemResource(info);
	}

	@Nullable
	public ViewItemResource getViewItemResource(SectionInfo info)
	{
		if( info.isRendered() )
		{
			return null;
		}

		final RootCloudViewItemModel model = getModel(info);
		ViewItemResource resource = model.getResource();
		if( resource != null )
		{
			return resource;
		}

		final CloudViewableItem vitem = getViewableItem(info);
		if( vitem == null )
		{
			// we are only generating urls
			return null;
		}

		resource = new CloudBaseViewItemResource(vitem);

		final String attachmentUuid = getModel(info).getAttachmentUuid();
		final IAttachment attachment = (attachmentUuid == null ? null : vitem.getAttachmentByUuid(attachmentUuid));
		if( attachmentUuid != null && attachment == null )
		{
			throw new NotFoundException("Attachment " + attachmentUuid + " could not be found.");
		}
		if( attachment != null )
		{
			resource = new CloudAttachmentViewItemResource(resource, attachmentResourceService.getViewableResource(
				info, resource.getViewableItem(), attachment));
		}

		ViewItemViewer viewer = resource.getViewer();

		if( viewer == null )
		{
			String viewerId = resource.getDefaultViewerId();
			if( viewerId == null )
			{
				viewerId = viewItemService.getDefaultViewerId(resource.getMimeType());
			}
			if( !Check.isEmpty(viewerId) )
			{
				ResourceViewer resourceViewer = viewItemService.getViewer(viewerId);
				if( resourceViewer != null )
				{
					viewer = resourceViewer.getViewer(info, resource);
				}
			}
		}
		if( viewer != resource.getViewer() )
		{
			resource = new UseViewer(resource, viewer);
		}
		viewer = resource.getViewer();
		if( viewer == null && attachment == null )
		{
			viewer = summarySection;
		}
		model.setActualViewer(viewer);

		model.setResource(resource);
		return resource;
	}

	@Nullable
	private CloudViewableItem getViewableItem(SectionInfo info)
	{
		return info.getAttributeForClass(CloudViewableItem.class);
	}

	public void setAttachment(SectionInfo info, @Nullable String attachmentUuid)
	{
		final RootCloudViewItemModel model = getModel(info);
		model.setAttachmentUuid(attachmentUuid);
	}

	/**
	 * Do not call this. Use CloudItemSectionInfo.getItemInfo instead, otherwise
	 * you will invoke unncessary REST calls
	 */
	@Override
	public CloudItemSectionInfo createCloudItemSectionInfo(SectionInfo info)
	{
		return new CloudItemSectionInfo(getViewableItem(info));
	}

	@Override
	public void processModel(SectionInfo info, ViewItemUrl viewItemUrl)
	{
		RootCloudViewItemModel model = getModel(info);
		int flags = viewItemUrl.getFlags();

		RenderTemplate templateSection = info.lookupSection(RenderTemplate.class);
		if( ((flags & ViewItemUrl.FLAG_IS_RESOURCE) != 0 && ((flags & ViewItemUrl.FLAG_PRESERVE_PARAMS) == 0)) )
		{
			templateSection.setHideNavigation(info, false);
			templateSection.setHideBanner(info, false);
		}
		else if( viewItemUrl.isShowNavOveridden() )
		{
			boolean hidenav = !viewItemUrl.isShowNav();
			templateSection.setHideNavigation(info, hidenav);
			templateSection.setHideBanner(info, hidenav);
		}

		if( (flags & ViewItemUrl.FLAG_IGNORE_SESSION_TEMPLATE) != 0 )
		{
			RootSelectionSection modal = info.lookupSection(RootSelectionSection.class);
			if( modal != null )
			{
				modal.getModel(info).setNoTemplate(true);
			}
		}
	}

	@Override
	public RootCloudViewItemModel instantiateModel(SectionInfo info)
	{
		return new RootCloudViewItemModel();
	}

	public static class CloudBaseViewItemResource implements ViewItemResource
	{
		private final Map<Object, Object> attrs = new HashMap<Object, Object>();
		private final CloudViewableItem viewableItem;
		protected ViewItemResource topLevel;

		public CloudBaseViewItemResource(CloudViewableItem viewableItem)
		{
			this.viewableItem = viewableItem;
			this.topLevel = this;
		}

		@Override
		public ViewableItem getViewableItem()
		{
			return viewableItem;
		}

		@Override
		public String getFilepath()
		{
			return "";
		}

		@Override
		public Set<String> getPrivileges()
		{
			return viewableItem.getPrivileges();
		}

		@Override
		public String getFileDirectoryPath()
		{
			String path = topLevel.getFilepath();
			int ind = path.lastIndexOf('/');
			if( ind == -1 )
			{
				return "";
			}
			return path.substring(0, ind);
		}

		@Override
		public String getFilenameWithoutPath()
		{
			return SectionUtils.getFilenameFromFilepath(topLevel.getFilepath());
		}

		@Override
		public final int getForwardCode()
		{
			return 302;
		}

		@Override
		public void setAttribute(Object key, Object value)
		{
			attrs.put(key, value);
		}

		@Override
		public boolean getBooleanAttribute(Object key)
		{
			Boolean b = (Boolean) attrs.get(key);
			return (b != null && b);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T getAttribute(Object key)
		{
			return (T) attrs.get(key);
		}

		@Override
		public void wrappedBy(ViewItemResource resource)
		{
			this.topLevel = resource;
		}

		@Nullable
		@Override
		public String getMimeType()
		{
			return null;
		}

		@Override
		public Bookmark createCanonicalURL()
		{
			return viewableItem.createStableResourceUrl("");
		}

		@Override
		public ContentStream getContentStream()
		{
			return null;
		}

		@Nullable
		@Override
		public String getDefaultViewerId()
		{
			return null;
		}

		@Override
		public boolean isPathMapped()
		{
			return true;
		}

		@Nullable
		@Override
		public ViewItemViewer getViewer()
		{
			return null;
		}

		@Nullable
		@Override
		public ViewAuditEntry getViewAuditEntry()
		{
			return null;
		}

		@Override
		public boolean isRestrictedResource()
		{
			return false;
		}
	}

	public class CloudAttachmentViewItemResource extends WrappedViewItemResource
	{
		private final ViewableResource viewableResource;

		public CloudAttachmentViewItemResource(ViewItemResource inner, ViewableResource viewableResource)
		{
			super(inner);
			setAttribute(ViewableResource.class, viewableResource);
			this.viewableResource = viewableResource;
		}

		@Override
		public String getFilepath()
		{
			return viewableResource.getFilepath();
		}

		@Override
		public ViewAuditEntry getViewAuditEntry()
		{
			return viewableResource.getViewAuditEntry();
		}

		@Override
		public Bookmark createCanonicalURL()
		{
			return viewableResource.createCanonicalUrl();
		}

		@Override
		public ContentStream getContentStream()
		{
			return viewableResource.getContentStream();
		}

		@Override
		public String getMimeType()
		{
			return viewableResource.getMimeType();
		}

		@Override
		public ViewItemViewer getViewer()
		{
			return null;
		}

		@Override
		public String getDefaultViewerId()
		{
			String viewerId = inner.getDefaultViewerId();
			if( viewerId == null )
			{
				final IAttachment attachment = viewableResource.getAttachment();
				if( attachment == null )
				{
					throw new Error("No attachment in viewableResource");
				}
				viewerId = attachment.getViewer();
			}
			return Check.isEmpty(viewerId) ? null : viewerId;
		}

		@Override
		public boolean isPathMapped()
		{
			return false;
		}
	}

	@NonNullByDefault(false)
	public static class RootCloudViewItemModel extends TwoColumnLayout.TwoColumnModel
	{
		private String attachmentUuid;
		// cached fields
		private ViewItemResource resource;
		private ViewItemViewer actualViewer;

		public String getAttachmentUuid()
		{
			return attachmentUuid;
		}

		public void setAttachmentUuid(String attachmentUuid)
		{
			this.attachmentUuid = attachmentUuid;
		}

		public ViewItemResource getResource()
		{
			return resource;
		}

		public void setResource(ViewItemResource resource)
		{
			this.resource = resource;
		}

		public ViewItemViewer getActualViewer()
		{
			return actualViewer;
		}

		public void setActualViewer(ViewItemViewer actualViewer)
		{
			this.actualViewer = actualViewer;
		}
	}
}
