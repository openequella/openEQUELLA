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

package com.tle.web.search.actions;

import com.tle.common.usermanagement.user.CurrentUser;
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
