package com.tle.admin.collection.summarydisplay;

import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

@SuppressWarnings("nls")
public class XsltConfig extends AbstractTemplatingConfig
{
	@Override
	public void setup()
	{
		super.setup();
		editor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
	}

	@Override
	public String getEditorLabelKey()
	{
		return "com.tle.admin.collection.tool.summarysections.xslt.desc";
	}
}