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

package com.tle.web.viewitem.viewer;

import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.viewers.AbstractNewWindowConfigDialog;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.dialog.model.DialogControl;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.sections.standard.renderers.LinkRenderer;

/**
 * @author larry
 */
@SuppressWarnings("nls")
public class GoogleDocViewerConfigDialog extends AbstractNewWindowConfigDialog
{
	/**
	 * Keys to the locale-specific strings in i18n.properties. The URLs are are
	 * also stored as locale-specific properties.
	 */
	@PlugKey("googledoc.tos.url.label")
	private static Label LABEL_TOS_LINK;

	@PlugKey("googledoc.tos.text")
	private static String KEY_TOS_TEXT;

	@PlugKey("googledocviewer")
	private static Label LABEL_TITLE;

	@Override
	public String getHeight()
	{
		return "auto";
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		TermsOfServiceText renderedLink = new TermsOfServiceText(LABEL_TOS_LINK, KEY_TOS_TEXT);
		tree.registerInnerSection(renderedLink, id);

		controls.add(new DialogControl(new TextLabel(""), renderedLink));
	}

	@Override
	protected Label getTitleLabel(RenderContext context)
	{
		return LABEL_TITLE;
	}
}

@SuppressWarnings("nls")
class TermsOfServiceText extends AbstractPrototypeSection<Object> implements HtmlRenderer
{
	/**
	 * Link to the text of the Google Docs Terms of Service, presumed to be
	 * unchanging.
	 */
	private static final String GOOGLEDOC_TOS_URL = "http://docs.google.com/viewer/TOS?";

	private final Label tosLinkLabel;
	private final String tosTextKey;

	public TermsOfServiceText(Label tosLinkLabel, String tosTextKey)
	{
		this.tosLinkLabel = tosLinkLabel;
		this.tosTextKey = tosTextKey;
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		SimpleBookmark bookmark = new SimpleBookmark(GOOGLEDOC_TOS_URL);
		HtmlLinkState htmlLinkState = new HtmlLinkState(tosLinkLabel, bookmark);
		htmlLinkState.setTarget("_blank");

		LinkRenderer linkRenderer = new LinkRenderer(htmlLinkState);
		LabelRenderer labelRenderer = new LabelRenderer(new KeyLabel(tosTextKey, SectionUtils.renderToString(context,
			linkRenderer)));
		return labelRenderer;
	}
}