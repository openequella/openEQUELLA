package com.tle.web.cloud.search.section;

import javax.inject.Inject;

import com.tle.web.search.actions.AbstractShareSearchQueryAction;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.dialog.EquellaDialog;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.annotations.Component;

public class CloudShareSearchQueryAction extends AbstractShareSearchQueryAction
{
	@PlugKey("actions.share")
	private static Label LABEL;

	@Inject
	@Component(name = "csd")
	private CloudShareSearchQueryDialog shareDialog;

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
}
