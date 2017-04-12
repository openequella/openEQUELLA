package com.tle.web.controls.universal.handlers.fileupload.packages;

import javax.inject.Singleton;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.common.Check;
import com.tle.core.filesystem.StagingFile;
import com.tle.core.guice.Bind;
import com.tle.core.mimetypes.MimeTypeConstants;
import com.tle.core.services.FileSystemService;
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
@NonNullByDefault
@Bind
@Singleton
public class ScormPackageAttachmentHandler extends AbstractPackageAttachmentHandler
{
	private static final String SCORM_PACKAGE_TYPE = "SCORM";
	private static final String KEY_SCORM_VERSION = "SCORM_VERSION";

	@PlugKey("handlers.file.packageoptions.asscorm")
	private static Label LABEL_TREAT_AS;

	static
	{
		PluginResourceHandler.init(ScormPackageAttachmentHandler.class);
	}

	@Override
	public Attachment createAttachment(SectionInfo info, UploadedFile file, PackageDetails packageDetailsSection,
		boolean real)
	{
		final CustomAttachment attachment = new CustomAttachment();
		attachment.setType("scorm");
		attachment.setData("fileSize", file.getSize());

		if( real )
		{
			final WebRepository repo = packageDetailsSection.getFileUploadHandler().getDialogState().getRepository();
			final String packageExtractedFolder = extractPackage(repo, file);
			final PackageInfo pkgInfo = readPackage(info, repo, packageExtractedFolder);
			final String name = pkgInfo.getTitle();
			if( name != null )
			{
				attachment.setDescription(name);
			}
			attachment.setData(KEY_SCORM_VERSION, pkgInfo.getScormVersion());
		}

		return attachment;
	}

	@Override
	public String getMimeType(SectionInfo info, UploadedFile file, String packageType)
	{
		if( packageType.equals(SCORM_PACKAGE_TYPE) )
		{
			return MimeTypeConstants.MIME_SCORM;
		}
		throw new Error("Unknown package type " + packageType);
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
		// Nothing
	}

	@Override
	public void commitNew(SectionInfo info, UploadedFile uploadedFile, PackageDetails packageDetailsSection,
		String replacementUuid)
	{
		FileUploadHandler handler = packageDetailsSection.getFileUploadHandler();
		Attachment attachment = uploadedFile.getAttachment();
		WebRepository repo = handler.getDialogState().getRepository();

		String packageFilename = uploadedFile.getFilename();
		String stagingPath = uploadedFile.getFilepath();
		String extractedPath = uploadedFile.getExtractedPath();

		attachment.setUrl(packageFilename);
		StagingFile staging = handler.getStagingFile();
		move(staging, stagingPath, FileSystemService.IMS_FOLDER + '/' + packageFilename);
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
		// Nothing?
	}
}
