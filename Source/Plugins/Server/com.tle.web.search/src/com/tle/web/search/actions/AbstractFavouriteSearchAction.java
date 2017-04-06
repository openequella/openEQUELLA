package com.tle.web.search.actions;

import com.tle.core.user.CurrentUser;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.dialog.EquellaDialog;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.Link;

@SuppressWarnings("nls")
public abstract class AbstractFavouriteSearchAction extends AbstractPrototypeSection<Object> implements HtmlRenderer
{
	@PlugKey("actions.favourite")
	private static Label LABEL_BUTTON;

	protected abstract EquellaDialog<?> getDialog();

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		final Link link = getDialog().getOpener();
		link.setStyleClass("add-to-favourites");
		link.setLabel(LABEL_BUTTON);
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( CurrentUser.wasAutoLoggedIn() || CurrentUser.isGuest() )
		{
			return null;
		}
		return SectionUtils.renderSectionResult(context, getDialog().getOpener());
	}

	protected Label getLabel()
	{
		return LABEL_BUTTON;
	}
}
