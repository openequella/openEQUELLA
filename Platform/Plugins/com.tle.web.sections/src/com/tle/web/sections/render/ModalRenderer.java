package com.tle.web.sections.render;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.RenderEventContext;

public interface ModalRenderer
{
	SectionResult renderModal(RenderEventContext context) throws Exception;

	boolean isModal(SectionInfo info);
}
