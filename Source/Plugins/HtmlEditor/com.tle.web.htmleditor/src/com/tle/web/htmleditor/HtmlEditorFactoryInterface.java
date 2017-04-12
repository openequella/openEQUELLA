package com.tle.web.htmleditor;

/**
 * @author aholland
 */
public interface HtmlEditorFactoryInterface
{
	HtmlEditorInterface createEditor();

	HtmlEditorControl createControl();
}
