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
import com.google.common.collect.Sets;
import com.tle.core.guice.Bind;
import com.tle.web.htmleditor.HtmlEditorButtonDefinition;
import com.tle.web.htmleditor.tinymce.TinyMceAddonButtonRenderer;
import com.tle.web.htmleditor.tinymce.addon.tle.selection.FileUploadSelectable;
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

/**
 * @author aholland
 */
@SuppressWarnings("nls")
@Bind
@Singleton
public class TleFileUploader extends AbstractSelectionAddon
{
	@PlugKey("uploader.button.name")
	private static Label LABEL_BUTTON;
	static
	{
		PluginResourceHandler.init(TleFileUploader.class);
	}

	private static final PluginResourceHelper resources = ResourcesService.getResourceHelper(TleFileUploader.class);
	private static final List<HtmlEditorButtonDefinition> BUTTONS = Collections
		.unmodifiableList(Lists.newArrayList(new HtmlEditorButtonDefinition("tle_fileuploader",
			TleTinyMceAddonConstants.FILE_UPLOADER_ID, new TinyMceAddonButtonRenderer(resources
				.url("scripts/tle_fileuploader/images/paperclip.gif"), LABEL_BUTTON), LABEL_BUTTON, 2, true)));

	@Inject
	private FileUploadSelectable selectable;

	@Override
	protected boolean isAddToRecentSelections()
	{
		return false;
	}

	@Override
	protected Class<? extends SelectionsMadeCallback> getCallbackClass()
	{
		return ResourceLinkerCallback.class;
	}

	@Override
	protected PluginResourceHelper getResourceHelper()
	{
		return resources;
	}

	@Override
	protected SelectableInterface getSelectable(SectionInfo info, SelectionSession session)
	{
		session.setCancelDisabled(true);
		session.setAllowedSelectNavActions(Sets.<String> newHashSet());
		return selectable;
	}

	@Override
	public boolean applies(String action)
	{
		return action.equals("select_upload");
	}

	@Override
	public String getId()
	{
		return TleTinyMceAddonConstants.FILE_UPLOADER_ID;
	}

	@Override
	public List<HtmlEditorButtonDefinition> getButtons(SectionInfo info)
	{
		return BUTTONS;
	}
}
