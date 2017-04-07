package com.tle.web.sections.errors;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionsController;
import com.tle.web.sections.events.SectionEvent;

public interface SectionsExceptionHandler
{
	boolean canHandle(SectionInfo info, Throwable ex, SectionEvent<?> event);

	void handle(Throwable exception, SectionInfo info, SectionsController controller, SectionEvent<?> event);
}
