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

package com.tle.web.viewitem.treeviewer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import com.dytech.edge.exceptions.AttachmentNotFoundException;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.IItem;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.item.attachments.ImsAttachment;
import com.tle.beans.item.attachments.ItemNavigationNode;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.common.PathUtils;
import com.tle.common.filesystem.FileSystemConstants;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.services.FileSystemService;
import com.tle.core.util.ims.IMSNavigationHelper;
import com.tle.core.util.ims.beans.IMSManifest;
import com.tle.ims.service.IMSService;
import com.tle.web.ajax.services.ScormAPIHandler;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.events.ParametersEvent;
import com.tle.web.sections.events.ParametersEventListener;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.result.util.IconLabel;
import com.tle.web.sections.result.util.IconLabel.Icon;
import com.tle.web.sections.standard.Link;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewitem.section.PathMapper.Type;
import com.tle.web.viewitem.section.RootItemFileSection;
import com.tle.web.viewurl.ViewAuditEntry;
import com.tle.web.viewurl.ViewItemResource;
import com.tle.web.viewurl.ViewItemUrl;
import com.tle.web.viewurl.ViewItemUrlFactory;
import com.tle.web.viewurl.ViewItemViewer;

@NonNullByDefault
@Bind
public class TreeNavigationSection extends AbstractTreeViewerSection<TreeNavigationSection.TreeNavigationModel>
	implements
		ParametersEventListener
{
	private static final String TREENAV_JSP = "treenav.jsp";
	public static final String VIEWIMS_JSP = "viewims.jsp";

	@Inject
	private ScormAPIHandler scormAPIHandler;
	@Inject
	private ViewItemUrlFactory itemUrls;
	@Inject
	private IMSService imsService;
	@Inject
	private IMSNavigationHelper navHelper;
	@Inject
	private FileSystemService fileSystem;
	@Inject
	private InstitutionService institutionService;

	@Nullable
	@Override
	public ViewAuditEntry getAuditEntry(SectionInfo info, ViewItemResource resource)
	{
		if( getModel(info).getMethod() != null )
		{
			return null;
		}
		List<Attachment> attachments = resource.getViewableItem().getItem().getAttachments();
		for( Attachment attachment : attachments )
		{
			if( attachment.getAttachmentType() == AttachmentType.IMS )
			{
				return new ViewAuditEntry("ims", attachment.getUrl()); //$NON-NLS-1$
			}

		}
		return null;
	}

	@Override
	protected void registerPathMappings(RootItemFileSection rootSection)
	{
		rootSection.addViewerMapping(Type.FULL, this, VIEWIMS_JSP, TREENAV_JSP);
	}

	@SuppressWarnings("nls")
	@Override
	public void handleParameters(SectionInfo info, ParametersEvent event) throws Exception
	{
		if( "download".equals(event.getParameter("viewMethod", false)) )
		{
			getModel(info).setDownload(true);
		}
	}

	@Override
	public Class<TreeNavigationModel> getModelClass()
	{
		return TreeNavigationModel.class;
	}

	@Override
	protected List<ItemNavigationNode> getTreeNodes(SectionInfo info)
	{
		ViewItemResource resource = getModel(info).getResource();
		ViewableItem<Item> vitem = resource.getViewableItem();
		final Item item = vitem.getItem();
		ImsAttachment ims = new UnmodifiableAttachments(item).getIms();
		if( ims != null && !ims.isExpand() )
		{
			return getTreeNodes(info, Collections.singletonList(ims), false);
		}
		else
		{
			return super.getTreeNodes(info);
		}
	}

	protected List<ItemNavigationNode> getTreeNodes(SectionInfo info, List<? extends IAttachment> attachments,
		boolean scorm)
	{
		List<ItemNavigationNode> treeNodes = new ArrayList<ItemNavigationNode>();
		ViewItemResource resource = getModel(info).getResource();
		ViewableItem<Item> vitem = resource.getViewableItem();

		for( IAttachment attachment : attachments )
		{
			try
			{
				String packageZip = attachment.getUrl();
				IMSManifest imsManifest = imsService.getImsManifest(vitem.getFileHandle(), packageZip, true);
				Item item2 = new Item();
				boolean expand = false;
				if( attachment instanceof ImsAttachment )
				{
					ImsAttachment imsAttach = (ImsAttachment) attachment;
					expand = imsAttach.isExpand();
				}
				navHelper.createTree(imsManifest, item2, vitem.getFileHandle(), packageZip, scorm, expand);
				treeNodes.addAll(item2.getTreeNodes());
			}
			catch( Exception e )
			{
				throw new SectionsRuntimeException(e);
			}
		}

		return treeNodes;
	}

	@Override
	public Collection<String> ensureOnePrivilege()
	{
		return ViewItemViewer.VIEW_ITEM_AND_VIEW_ATTACHMENTS_PRIV;
	}

	@Nullable
	@Override
	public IAttachment getAttachment(SectionInfo info, ViewItemResource resource)
	{
		final ViewableItem<?> vitem = resource.getViewableItem();
		final IItem<?> item = vitem.getItem();
		return new UnmodifiableAttachments(item).getIms();
	}

	@Nullable
	@Override
	public SectionResult view(RenderContext info, ViewItemResource resource)
	{
		final TreeNavigationModel model = getModel(info);
		if( model.isDownload() )
		{
			final ViewableItem<Item> vitem = resource.getViewableItem();
			final Item item = vitem.getItem();
			Attachment ims = new UnmodifiableAttachments(item).getIms();
			final FileHandle handle = resource.getViewableItem().getFileHandle();

			String folder = FileSystemConstants.IMS_FOLDER;
			if( ims == null )
			{
				List<CustomAttachment> scorms = new UnmodifiableAttachments(item).getCustomList("scorm");
				if( scorms.size() > 0 )
				{
					ims = scorms.get(0);
				}

				if( ims == null )
				{
					throw new AttachmentNotFoundException(item.getItemId(), "package");
				}
				final String unadornedFilename = PathUtils.getFilenameFromFilepath(ims.getUrl());

				// I don't think this has ever been found _SCORM, but I can't be
				// sure
				if( fileSystem.fileIsDir(handle, FileSystemService.SCORM_FOLDER) && fileSystem.fileExists(handle,
					PathUtils.filePath(FileSystemService.SCORM_FOLDER, unadornedFilename)) )
				{
					folder = FileSystemService.SCORM_FOLDER;
				}
			}

			// packageZip already has the IMS folder part if it was added with
			// the universal attachment control
			String zipFile = ims.getUrl();
			if( !zipFile.startsWith(FileSystemConstants.IMS_FOLDER)
				|| !zipFile.startsWith(FileSystemService.SCORM_FOLDER) )
			{
				zipFile = folder + '/' + zipFile;
			}
			try
			{
				imsService.ensureIMSPackage(handle, zipFile);
			}
			catch( Exception t )
			{
				SectionUtils.throwRuntime(t);
			}

			info.forwardToUrl(vitem.createStableResourceUrl(zipFile).getHref());

			return null;
		}

		model.setAjaxUrl(institutionService.getInstitutionUrl() + "scorm/"); //$NON-NLS-1$
		return super.view(info, resource);
	}

	@Override
	protected void prepareTitle(SectionInfo info, Link title, BundleLabel itemName, ViewItemResource resource)
	{
		ViewItemUrl url = itemUrls.createItemUrl(info, resource.getViewableItem());
		title.setLabel(info, new IconLabel(Icon.BACK, itemName));
		title.setBookmark(info, url);
	}

	@Override
	protected void viewingNode(ItemNavigationNode node)
	{
		scormAPIHandler.setCurrentIdentifier(node.getIdentifier());
	}

	@NonNullByDefault(false)
	public static class TreeNavigationModel extends AbstractTreeViewerModel
	{
		private String scormurl;
		private String navref;
		private String cookies;
		private String ajaxUrl;
		@Bookmarked
		private boolean download;
		private String attachmentControlId;

		public String getScormurl()
		{
			return scormurl;
		}

		public void setScormurl(String scormurl)
		{
			this.scormurl = scormurl;
		}

		public String getNavref()
		{
			return navref;
		}

		public void setNavref(String navref)
		{
			this.navref = navref;
		}

		public String getCookies()
		{
			return cookies;
		}

		public void setCookies(String cookies)
		{
			this.cookies = cookies;
		}

		public String getAjaxUrl()
		{
			return ajaxUrl;
		}

		public void setAjaxUrl(String ajaxUrl)
		{
			this.ajaxUrl = ajaxUrl;
		}

		public boolean isDownload()
		{
			return download;
		}

		public void setDownload(boolean download)
		{
			this.download = download;
		}

		public String getAttachmentControlId()
		{
			return attachmentControlId;
		}

		public void setAttachmentControlId(String attachmentControlId)
		{
			this.attachmentControlId = attachmentControlId;
		}
	}
}
