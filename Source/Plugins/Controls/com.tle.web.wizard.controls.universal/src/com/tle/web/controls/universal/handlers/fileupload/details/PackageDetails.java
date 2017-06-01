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

package com.tle.web.controls.universal.handlers.fileupload.details;

import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.tle.annotation.NonNullByDefault;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.item.attachments.ImsAttachment;
import com.tle.beans.item.attachments.ModifiableAttachments;
import com.tle.common.NameValue;
import com.tle.common.collection.AttachmentConfigConstants;
import com.tle.core.filesystem.StagingFile;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.security.TLEAclManager;
import com.tle.core.services.FileSystemService;
import com.tle.web.controls.universal.DialogRenderOptions;
import com.tle.web.controls.universal.handlers.FileUploadHandler;
import com.tle.web.controls.universal.handlers.fileupload.PackageAttachmentHandler;
import com.tle.web.controls.universal.handlers.fileupload.UploadedFile;
import com.tle.web.controls.universal.handlers.fileupload.packages.IMSPackageAttachmentHandler;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.listmodel.EnumListModel;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.wizard.impl.WebRepository;

@Bind
@NonNullByDefault
@SuppressWarnings("nls")
public class PackageDetails extends AbstractDetailsEditor<PackageDetails.PackageDetailsModel>
{
	@PlugKey("handlers.file.packagedetails.")
	private static String KEY_PFXBUTTON;

	enum ExpandType
	{
		SINGLE, EXPAND
	}

	@Component
	private SingleSelectionList<ExpandType> expandButtons;
	@Component
	private SingleSelectionList<NameValue> viewers;

	@Inject
	private PluginTracker<PackageAttachmentHandler> packageHandlersTracker;
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private TLEAclManager aclManager;

	@Override
	public PackageDetailsModel instantiateModel(SectionInfo info)
	{
		return new PackageDetailsModel();
	}

	@Override
	public boolean isShowViewLink()
	{
		return false;
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		expandButtons.setListModel(new EnumListModel<ExpandType>(KEY_PFXBUTTON, true, ExpandType.values()));
		expandButtons.setAlwaysSelect(true);
	}

	@Override
	public void onRegister(SectionTree tree, String parentId, FileUploadHandler handler)
	{
		super.onRegister(tree, parentId, handler);
		viewers.setListModel(handler.createViewerModel());
	}

	@Override
	public SectionRenderable renderDetailsEditor(RenderContext context, DialogRenderOptions renderOptions,
		UploadedFile uploadedFile)
	{
		PackageDetailsModel model = getModel(context);
		model.setEditTitle(new TextLabel(displayName.getValue(context)));
		model.setShowRestrict(!aclManager.filterNonGrantedPrivileges(AttachmentConfigConstants.RESTRICT_ATTACHMENTS)
			.isEmpty());
		model.setShowPreview(getFileUploadHandler().getFileSettings().isAllowPreviews());
		model.setShowViewers(!(viewers.getListModel().getOptions(context).size() == 2));
		model.setShowExpandButtons(uploadedFile.getResolvedSubType().equals(IMSPackageAttachmentHandler.PACKAGE_TYPE));
		return viewFactory.createResult("file/file-packageedit.ftl", this);
	}

	@Override
	public void initialiseFromUpload(SectionInfo info, UploadedFile uploadedFile, boolean resolved)
	{
		final Attachment attachment = getPackageHandler(uploadedFile).createAttachment(info, uploadedFile, this,
			resolved);
		if( attachment.getUrl() == null )
		{
			attachment.setUrl(uploadedFile.getFilepath());
		}
		if( attachment.getDescription() == null )
		{
			attachment.setDescription(uploadedFile.getFilename());
		}
		attachment.setMd5sum(uploadedFile.getMd5());
		uploadedFile.setAttachment(attachment);
	}

	@Override
	public void prepareForEdit(SectionInfo info, UploadedFile uploadedFile)
	{
		Attachment attachment = uploadedFile.getAttachment();
		AttachmentType attachmentType = attachment.getAttachmentType();
		if( attachmentType == AttachmentType.IMS )
		{
			uploadedFile.setResolvedType(FileUploadHandler.FILE_TYPE_PACKAGE);
			uploadedFile.setResolvedSubType(IMSPackageAttachmentHandler.PACKAGE_TYPE);
		}
		else if( attachmentType == AttachmentType.CUSTOM )
		{
			// FIXME: plugin point... need to reverse an attachment into an
			// uploadedFile
			CustomAttachment ca = (CustomAttachment) attachment;
			if( ca.getType().equals("scorm") )
			{
				uploadedFile.setResolvedType(FileUploadHandler.FILE_TYPE_PACKAGE);
				uploadedFile.setResolvedSubType("SCORM");
			}
			else if( ca.getType().equals("qtitest") )
			{
				uploadedFile.setResolvedType(FileUploadHandler.FILE_TYPE_PACKAGE);
				uploadedFile.setResolvedSubType("QTITEST");
			}
			else if( ca.getType().equals("mets") )
			{
				uploadedFile.setResolvedType(FileUploadHandler.FILE_TYPE_PACKAGE);
				uploadedFile.setResolvedSubType("METS");
			}
		}
	}

	@Override
	public void cleanup(SectionInfo info, UploadedFile uploadedFile)
	{
		// no need
	}

	@Override
	public void removeAttachment(SectionInfo info, Attachment attachment, boolean willBeReplaced)
	{
		super.removeAttachment(info, attachment, willBeReplaced);
		removePackageResources();
		StagingFile stagingFile = getFileUploadHandler().getStagingFile();
		String packageName = attachment.getUrl();
		fileSystemService.removeFile(stagingFile, packageName);
		// Only supports one package
		// FIXME: plugin point
		fileSystemService.removeFile(stagingFile, FileSystemService.IMS_FOLDER);
		fileSystemService.removeFile(stagingFile, FileSystemService.SCORM_FOLDER);
		fileSystemService.removeFile(stagingFile, FileSystemService.SECURE_FOLDER);
	}

	// Does not handle multiple packages
	public void removePackageResources()
	{
		List<Attachment> toRemove = Lists.newArrayList();
		WebRepository repo = getFileUploadHandler().getDialogState().getRepository();
		final ModifiableAttachments attachments = repo.getAttachments();
		Iterator<IAttachment> iter = attachments.iterator();
		while( iter.hasNext() )
		{
			IAttachment attachment = iter.next();
			AttachmentType type = attachment.getAttachmentType();
			// FIXME: plugin point
			switch( type )
			{
				case IMSRES:
				case CUSTOM:
					if( type == AttachmentType.CUSTOM )
					{
						CustomAttachment custom = (CustomAttachment) attachment;
						if( !custom.getType().equals("scormres") )
						{
							break;
						}
					}
					toRemove.add((Attachment) attachment);
					break;

				default:
					break;
			}
		}
		repo.removeNavigationNodes(toRemove);
		repo.getAttachments().removeAll(toRemove);
	}

	@Override
	public void setupDetailsForEdit(SectionInfo info, UploadedFile uploadedFile)
	{
		super.setupDetailsForEdit(info, uploadedFile);
		Attachment attachment = uploadedFile.getAttachment();
		viewers.setSelectedStringValue(info, attachment.getViewer());
		final String resolvedSubType = uploadedFile.getResolvedSubType();
		if( resolvedSubType == null || resolvedSubType.equals("IMS") )
		{
			ImsAttachment imsAttachment = (ImsAttachment) attachment;
			expandButtons.setSelectedValue(info, imsAttachment.isExpand() ? ExpandType.EXPAND : ExpandType.SINGLE);
		}
	}

	@Override
	public void saveDetailsToAttachment(SectionInfo info, UploadedFile uploadedFile, Attachment attachment)
	{
		super.saveDetailsToAttachment(info, uploadedFile, attachment);
		getPackageHandler(uploadedFile).saveDetailsToAttachment(info, uploadedFile, this, attachment);

		attachment.setViewer(viewers.getSelectedValueAsString(info));
	}

	@Override
	public void commitNew(SectionInfo info, UploadedFile uploadedFile, String replacementUuid)
	{
		super.commitNew(info, uploadedFile, replacementUuid);
		Attachment attachment = uploadedFile.getAttachment();
		if( uploadedFile.isDetailEditing() )
		{
			saveDetailsToAttachment(info, uploadedFile, attachment);
		}

		getPackageHandler(uploadedFile).commitNew(info, uploadedFile, this, replacementUuid);
	}

	@Override
	public void commitEdit(SectionInfo info, UploadedFile uploadedFile, Attachment attachment)
	{
		saveDetailsToAttachment(info, uploadedFile, attachment);

		getPackageHandler(uploadedFile).commitEdit(info, uploadedFile, this, attachment);
	}

	private PackageAttachmentHandler getPackageHandler(UploadedFile file)
	{
		String packageType = file.getResolvedSubType();
		if( packageType == null )
		{
			packageType = IMSPackageAttachmentHandler.PACKAGE_TYPE;
		}
		final PackageAttachmentHandler packageAttachmentHandler = packageHandlersTracker.getBeanMap().get(packageType);
		if( packageAttachmentHandler == null )
		{
			throw new Error("No package handler for package type " + packageType);
		}
		return packageAttachmentHandler;
	}

	public boolean isExpand(SectionInfo info)
	{
		return expandButtons.getSelectedValue(info) == ExpandType.EXPAND;
	}

	public void setExpandChanged(SectionInfo info, boolean changed)
	{
		getModel(info).setExpandChanged(changed);
	}

	public boolean isExpandChanged(SectionInfo info)
	{
		return getModel(info).isExpandChanged();
	}

	public SingleSelectionList<ExpandType> getExpandButtons()
	{
		return expandButtons;
	}

	public SingleSelectionList<NameValue> getViewers()
	{
		return viewers;
	}

	public static class PackageDetailsModel extends AbstractDetailsEditor.Model
	{
		private boolean showExpandButtons;
		private boolean expandChanged;

		public boolean isShowExpandButtons()
		{
			return showExpandButtons;
		}

		public void setShowExpandButtons(boolean showExpandButtons)
		{
			this.showExpandButtons = showExpandButtons;
		}

		public boolean isExpandChanged()
		{
			return expandChanged;
		}

		public void setExpandChanged(boolean expandChanged)
		{
			this.expandChanged = expandChanged;
		}
	}
}
