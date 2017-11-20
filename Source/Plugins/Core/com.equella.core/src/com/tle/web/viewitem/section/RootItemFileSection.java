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

package com.tle.web.viewitem.section;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import com.google.inject.Provider;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.core.institution.InstitutionService;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.core.plugins.AbstractPluginService;
import com.tle.core.services.FileSystemService;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.integration.Integration;
import com.tle.web.integration.IntegrationSessionData;
import com.tle.web.integration.service.IntegrationService;
import com.tle.web.login.LogonSection;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.DirectEvent;
import com.tle.web.sections.events.BeforeEventsListener;
import com.tle.web.sections.events.ForwardEventListener;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.SectionEvent;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.generic.NumberOrderComparator;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.section.RootSelectionSection;
import com.tle.web.stream.ContentStream;
import com.tle.web.template.RenderTemplate;
import com.tle.web.viewable.NewDefaultViewableItem;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewable.servlet.ItemServlet;
import com.tle.web.viewitem.DRMFilter;
import com.tle.web.viewitem.ViewItemAuditor;
import com.tle.web.viewitem.info.DefaultItemFileInfo;
import com.tle.web.viewitem.section.PathMapper.Type;
import com.tle.web.viewurl.ItemSectionInfo;
import com.tle.web.viewurl.ItemSectionInfo.ItemSectionInfoFactory;
import com.tle.web.viewurl.ResourceViewer;
import com.tle.web.viewurl.UseViewer;
import com.tle.web.viewurl.ViewAuditEntry;
import com.tle.web.viewurl.ViewItemFilter;
import com.tle.web.viewurl.ViewItemResource;
import com.tle.web.viewurl.ViewItemService;
import com.tle.web.viewurl.ViewItemUrl;
import com.tle.web.viewurl.ViewItemUrlProcessor;
import com.tle.web.viewurl.ViewItemViewer;

@NonNullByDefault
public class RootItemFileSection extends AbstractPrototypeSection<RootItemFileSection.RootItemFileModel>
	implements
		HtmlRenderer,
		ItemSectionInfoFactory,
		ViewItemUrlProcessor,
		ForwardEventListener,
		BeforeEventsListener
{
	protected static final String KEY_PFX = AbstractPluginService.getMyPluginId(RootItemFileSection.class)+".";

	@Inject
	private SelectionService selectionService;
	@Inject
	private IntegrationService integrationService;
	@Inject
	private MimeTypeService mimeService;
	@Inject
	private ViewItemService viewItemService;
	@Inject
	private ViewItemAuditor auditor;
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private InstitutionService institutionService;
	@Inject
	private Provider<DefaultItemFileInfo> itemInfoProvider;

	private final PathMapper<ViewItemViewer> pathMappedViewers = new PathMapper<ViewItemViewer>();
	private final List<ViewItemFilter> filterViewers = new ArrayList<ViewItemFilter>();
	private final OrderedPathMapper<ViewItemFilter> pathMappedFilters = new OrderedPathMapper<ViewItemFilter>(
		NumberOrderComparator.HIGHEST_FIRST);
	private ViewItemFilter fallbackFilter;

	@Override
	public ItemSectionInfo getItemSectionInfo(SectionInfo info)
	{
		DefaultItemFileInfo itemInfo = itemInfoProvider.get();
		itemInfo.setViewableItem(getViewableItem(info));
		return itemInfo;
	}

	@Nullable
	public ViewableItem<Item> getViewableItem(SectionInfo info)
	{
		return info.getAttribute(ItemServlet.VIEWABLE_ITEM);
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
		RootItemFileModel model = getModel(info);
		ViewItemResource resource = model.getResource();
		if( resource != null )
		{
			return resource;
		}
		ViewableItem<?> vitem = getViewableItem(info);
		if( vitem == null )
		{
			// we are only generating urls
			return null;
		}
		String filename = model.getFilename();
		String mimeType = "equella/summary"; //$NON-NLS-1$
		if( filename.length() > 0 )
		{
			mimeType = mimeService.getMimeTypeForFilename(filename);
		}
		resource = new BaseViewItemResource(getViewableItem(info), filename, mimeType, model.getViewer());

		for( ViewItemFilter filter : filterViewers )
		{
			resource = filter.filter(info, resource);
			if( resource == null )
			{
				return null;
			}
		}
		ViewItemViewer viewer;
		if( resource.isPathMapped() )
		{
			viewer = pathMappedViewers.getMapping(filename, resource.getMimeType());
			if( viewer == null )
			{
				resource = fallbackFilter.filter(info, resource);
				if( resource == null )
				{
					return null;
				}
				viewer = resource.getViewer();
			}
		}
		else
		{
			viewer = resource.getViewer();
		}
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

		Collection<ViewItemFilter> filters = pathMappedFilters.getMatchingFilters(filename, mimeType);
		for( ViewItemFilter filter : filters )
		{
			resource = filter.filter(info, resource);
			if( resource == null )
			{
				return null;
			}
		}
		viewer = resource.getViewer();
		model.setActualViewer(viewer);
		if( viewer instanceof ResourceViewerAware )
		{
			((ResourceViewerAware) viewer).beforeRender(info, resource);
		}
		model.setResource(resource);
		return resource;
	}

	public ViewItemResource getBaseViewItemResource(SectionInfo info)
	{
		RootItemFileModel model = getModel(info);
		ViewableItem<Item> vitem = getViewableItem(info);
		if( vitem == null )
		{
			// we are only generating urls
			return null;
		}
		String filename = model.getFilename();
		String mimeType = "equella/summary"; //$NON-NLS-1$
		if( filename.length() > 0 )
		{
			mimeType = mimeService.getMimeTypeForFilename(filename);
		}
		return getBaseViewItemResource(vitem, filename, mimeType, model.getViewer());
	}

	private ViewItemResource getBaseViewItemResource(ViewableItem<Item> viewableItem, String filename, String mimeType,
		String viewer)
	{
		return new BaseViewItemResource(viewableItem, filename, mimeType, viewer);
	}

	@Nullable
	@Override
	public SectionResult renderHtml(RenderEventContext info) throws IOException
	{
		RootItemFileModel model = getModel(info);
		ViewItemViewer viewer = model.getActualViewer();
		ViewItemResource resource = model.getResource();
		try
		{
			ViewableItem<?> viewableItem = resource.getViewableItem();
			checkRestrictedResource(info, resource, viewer);
			if( viewer == null )
			{
				ensureOnePrivilege(resource.getPrivileges(), ViewItemViewer.VIEW_ITEM_AND_VIEW_ATTACHMENTS_PRIV);
				ViewAuditEntry vae = resource.getViewAuditEntry();
				if( viewableItem.isItemForReal() && vae != null )
				{
					auditor.audit(vae, viewableItem.getItemId());
				}
				info.forwardToUrl(resource.createCanonicalURL().getHref(), resource.getForwardCode());
				return null;
			}
			ensureOnePrivilege(resource.getPrivileges(), viewer.ensureOnePrivilege());
			if( viewableItem.isItemForReal() )
			{
				auditor.audit(viewer.getAuditEntry(info, resource), viewableItem.getItemId());
			}
			return viewer.view(info, resource);
		}
		catch( AccessDeniedException ade )
		{
			if( CurrentUser.isGuest() )
			{
				LogonSection.forwardToLogon(info,
					institutionService.removeInstitution(info.getPublicBookmark().getHref()),
					LogonSection.STANDARD_LOGON_PATH);
				return null;
			}
			throw ade;
		}
	}

	private void checkRestrictedResource(SectionInfo info, ViewItemResource resource, @Nullable ViewItemViewer viewer)
	{
		boolean checkRestricted = false;
		if( resource.isRestrictedResource() )
		{
			checkRestricted = true;
		}
		else if( viewer != null )
		{
			final IAttachment attachment = viewer.getAttachment(info, resource);
			if( attachment != null && attachment.isRestricted() )
			{
				checkRestricted = true;
			}
		}
		if( checkRestricted )
		{
			ensureOnePrivilege(resource.getPrivileges(), ViewItemViewer.VIEW_RESTRICTED_ATTACHMENTS);
		}
	}

	public boolean isForPreview(SectionInfo info)
	{
		RootItemFileModel model = getModel(info);
		return model.isForPreview();
	}

	public void setActualViewer(SectionInfo info, ViewItemViewer actualViewer)
	{
		getModel(info).setActualViewer(actualViewer);
	}

	public boolean isInIntegration(SectionInfo info)
	{
		return integrationService.isInIntegrationSession(info);
	}

	public static void ensureOnePrivilege(@Nullable Set<String> privs, @Nullable Collection<String> required)
	{
		if( required == null || required.isEmpty() )
		{
			return;
		}

		for( String reqPriv : required )
		{
			if( privs != null && privs.contains(reqPriv) )
			{
				return;
			}
		}

		StringBuilder sbuf = new StringBuilder();
		boolean first = true;
		for( String priv : required )
		{
			if( !first )
			{
				sbuf.append(", "); //$NON-NLS-1$
			}
			sbuf.append(priv);
			first = false;
		}
		throw new AccessDeniedException(CurrentLocale.get(   KEY_PFX +"viewitem.missingprivileges", sbuf.toString())); //$NON-NLS-1$
	}

	@Override
	public String getDefaultPropertyName()
	{
		return ""; //$NON-NLS-1$
	}

	@Override
	public Class<RootItemFileModel> getModelClass()
	{
		return RootItemFileModel.class;
	}

	@NonNullByDefault(false)
	public static class RootItemFileModel
	{
		@Bookmarked(stateful = false, rendered = true)
		private String filename = ""; //$NON-NLS-1$
		@Bookmarked(name = "hn", stateful = false)
		private boolean hideNav;
		@Bookmarked(name = "vi")
		private String viewer;
		@Bookmarked(name = "fp")
		private boolean forPreview;
		private ViewItemResource resource;
		private ViewItemViewer actualViewer;
		private String itemdir;
		private boolean popup;

		public boolean isPopup()
		{
			return popup;
		}

		public void setPopup(boolean popup)
		{
			this.popup = popup;
		}

		public String getItemdir()
		{
			return itemdir;
		}

		public void setItemdir(String itemdir)
		{
			this.itemdir = itemdir;
		}

		public String getFilename()
		{
			return filename;
		}

		public void setFilename(String filename)
		{
			this.filename = filename;
		}

		public String getViewer()
		{
			return viewer;
		}

		public void setViewer(String viewer)
		{
			this.viewer = viewer;
		}

		public boolean isForPreview()
		{
			return forPreview;
		}

		public void setForPreview(boolean forPreview)
		{
			this.forPreview = forPreview;
		}

		public boolean isHideNav()
		{
			return hideNav;
		}

		public void setHideNav(boolean hideNav)
		{
			this.hideNav = hideNav;
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

	@Override
	public void processModel(SectionInfo info, ViewItemUrl viewItemUrl)
	{
		RootItemFileModel model = getModel(info);
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
		if( (flags & ViewItemUrl.FLAG_FOR_PREVIEW) != 0 )
		{
			model.setForPreview(true);
			selectionService.disableSelection(info);
		}

		if( (flags & ViewItemUrl.FLAG_IGNORE_SESSION_TEMPLATE) != 0 )
		{
			RootSelectionSection modal = info.lookupSection(RootSelectionSection.class);
			if( modal != null )
			{
				modal.getModel(info).setNoTemplate(true);
			}
		}

		model.setViewer(viewItemUrl.getViewer());
		if( viewItemUrl.isSkipDrm() )
		{
			DRMFilter drmFilter = info.lookupSection(DRMFilter.class);
			if( drmFilter != null )
			{
				drmFilter.setSkip(info, true);
			}
		}
	}

	@Override
	public void forwardCreated(SectionInfo info, SectionInfo forward)
	{
		RootItemFileModel srcModel = getModel(info);

		RootItemFileSection destSection = forward.lookupSection(RootItemFileSection.class);
		if( destSection != null )
		{
			RootItemFileModel destModel = getModel(forward);
			destModel.setForPreview(srcModel.isForPreview());
		}
	}

	public void setFallbackMapping(ViewItemFilter fallback)
	{
		this.fallbackFilter = fallback;
	}

	public void addFilterViewer(ViewItemFilter filter)
	{
		filterViewers.add(filter);
	}

	public void addViewerMapping(Type type, ViewItemViewer viewer, String... paths)
	{
		for( String path : paths )
		{
			pathMappedViewers.addMapping(type, path, viewer);
		}
	}

	public void addFilterMapping(Type type, ViewItemFilter viewer, String... paths)
	{
		if( type == Type.ALWAYS )
		{
			pathMappedFilters.addMapping(type, null, viewer);
		}
		else
		{
			for( String path : paths )
			{
				pathMappedFilters.addMapping(type, path, viewer);
			}
		}
	}

	@Override
	public void beforeEvents(SectionInfo info)
	{
		RootItemFileModel model = getModel(info);
		String filename = model.getFilename();
		if( filename != null )
		{
			filename = filename.toLowerCase();
		}
		boolean viewitem = (filename == null || filename.equals("viewitem.jsp") || filename.equals("viewpopup.jsp"));
		if( viewitem || model.isHideNav() )
		{
			RenderTemplate templateSection = info.lookupSection(RenderTemplate.class);
			templateSection.setHideNavigation(info, true);
			templateSection.setHideBanner(info, true);
			if( viewitem )
			{
				model.setFilename(""); //$NON-NLS-1$
			}
		}
		if( integrationService.getSessionData(info) != null )
		{
			return;
		}
		ViewableItem<?> vitem = getViewableItem(info);
		if( vitem instanceof NewDefaultViewableItem )
		{
			NewDefaultViewableItem ndvi = (NewDefaultViewableItem) vitem;
			if( ndvi.getIntegrationType() != null )
			{
				Integration<? extends IntegrationSessionData> iService = integrationService
					.getIntegrationServiceForId(ndvi.getIntegrationType());
				integrationService.setupSessionData(info, iService.createDataForViewing(info));
			}
		}
	}

	public class BaseViewItemResource implements ViewItemResource
	{
		private final Map<Object, Object> attrs = new HashMap<Object, Object>();
		private final ViewableItem<?> viewableItem;
		private final String filepath;
		private final String mimeType;
		private final String viewerId;
		protected ViewItemResource topLevel;

		public BaseViewItemResource(ViewableItem<?> viewableItem, String filepath, String mimeType, String viewerId)
		{
			this.viewableItem = viewableItem;
			this.filepath = filepath;
			this.mimeType = mimeType;
			this.viewerId = viewerId;
			this.topLevel = this;
		}

		@Override
		public ViewableItem<?> getViewableItem()
		{
			return viewableItem;
		}

		@Override
		public String getFilepath()
		{
			return filepath;
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
				return ""; //$NON-NLS-1$
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

		@Override
		public String getMimeType()
		{
			return mimeType;
		}

		@Override
		public Bookmark createCanonicalURL()
		{
			return viewableItem.createStableResourceUrl(filepath);
		}

		@Override
		public ContentStream getContentStream()
		{
			return fileSystemService.getContentStream(topLevel.getViewableItem().getFileHandle(),
				topLevel.getFilepath(), topLevel.getMimeType());
		}

		@Nullable
		@Override
		public String getDefaultViewerId()
		{
			return viewerId;
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
}
