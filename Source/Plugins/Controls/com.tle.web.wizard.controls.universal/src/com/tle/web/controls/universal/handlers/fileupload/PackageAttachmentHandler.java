package com.tle.web.controls.universal.handlers.fileupload;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.item.attachments.Attachment;
import com.tle.web.controls.universal.handlers.fileupload.details.PackageDetails;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.render.Label;

/**
 * @author Aaron
 */
@NonNullByDefault
public interface PackageAttachmentHandler
{
	/**
	 * @param repo
	 * @param file
	 * @param real If false then just create an attachment that looks the part,
	 *            but will be eventually thrown out. If true then do any
	 *            unzipping and processing etc.
	 * @return
	 */
	Attachment createAttachment(SectionInfo info, UploadedFile file, PackageDetails packageDetailsSection, boolean real);

	void saveDetailsToAttachment(SectionInfo info, UploadedFile uploadedFile, PackageDetails packageDetailsSection,
		Attachment attachment);

	void commitNew(SectionInfo info, UploadedFile uploadedFile, PackageDetails packageDetailsSection,
		String replacementUuid);

	void commitEdit(SectionInfo info, UploadedFile uploadedFile, PackageDetails packageDetailsSection,
		Attachment attachment);

	/**
	 * A handler can possibly handle multiple package types, hence the param.
	 * 
	 * @param packageType
	 * @return
	 */
	String getMimeType(SectionInfo info, UploadedFile file, String packageType);

	Label getTreatAsLabel(SectionInfo info, UploadedFile file, String packageType);
}
