package com.tle.web.mimetypes.section;

import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.viewers.AbstractNewWindowConfigDialog;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.Label;

public class DefaultViewerConfigDialog extends AbstractNewWindowConfigDialog
{
	@PlugKey("default.title")
	private static Label DEFAULT_TITLE_LABEL;

	@Override
	protected Label getTitleLabel(RenderContext context)
	{
		return DEFAULT_TITLE_LABEL;
	}

}
