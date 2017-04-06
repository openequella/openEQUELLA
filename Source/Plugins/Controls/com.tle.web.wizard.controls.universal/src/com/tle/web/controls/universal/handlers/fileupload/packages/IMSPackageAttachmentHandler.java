package com.tle.web.controls.universal.handlers.fileupload.packages;

import java.io.IOException;

import javax.inject.Singleton;

import com.google.common.base.Throwables;
import com.tle.annotation.NonNullByDefault;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.ImsAttachment;
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
@SuppressWarnings("nls")
@NonNullByDefault
@Bind
@Singleton
public class IMSPackageAttachmentHandler extends AbstractPackageAttachmentHandler
{
	public static final String PACKAGE_TYPE = "IMS";

	@PlugKey("handlers.file.packageoptions.aspackage")
	private static Label LABEL_TREAT_AS;

	static
	{
		PluginResourceHandler.init(IMSPackageAttachmentHandler.class);
	}

	@Override
	public Attachment createAttachment(SectionInfo info, UploadedFile file, PackageDetails packageDetailsSection,
		boolean real)
	{
		final ImsAttachment imsAttachment = new ImsAttachment();
		imsAttachment.setSize(file.getSize());
		imsAttachment.setExpand(false);

		if( real )
		{
			final WebRepository repo = packageDetailsSection.getFileUploadHandler().getDialogState().getRepository();
			final String packageExtractedFolder = extractPackage(repo, file);
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
			return MimeTypeConstants.MIME_IMS;
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
		final String resolvedSubType = uploadedFile.getResolvedSubType();
		if( resolvedSubType == null || resolvedSubType.equals("IMS") )
		{
			ImsAttachment imsAttachment = (ImsAttachment) attachment;

			boolean newExpand = packageDetailsSection.isExpand(info);
			packageDetailsSection.setExpandChanged(info, imsAttachment.isExpand() != newExpand);
			imsAttachment.setExpand(newExpand);
		}
	}

	@Override
	public void commitNew(SectionInfo info, UploadedFile uploadedFile, PackageDetails packageDetailsSection,
		String replacementUuid)
	{
		FileUploadHandler handler = packageDetailsSection.getFileUploadHandler();
		Attachment attachment = uploadedFile.getAttachment();
		StagingFile staging = handler.getStagingFile();
		boolean expand = false;
		if( attachment instanceof ImsAttachment )
		{
			ImsAttachment imsAttachment = (ImsAttachment) attachment;
			expand = imsAttachment.isExpand();
		}

		WebRepository repo = handler.getDialogState().getRepository();
		try
		{
			String packageExtractedFolder = uploadedFile.getFilename();
			String stagingPath = uploadedFile.getFilepath();
			String extractedPath = uploadedFile.getExtractedPath();
			if( expand )
			{
				repo.createPackageNavigation(info, extractedPath, stagingPath, packageExtractedFolder, true);
			}
			attachment.setUrl(packageExtractedFolder);
			move(staging, stagingPath, FileSystemService.IMS_FOLDER + '/' + packageExtractedFolder);
			if( !Check.isEmpty(extractedPath) )
			{
				move(staging, extractedPath, packageExtractedFolder);
			}
			repo.getState().getWizardMetadataMapper().setPackageExtractedFolder(packageExtractedFolder);
		}
		catch( IOException e )
		{
			throw Throwables.propagate(e);
		}
	}

	@Override
	public void commitEdit(SectionInfo info, UploadedFile uploadedFile, PackageDetails packageDetailsSection,
		Attachment attachment)
	{
		if( packageDetailsSection.isExpandChanged(info) )
		{
			ImsAttachment imsAttachment = (ImsAttachment) attachment;
			FileUploadHandler handler = packageDetailsSection.getFileUploadHandler();
			WebRepository repo = handler.getDialogState().getRepository();
			if( imsAttachment.isExpand() )
			{
				try
				{
					String packageFilename = imsAttachment.getUrl();
					repo.createPackageNavigation(info, packageFilename, FileSystemService.IMS_FOLDER + '/'
						+ packageFilename, packageFilename, true);
				}
				catch( IOException e )
				{
					throw Throwables.propagate(e);
				}
			}
			else
			{
				packageDetailsSection.removePackageResources();
			}
		}
	}
}
