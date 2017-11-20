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
