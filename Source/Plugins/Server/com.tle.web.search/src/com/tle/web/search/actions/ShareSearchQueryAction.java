package com.tle.web.search.actions;

import javax.inject.Inject;

import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.dialog.EquellaDialog;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.annotations.Component;

@Bind
public class ShareSearchQueryAction extends AbstractShareSearchQueryAction
{
	@PlugKey("actions.share")
	private static Label LABEL;

	@Inject
	@Component(name = "sd")
	private StandardShareSearchQueryDialog shareDialog;

	@Override
	public Label getLabel()
	{
		return LABEL;
	}

	@Override
	public EquellaDialog<?> getDialog()
	{
		return shareDialog;
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( getModel(context).isDisabled() )
		{
			return null;
		}

		return SectionUtils.renderSectionResult(context, shareDialog.getOpener());
	}

}
