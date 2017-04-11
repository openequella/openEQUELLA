package com.tle.web.sections.registry.handler;

import com.tle.web.sections.Section;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.events.ParametersEvent;
import com.tle.web.sections.events.ParametersEventListener;

public class AnnotationRequestListener extends TargetedListener implements ParametersEventListener
{
	private final AnnotatedBookmarkScanner handler;

	public AnnotationRequestListener(String id, Section section, SectionTree tree, AnnotatedBookmarkScanner handler)
	{
		super(id, section, tree);
		this.handler = handler;
	}

	@Override
	public void handleParameters(SectionInfo info, ParametersEvent event) throws Exception
	{
		handler.handleParameters(info, id + ".", info.getModelForId(id), event); //$NON-NLS-1$
	}
}
