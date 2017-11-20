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

package com.tle.web.viewitem.summary.sidebar.actions;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.annotations.Component;

@NonNullByDefault
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

	@Nullable
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
