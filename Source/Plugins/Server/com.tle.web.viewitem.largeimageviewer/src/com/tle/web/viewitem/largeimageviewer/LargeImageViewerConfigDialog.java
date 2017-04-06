package com.tle.web.viewitem.largeimageviewer;

import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.viewers.AbstractNewWindowConfigDialog;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.Label;

public class LargeImageViewerConfigDialog extends AbstractNewWindowConfigDialog
{
	@PlugKey("title")
	private static Label LABEL_TITLE;

	public LargeImageViewerConfigDialog()
	{
		super(false);
	}

	@Override
	protected Label getTitleLabel(RenderContext context)
	{
		return LABEL_TITLE;
	}
}
