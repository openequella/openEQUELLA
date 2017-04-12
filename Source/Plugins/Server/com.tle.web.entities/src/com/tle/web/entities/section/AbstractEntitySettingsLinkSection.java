package com.tle.web.entities.section;

import com.tle.common.Pair;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.settings.AbstractParentSettingsSection;

@TreeIndexed
public abstract class AbstractEntitySettingsLinkSection<M> extends AbstractParentSettingsSection<M>
{
	protected abstract HtmlLinkState getShowEntitiesLink(SectionInfo info);

	protected abstract Label getDescriptionLabel(SectionInfo info);

	@Override
	public Pair<HtmlLinkState, Label> getLink(RenderEventContext context)
	{
		return new Pair<HtmlLinkState, Label>(getShowEntitiesLink(context), getDescriptionLabel(context));
	}
}
