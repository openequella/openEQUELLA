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

import javax.inject.Inject;

import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.dialog.EquellaDialog;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.annotations.Component;

@Bind
public class ShareSearchQueryAction extends AbstractShareSearchQueryAction
{
	@PlugKey("actions.share")
	private static Label LABEL;

	@Inject
	@Component(name = "sd")
	private StandardShareSearchQueryDialog shareDialog;

	@Override
	public Label getLabel()
	{
		return LABEL;
	}

	@Override
	public EquellaDialog<?> getDialog()
	{
		return shareDialog;
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( getModel(context).isDisabled() )
		{
			return null;
		}

		return SectionUtils.renderSectionResult(context, shareDialog.getOpener());
	}

}
