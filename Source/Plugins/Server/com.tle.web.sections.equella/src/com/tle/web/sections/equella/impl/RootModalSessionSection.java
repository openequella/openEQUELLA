package com.tle.web.sections.equella.impl;

import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.AbstractRootModalSessionSection;
import com.tle.web.sections.equella.AbstractRootModalSessionSection.RootModalSessionModel;
import com.tle.web.sections.equella.ModalSession;
import com.tle.web.sections.events.RenderEventContext;

/**
 * @author aholland
 */
public class RootModalSessionSection extends AbstractRootModalSessionSection<RootModalSessionModel>
{
	@TreeLookup
	private ModalErrorSection errorSection;

	@Override
	public String getDefaultPropertyName()
	{
		return "_md"; //$NON-NLS-1$
	}

	@Override
	protected SectionId getErrorSection()
	{
		return errorSection;
	}

	@Override
	protected SectionResult getFinalRenderable(RenderEventContext context, RootModalSessionModel model)
	{
		return model.getParts().getNamedResult(context, "body"); //$NON-NLS-1$
	}

	@Override
	protected void setupModelForRender(SectionInfo info, RootModalSessionModel model)
	{
		// nothing
	}

	@Override
	protected Object getSessionKey()
	{
		return ModalSession.class;
	}
}
