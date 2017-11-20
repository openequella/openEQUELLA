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

package com.tle.web.htmleditor.tinymce.addon.tle;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.Lists;
import com.tle.core.guice.Bind;
import com.tle.mycontent.service.MyContentService;
import com.tle.mycontent.web.selection.MyContentSelectable;
import com.tle.mycontent.web.selection.MyContentSelectionSettings;
import com.tle.web.htmleditor.HtmlEditorButtonDefinition;
import com.tle.web.htmleditor.tinymce.TinyMceAddonButtonRenderer;
import com.tle.web.htmleditor.tinymce.addon.tle.selection.callback.ScrapbookEmbedderCallback;
import com.tle.web.htmleditor.tinymce.addon.tle.service.MimeTemplateService;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.selection.SelectableInterface;
import com.tle.web.selection.SelectionSession;
import com.tle.web.selection.SelectionsMadeCallback;
import com.tle.web.selection.filter.SelectionFilter;

/**
 * @author aholland
 */
@Bind
@Singleton
@SuppressWarnings("nls")
public class TleScrapbookEmbedder extends AbstractSelectionAddon
{
	@PlugKey("scrapbook.button.name")
	private static Label LABEL_BUTTON;
	static
	{
		PluginResourceHandler.init(TleScrapbookEmbedder.class);
	}

	private static final PluginResourceHelper resources = ResourcesService
		.getResourceHelper(TleScrapbookEmbedder.class);
	private static final List<HtmlEditorButtonDefinition> BUTTONS = Collections.unmodifiableList(Lists
		.newArrayList(new HtmlEditorButtonDefinition("tle_scrapbookpicker",
			TleTinyMceAddonConstants.SCRAPBOOK_PICKER_ID, new TinyMceAddonButtonRenderer(resources
				.url("scripts/tle_scrapbookpicker/images/scrapbook.gif"), LABEL_BUTTON), LABEL_BUTTON, 2, true)));

	@Inject
	private MyContentService myContentService;
	@Inject
	private MimeTemplateService mimeTemplateService;
	@Inject
	private MyContentSelectable scrapbookSelectable;

	@Override
	public boolean applies(String action)
	{
		return action.equals("select_embed");
	}

	@Override
	protected boolean isAddToRecentSelections()
	{
		return false;
	}

	@Override
	public boolean isEnabled()
	{
		return myContentService.isMyContentContributionAllowed();
	}

	@Override
	protected Class<? extends SelectionsMadeCallback> getCallbackClass()
	{
		return ScrapbookEmbedderCallback.class;
	}

	@Override
	protected SelectableInterface getSelectable(SectionInfo info, SelectionSession session)
	{
		if( myContentService.isMyContentContributionAllowed() )
		{
			final MyContentSelectionSettings myContentSettings = new MyContentSelectionSettings();
			myContentSettings.setRawFilesOnly(true);
			session.setAttribute(MyContentSelectionSettings.class, myContentSettings);

			final SelectionFilter mimeFilter = new SelectionFilter();
			mimeFilter.setAllowedMimeTypes(mimeTemplateService.getEmbeddableMimeTypes());
			session.setAttribute(SelectionFilter.class, mimeFilter);

			session.setSkipCheckoutPage(true);
			session.setCancelDisabled(true);
			session.setSelectScrapbook(true);

			return scrapbookSelectable;
		}
		return null;
	}

	@Override
	public String getId()
	{
		return TleTinyMceAddonConstants.SCRAPBOOK_PICKER_ID;
	}

	@Override
	protected PluginResourceHelper getResourceHelper()
	{
		return resources;
	}

	@Override
	public List<HtmlEditorButtonDefinition> getButtons(SectionInfo info)
	{
		return BUTTONS;
	}
}
