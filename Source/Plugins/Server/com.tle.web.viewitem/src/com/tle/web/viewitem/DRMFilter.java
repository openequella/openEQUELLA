package com.tle.web.viewitem;

import com.tle.web.sections.Section;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.standard.Button;

@TreeIndexed
public interface DRMFilter extends Section
{
	void setSkip(SectionInfo info, boolean skip);

	void initAcceptButton(SectionInfo info, Button acceptButton, JSHandler handler, JSHandler previewHandler);

	JSCallable getLicenseFunction();
}
