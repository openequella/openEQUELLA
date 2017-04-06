/*
 * Created on Jun 21, 2004 For "The Learning Edge"
 */
package com.tle.web.wizard.standard.controls;

import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.wizard.controls.AbstractSimpleWebControl;
import com.tle.web.wizard.controls.CButton;

/**
 * @author jmaginnis
 */
@Bind
public class Button extends AbstractSimpleWebControl
{
	@Component(stateful = false)
	private com.tle.web.sections.standard.Button button;
	@EventFactory
	private EventGenerator events;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		button.setClickHandler(events.getNamedHandler("clicked")); //$NON-NLS-1$
	}

	@EventHandlerMethod
	public void clicked(SectionInfo info)
	{
		((CButton) getWrappedControl()).setActionFired(true);
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		addDisabler(context, button);
		button.setLabel(context, new TextLabel(getTitle()));
		return SectionUtils.renderSection(context, button.getSectionId());
	}

	@Override
	public void doEdits(SectionInfo info)
	{
		// nothing
	}

	@Override
	protected ElementId getIdForLabel()
	{
		return button;
	}
}
