package com.tle.web.settings;

import com.tle.common.Pair;
import com.tle.web.sections.ViewableChildInterface;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.model.HtmlLinkState;

public abstract class AbstractParentSettingsSection<M> extends AbstractPrototypeSection<M>
	implements
		ViewableChildInterface
{
	public abstract Pair<HtmlLinkState, Label> getLink(RenderEventContext context);
}
