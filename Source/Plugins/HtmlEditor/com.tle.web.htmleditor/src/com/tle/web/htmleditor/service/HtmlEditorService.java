package com.tle.web.htmleditor.service;

import com.dytech.edge.exceptions.InvalidDataException;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.filesystem.FileHandle;
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