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

package com.tle.core.taxonomy.wizard;

import com.tle.annotation.NonNullByDefault;
import com.tle.core.guice.Bind;
import com.tle.core.taxonomy.TermResult;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.render.CombinedRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.renderers.LinkRenderer;

@NonNullByDefault
@Bind
@SuppressWarnings("nls")
public class WidePopupBrowserDialog extends AbstractPopupBrowserDialog<WidePopupBrowserDialog.WideBrowserModel>
{
	private static final LabelRenderer SPACER = new LabelRenderer(new TextLabel("&nbsp;&nbsp;&nbsp;", true));

	@PlugKey("wizard.widepopupbrowser.select")
	private static Label SELECT_LABEL;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Override
	protected String getContentBodyClass(RenderContext context)
	{
		return "widepopupbrowserdialog";
	}

	@Override
	protected SectionRenderable getRenderableContents(RenderContext context)
	{
		return viewFactory.createResult("popupbrowser/widepopup.ftl", this);
	}

	@Override
	protected SectionRenderable getTermClickTarget(TermResult tr)
	{
		SectionRenderable sr = SectionUtils.convertToRenderer(tr.getTerm());
		if( isSelectable(tr) )
		{
			HtmlLinkState selectLink = new HtmlLinkState(SELECT_LABEL, new OverrideHandler(selectTermFunc,
				tr.getFullTerm()));
			selectLink.addClass("add");
			sr = new CombinedRenderer(sr, SPACER, new LinkRenderer(selectLink));
		}
		return sr;
	}

	@Override
	public WideBrowserModel instantiateDialogModel(SectionInfo info)
	{
		return new WideBrowserModel();
	}

	public static class WideBrowserModel extends AbstractPopupBrowserDialog.AbstractPopupBrowserModel
	{
		// Nothing - should probably make the extended class not abstract
		// instead
	}
}
