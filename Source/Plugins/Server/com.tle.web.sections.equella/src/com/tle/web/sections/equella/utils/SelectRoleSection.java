package com.tle.web.sections.equella.utils;

import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.events.js.ParameterizedEvent;
import com.tle.web.sections.js.JSCallable;

@Bind
public class SelectRoleSection extends AbstractSelectRoleSection<AbstractSelectRoleSection.Model>
{
	@SuppressWarnings("nls")
	@Override
	protected JSCallable getResultUpdater(SectionTree tree, ParameterizedEvent eventHandler)
	{
		return ajax.getAjaxUpdateDomFunction(tree, null, eventHandler,
			ajax.getEffectFunction(EffectType.REPLACE_WITH_LOADING), RESULTS_DIVID, "buttons");
	}

	@Override
	protected SelectedRole createSelectedRole(SectionInfo info, String uuid, String displayName)
	{
		return new SelectedRole(uuid, displayName);
	}
}