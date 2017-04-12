package com.tle.web.sections.standard;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.DocumentParamsEvent;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.standard.js.JSValueComponent;
import com.tle.web.sections.standard.js.modules.CodeMirrorLibrary.EditorType;
import com.tle.web.sections.standard.model.CodeMirrorState;

public class CodeMirror extends AbstractValueStateComponent<CodeMirrorState, JSValueComponent>
{
	private boolean showHelp;
	private boolean allowFullScreen;
	private EditorType editorType;

	public CodeMirror()
	{
		super(RendererConstants.CODE_MIRROR);
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return setupState(info, new CodeMirrorState());
	}

	@Override
	protected CodeMirrorState setupState(SectionInfo info, CodeMirrorState state)
	{
		super.setupState(info, state);
		state.setShowHelp(showHelp);
		state.setAllowFullScreen(allowFullScreen);
		state.setEditorType(editorType);
		return state;
	}

	public String getValue(SectionInfo info)
	{
		return getState(info).getValue();
	}

	public void setValue(SectionInfo info, String value)
	{
		getState(info).setValue(value);
	}

	@Override
	protected String getBookmarkStringValue(CodeMirrorState state)
	{
		return state.getValue();
	}

	@Override
	public JSExpression createGetExpression()
	{
		return super.createGetExpression();
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

	@Override
	public void document(SectionInfo info, DocumentParamsEvent event)
	{
		// TODO Auto-generated method stub
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
