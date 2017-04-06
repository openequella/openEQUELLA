package com.tle.web.mimetypes.section;

import com.tle.beans.mime.MimeEntry;
import com.tle.web.sections.Section;
import com.tle.web.sections.SectionInfo;

public interface MimeEntryEditSection extends Section
{
	void loadEntry(SectionInfo info, MimeEntry entry);

	void saveEntry(SectionInfo info, MimeEntry entry);
}
