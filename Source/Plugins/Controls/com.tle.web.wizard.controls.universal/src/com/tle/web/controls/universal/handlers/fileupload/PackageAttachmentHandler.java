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
