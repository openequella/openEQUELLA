package com.tle.web.viewitem.summary.sidebar.actions;

import com.tle.web.sections.SectionTree;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.annotations.Component;

public abstract class GenericMinorActionSection extends GenericActionSection<Link>
{
	@Component
	private Link link;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		// So much better!
		// link.setDefaultRenderer(EquellaButtonExtension.ACTION_BUTTON);
	}

	protected abstract Label getLinkLabel();

	// for alphabetical sorting in MinorActionsGroupSection
	public abstract String getLinkText();

	protected Label getConfirmation()
	{
		return null;
	}

	@Override
	public final Link getComponent()
	{
		return link;
	}

	@Override
	protected void setupComponent(Link link)
	{
		link.setLabel(getLinkLabel());
	}

	@Override
	protected void setupHandler(JSHandler handler)
	{
		Label clickConfirmation = getConfirmation();
		if( clickConfirmation != null )
		{
			handler.addValidator(new Confirm(clickConfirmation));
		}
	}
}
