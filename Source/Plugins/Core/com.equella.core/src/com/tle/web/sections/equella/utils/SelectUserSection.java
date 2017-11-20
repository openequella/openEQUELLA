/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.web.sections.equella.utils;

import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.events.js.ParameterizedEvent;
import com.tle.web.sections.js.JSCallable;

@Bind
public final class SelectUserSection extends AbstractSelectUserSection<AbstractSelectUserSection.Model>
{
	@SuppressWarnings("nls")
	@Override
	protected JSCallable getResultUpdater(SectionTree tree, ParameterizedEvent eventHandler)
	{
		return ajax.getAjaxUpdateDomFunction(tree, null, eventHandler,
			ajax.getEffectFunction(EffectType.REPLACE_WITH_LOADING), RESULTS_DIVID, "buttons");
	}

	@Override
	protected SelectedUser createSelectedUser(SectionInfo info, String uuid, String displayName)
	{
		return new SelectedUser(uuid, displayName);
	}
}
