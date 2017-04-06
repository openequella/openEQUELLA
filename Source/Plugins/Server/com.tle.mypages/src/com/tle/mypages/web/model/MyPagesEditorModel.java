package com.tle.mypages.web.model;

import com.tle.web.sections.render.SectionRenderable;

/**
 * @author aholland
 */
public class MyPagesEditorModel
{
	private SectionRenderable editorRenderable;
	private SectionRenderable extraRenderable;

	private boolean showEditor;

	public boolean isShowEditor()
	{
		return showEditor;
	}

	public void setShowEditor(boolean showEditor)
	{
		this.showEditor = showEditor;
	}

	public SectionRenderable getEditorRenderable()
	{
		return editorRenderable;
	}

	public void setEditorRenderable(SectionRenderable editorRenderable)
	{
		this.editorRenderable = editorRenderable;
	}

	public SectionRenderable getExtraRenderable()
	{
		return extraRenderable;
	}

	public void setExtraRenderable(SectionRenderable extraRenderable)
	{
		this.extraRenderable = extraRenderable;
	}
}
