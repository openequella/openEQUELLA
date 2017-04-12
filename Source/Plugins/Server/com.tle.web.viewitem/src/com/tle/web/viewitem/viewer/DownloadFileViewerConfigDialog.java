package com.tle.web.viewitem.viewer;

import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.viewers.AbstractResourceViewerConfigDialog;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.dialog.model.DialogControl;

public class DownloadFileViewerConfigDialog extends AbstractResourceViewerConfigDialog
{
	@PlugKey("appendtoken")
	private static Label APPEND_TOKEN_LABEL;

	@PlugKey("downloadfileviewer")
	private static Label TITLE_LABEL;

	@Component
	private Checkbox appendToken;

	@Override
	@SuppressWarnings("nls")
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		controls.add(new DialogControl(APPEND_TOKEN_LABEL, appendToken));
		mappings.addMapMapping("attr", "appendToken", appendToken);
	}

	@Override
	protected Label getTitleLabel(RenderContext context)
	{
		return TITLE_LABEL;
	}
}
