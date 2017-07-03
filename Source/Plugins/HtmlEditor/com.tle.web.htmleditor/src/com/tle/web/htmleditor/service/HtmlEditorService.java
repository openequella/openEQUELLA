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

package com.tle.web.htmleditor.service;

import com.tle.common.beans.exception.InvalidDataException;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.htmleditor.HtmlEditorConfiguration;
import com.tle.web.htmleditor.HtmlEditorConfigurationEditingSession;
import com.tle.web.htmleditor.HtmlEditorControl;
import com.tle.web.htmleditor.HtmlEditorInterface;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.SectionRenderable;

@NonNullByDefault
@SuppressWarnings("nls")
public interface HtmlEditorService
{
	String DISPLAY_CLASS = "htmlcontent";
	String CONTENT_DIRECTORY = "_HtmlControlContent";

	HtmlEditorInterface getEditor();

	HtmlEditorControl getControl();

	HtmlEditorConfiguration getEditorConfig();

	HtmlEditorConfigurationEditingSession createEditorConfigEditingSession();

	HtmlEditorConfigurationEditingSession getEditorConfigEditingSession(String sessionId);

	void cancelEditorConfigEditingSession(String sessionId);

	void commitEditorConfigEditingSession(String sessionId);

	void validateEditorOptions(String editorOptions) throws InvalidDataException;

	String getStylesheetContents();

	void setStylesheetContents(String css);

	/**
	 * @return Will return null if no user styles have been created
	 */
	@Nullable
	String getStylesheetRelativeUrl();

	void exportStylesheet(FileHandle handle, @Nullable String folder);

	SectionRenderable getHtmlRenderable(RenderContext context, String html);
}