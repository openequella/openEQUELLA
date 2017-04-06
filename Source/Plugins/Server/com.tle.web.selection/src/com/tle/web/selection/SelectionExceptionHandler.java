package com.tle.web.selection;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.AbstractModalSessionExceptionHandler;
import com.tle.web.selection.section.RootSelectionSection;

@Bind
@Singleton
public class SelectionExceptionHandler extends AbstractModalSessionExceptionHandler<SelectionSession>
{
	@Inject
	private SelectionService selectionService;

	@Override
	protected SelectionServiceImpl getModalService()
	{
		return (SelectionServiceImpl) selectionService;
	}

	@Override
	protected boolean shouldHandle(SectionInfo info)
	{
		RootSelectionSection rootSection = info.lookupSection(RootSelectionSection.class);
		return !rootSection.getModel(info).isRendering();
	}
}
