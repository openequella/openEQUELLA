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

package com.tle.web.cloud.view.actions;

import javax.inject.Inject;

import com.tle.core.email.EmailService;
import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.ViewableChildInterface;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.equella.render.EquellaButtonExtension;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.annotations.Component;

@SuppressWarnings("nls")
@Bind
public class CloudShareWithOthersLinkSection extends AbstractPrototypeSection<Object>
	implements
		ViewableChildInterface,
		HtmlRenderer
{
	@PlugKey("share.cloud.item.title")
	private static Label SHARE_LABEL;

	@PlugURL("css/share/share.css")
	private static String CSS;

	@Inject
	@Component(name = "swod")
	private CloudShareWithOthersDialog dialog;

	@Inject
	private EmailService emailService;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( !emailService.hasMailSettings() )
		{
			return null;
		}
		return SectionUtils.renderSectionResult(context, dialog.getOpener());
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		Link button = dialog.getOpener();
		button.setLabel(SHARE_LABEL);
		button.setStyleClass("shareButton");
		button.setDefaultRenderer(EquellaButtonExtension.ACTION_BUTTON);
		button.addPrerenderables(CssInclude.include(CSS).hasRtl().make());
	}

	@Override
	public boolean canView(SectionInfo info)
	{
		return true;
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new Object();
	}
}
