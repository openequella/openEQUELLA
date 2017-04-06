package com.tle.web.sections.standard.model;

import com.tle.web.sections.standard.RendererConstants;
import com.tle.web.sections.standard.js.modules.CodeMirrorLibrary.EditorType;

public class CodeMirrorState extends HtmlValueState
{
	private boolean showHelp;
	private boolean allowFullScreen;
	private EditorType editorType;

	public CodeMirrorState()
	{
		super(RendererConstants.CODE_MIRROR);
	}

	public boolean isShowHelp()
	{
		return showHelp;
	}

	public void setShowHelp(boolean showHelp)
	{
		this.showHelp = showHelp;
	}

	public EditorType getEditorType()
	{
		return editorType;
	}

	public void setEditorType(EditorType editorType)
	{
		this.editorType = editorType;
	}

	public boolean isAllowFullScreen()
	{
		return allowFullScreen;
	}

	public void setAllowFullScreen(boolean allowFullScreen)
	{
		this.allowFullScreen = allowFullScreen;
	}
}
