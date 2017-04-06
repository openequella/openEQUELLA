package com.tle.web.controls.universal.handlers.fileupload;

import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;

/**
 * @author Aaron
 */
public interface TypeOptions extends SectionId
{
	void loadOptions(SectionInfo info, UploadedFile uploadedFile);

	boolean saveOptions(SectionInfo info, UploadedFile uploadedFile);
}
