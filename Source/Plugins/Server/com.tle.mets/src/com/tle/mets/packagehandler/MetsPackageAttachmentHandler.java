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

package com.tle.mets.packagehandler;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.common.Check;
import com.tle.common.PathUtils;
import com.tle.core.filesystem.StagingFile;
import com.tle.core.guice.Bind;
import com.tle.mets.metsimport.METSTreeBuilder;
import com.tle.web.controls.universal.UniversalControlState;
import com.tle.web.controls.universal.handlers.FileUploadHandler;
import com.tle.web.controls.universal.handlers.fileupload.AbstractPackageAttachmentHandler;
import com.tle.web.controls.universal.handlers.fileupload.UploadedFile;
import com.tle.web.controls.universal.handlers.fileupload.details.PackageDetails;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.wizard.PackageTreeBuilder.PackageInfo;
import com.tle.web.wizard.impl.WebRepository;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@NonNullByDefault
@Bind
@Singleton
public class MetsPackageAttachmentHandler extends AbstractPackageAttachmentHandler
{
	private static final String PACKAGE_TYPE = "METS";

	@PlugKey("handlers.file.packageoptions.aspackage")
	private static Label LABEL_TREAT_AS;
	static
	{
		PluginResourceHandler.init(MetsPackageAttachmentHandler.class);
	}

	@Inject
	private METSTreeBuilder metsTreeBuilder;

	@Override
	public Attachment createAttachment(SectionInfo info, UploadedFile file, PackageDetails packageDetailsSection,
		boolean real)
	{
		final CustomAttachment imsAttachment = new CustomAttachment();
		imsAttachment.setType("mets");
		// imsAttachment.setSize((int) file.getSize());
		// imsAttachment.setExpand(false);

		if( real )
		{
			final WebRepository repo = packageDetailsSection.getFileUploadHandler().getDialogState().getRepository();
			final String packageExtractedFolder;
			if( repo.isArchive(file.getFilename()) )
			{
				packageExtractedFolder = extractPackage(repo, file);
			}
			else
			{
				packageExtractedFolder = file.getFilepath();
			}
			final PackageInfo pkgInfo = readPackage(info, repo, packageExtractedFolder);
			final String name = pkgInfo.getTitle();
			if( name != null )
			{
				imsAttachment.setDescription(name);
			}
		}

		return imsAttachment;
	}

	@Override
	public String getMimeType(SectionInfo info, UploadedFile file, String packageType)
	{
		if( packageType.equals(PACKAGE_TYPE) )
		{
			// return MimeTypeConstants.MIME_IMS;
			return "equella/mets-package";
		}
		throw new RuntimeException("Unknown package type " + packageType);
	}

	@Override
	public Label getTreatAsLabel(SectionInfo info, UploadedFile file, String packageType)
	{
		return LABEL_TREAT_AS;
	}

	@Override
	public void saveDetailsToAttachment(SectionInfo info, UploadedFile uploadedFile,
		PackageDetails packageDetailsSection, Attachment attachment)
	{
		// No details to save
	}

	@Override
	public void commitNew(SectionInfo info, UploadedFile uploadedFile, PackageDetails packageDetailsSection,
		String replacementUuid)
	{
		FileUploadHandler handler = packageDetailsSection.getFileUploadHandler();
		Attachment attachment = uploadedFile.getAttachment();
		StagingFile staging = handler.getStagingFile();

		UniversalControlState dialogState = handler.getDialogState();
		WebRepository repo = dialogState.getRepository();

		String packageFilename = uploadedFile.getFilename();
		String stagingPath = uploadedFile.getFilepath();
		String extractedPath = uploadedFile.getExtractedPath();
		metsTreeBuilder.createTree((Item) dialogState.getViewableItem(info).getItem(), staging, extractedPath,
			stagingPath, packageFilename, false);

		final String newLoc = PathUtils.filePath("_METS", packageFilename);
		attachment.setUrl(newLoc);
		move(staging, stagingPath, newLoc);
		if( !Check.isEmpty(extractedPath) )
		{
			move(staging, extractedPath, packageFilename);
		}
		repo.getState().getWizardMetadataMapper().setPackageExtractedFolder(packageFilename);
	}

	@Override
	public void commitEdit(SectionInfo info, UploadedFile uploadedFile, PackageDetails packageDetailsSection,
		Attachment attachment)
	{
		// Nothing to edit
	}
}
