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

package com.tle.web.qti.packagehandler;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.base.Throwables;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.common.Check;
import com.tle.common.PathUtils;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.core.guice.Bind;
import com.tle.core.qti.QtiConstants;
import com.tle.core.qti.beans.QtiTestDetails;
import com.tle.core.qti.service.QtiService;
import com.tle.core.util.ims.beans.IMSManifest;
import com.tle.core.util.ims.beans.IMSResource;
import com.tle.ims.service.IMSService;
import com.tle.web.controls.universal.handlers.FileUploadHandler;
import com.tle.web.controls.universal.handlers.fileupload.AbstractPackageAttachmentHandler;
import com.tle.web.controls.universal.handlers.fileupload.UploadedFile;
import com.tle.web.controls.universal.handlers.fileupload.details.PackageDetails;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.wizard.impl.WebRepository;

import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@NonNullByDefault
@Bind
@Singleton
public class QtiPackageAttachmentHandler extends AbstractPackageAttachmentHandler
{
	private static final String TEST_PACKAGE_TYPE = "QTITEST";

	@PlugKey("packageoptions.asqti")
	private static Label LABEL_TREAT_AS;

	static
	{
		PluginResourceHandler.init(QtiPackageAttachmentHandler.class);
	}

	@Inject
	private IMSService imsService;
	@Inject
	private QtiService qtiService;

	@Override
	public Attachment createAttachment(SectionInfo info, UploadedFile file, PackageDetails packageDetailsSection,
		boolean real)
	{
		CustomAttachment qti = new CustomAttachment();
		qti.setType(QtiConstants.TEST_CUSTOM_ATTACHMENT_TYPE);
		qti.setData(QtiConstants.KEY_FILE_SIZE, file.getSize());

		if( real )
		{
			FileUploadHandler handler = packageDetailsSection.getFileUploadHandler();
			String unzippedPath = FileUploadHandler.getUploadFilepath("_pkg");
			WebRepository repo = handler.getDialogState().getRepository();
			try
			{
				repo.unzipFile(file.getFilepath(), unzippedPath, false);
			}
			catch( Exception e )
			{
				throw Throwables.propagate(e);
			}
			file.setExtractedPath(unzippedPath);

			populateDetails(repo, qti, unzippedPath);
		}
		return qti;
	}

	@Nullable
	private String findTestXml(WebRepository repo, CustomAttachment qti, String packageExtractedFolder)
	{
		// Extract the assessmentTest XML file location from the
		// manifest
		try
		{
			// copy the manifest XML (FIXME: shouldn't need to do this!)
			// this hack is required because
			// IMSServiceImpl.ensureCombinedManifest expects there to be file
			// here, and if not creates a SCORM one instead.
			// Ugh.
			// FIXME: actually it will wipe any existing manifest if you cancel
			// out of this dialog session!
			repo.copy(packageExtractedFolder + "/imsmanifest.xml", "_IMS/imsmanifest.xml");
			qti.setUrl("_IMS/imsmanifest.xml");

			final IMSManifest imsManifest = imsService.getImsManifest(new StagingFile(repo.getStagingid()),
				packageExtractedFolder, true);
			final List<IMSResource> allResources = imsManifest.getAllResources();
			for( IMSResource imsResource : allResources )
			{
				final String type = imsResource.getType();
				if( type != null )
				{
					if( type.startsWith("imsqti_test_xml") )
					{
						return imsResource.getHref();
					}
				}
			}
			return null;
		}
		catch( Exception e )
		{
			throw Throwables.propagate(e);
		}
	}

	@Override
	public String getMimeType(SectionInfo info, UploadedFile file, String packageType)
	{
		if( packageType.equals(TEST_PACKAGE_TYPE) )
		{
			return QtiConstants.TEST_MIME_TYPE;
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
		final FileUploadHandler handler = packageDetailsSection.getFileUploadHandler();
		final CustomAttachment attachment = (CustomAttachment) uploadedFile.getAttachment();
		final StagingFile staging = handler.getStagingFile();

		final WebRepository repo = handler.getDialogState().getRepository();

		final String extractedPath = uploadedFile.getExtractedPath();
		// how can it not be??
		if( !Check.isEmpty(extractedPath) )
		{
			move(staging, extractedPath, QtiConstants.QTI_FOLDER_PATH);
			final String manifestPath = PathUtils.filePath(QtiConstants.QTI_FOLDER_PATH, "imsmanifest.xml");
			attachment.setUrl(manifestPath);

			repo.getState().getWizardMetadataMapper().setPackageExtractedFolder(QtiConstants.QTI_FOLDER_PATH);
		}
	}

	private void populateDetails(WebRepository repo, CustomAttachment attachment, String baseExtractedPath)
	{
		String xmlRelLoc = findTestXml(repo, attachment, baseExtractedPath);
		if( xmlRelLoc != null )
		{
			final ResolvedAssessmentTest quiz = qtiService.loadV2Test(new StagingFile(repo.getStagingid()),
				baseExtractedPath, xmlRelLoc);
			final QtiTestDetails details = qtiService.getTestDetails(quiz);

			attachment.setDescription(details.getTitle());

			final String testUuid = UUID.randomUUID().toString();
			attachment.setData(QtiConstants.KEY_TEST_UUID, testUuid);
			attachment.setData(QtiConstants.KEY_XML_PATH, PathUtils.filePath(QtiConstants.QTI_FOLDER_PATH, xmlRelLoc));

			final String toolName = details.getToolName();
			if( toolName != null )
			{
				attachment.setData(QtiConstants.KEY_TOOL_NAME, toolName);
				attachment.setData(QtiConstants.KEY_TOOL_VERSION, details.getToolVersion());
			}
			attachment.setData(QtiConstants.KEY_MAX_TIME, details.getMaxTime());
			attachment.setData(QtiConstants.KEY_QUESTION_COUNT, details.getQuestionCount());
			attachment.setData(QtiConstants.KEY_SECTION_COUNT, details.getSectionCount());
			attachment.setData(QtiConstants.KEY_NAVIGATION_MODE, details.getNavigationMode().toQtiString());
		}
	}

	@Override
	public void commitEdit(SectionInfo info, UploadedFile uploadedFile, PackageDetails packageDetailsSection,
		Attachment attachment)
	{
		// Move along, nothing to see here. Please disperse.
	}
}
