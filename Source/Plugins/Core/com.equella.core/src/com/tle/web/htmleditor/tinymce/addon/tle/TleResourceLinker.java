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
import com.tle.mycontent.web.selection.MyContentSelectionSettings;
import com.tle.web.htmleditor.HtmlEditorButtonDefinition;
import com.tle.web.htmleditor.tinymce.TinyMceAddonButtonRenderer;
import com.tle.web.htmleditor.tinymce.addon.tle.selection.callback.ResourceLinkerCallback;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.selection.SelectableInterface;
import com.tle.web.selection.SelectionSession;
import com.tle.web.selection.SelectionsMadeCallback;
import com.tle.web.selection.home.SelectionHomeSelectable;

/**
 * Embeds selected item attachments into a HTML editor. If the attachment is not
 * embeddable then the attachment is linked to instead. Attachments are
 * embeddable if they have an embedding template setup, either in the the
 * learningedge-config/plugins/com.tle.web.htmleditor
 * .tinymce.addon.tle/deftemplate.properties file, or
 * MimeEntry.attributes["EmbeddingTemplate"] (configured through mime.do)
 * 
 * @author aholland
 */
@SuppressWarnings("nls")
@Bind
@Singleton
public class TleResourceLinker extends AbstractSelectionAddon
{
	@PlugKey("linker.button.name")
	private static Label LABEL_BUTTON;
	static
	{
		PluginResourceHandler.init(TleResourceLinker.class);
	}

	private static final PluginResourceHelper resources = ResourcesService.getResourceHelper(TleResourceLinker.class);
	private static final List<HtmlEditorButtonDefinition> BUTTONS = Collections.unmodifiableList(Lists
		.newArrayList(new HtmlEditorButtonDefinition("tle_reslinker", TleTinyMceAddonConstants.RESOURCE_LINKER_ID,
			new TinyMceAddonButtonRenderer(resources.url("scripts/tle_reslinker/images/equellabutton.gif"),
				LABEL_BUTTON), LABEL_BUTTON, 2, true)));

	@Inject
	private MyContentService myContentService;
	@Inject
	private SelectionHomeSelectable selectionHomeSelectable;

	@Override
	protected boolean isAddToRecentSelections()
	{
		return true;
	}

	@Override
	public boolean applies(String action)
	{
		return action.equals("select_link");
	}

	@Override
	protected Class<? extends SelectionsMadeCallback> getCallbackClass()
	{
		return ResourceLinkerCallback.class;
	}

	@Override
	protected SelectableInterface getSelectable(SectionInfo info, SelectionSession session)
	{
		if( myContentService.isMyContentContributionAllowed() )
		{
			MyContentSelectionSettings settings = new MyContentSelectionSettings();
			settings.setRawFilesOnly(true);
			session.setAttribute(MyContentSelectionSettings.class, settings);
			session.setCancelDisabled(true);
		}
		session.setHomeSelectable("home");

		return selectionHomeSelectable;
	}

	@Override
	public String getId()
	{
		return TleTinyMceAddonConstants.RESOURCE_LINKER_ID;
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
