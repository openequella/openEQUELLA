package com.tle.web.controls.universal;

import java.util.List;

import com.google.common.collect.Lists;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.standard.Button;

public class DialogRenderOptions
{
	private final List<Button> actions = Lists.newArrayList();
	private JSHandler saveClickHandler;
	private boolean fullscreen;
	private boolean showSave;
	private boolean showAddReplace;

	public List<Button> getActions()
	{
		return actions;
	}

	public void addAction(Button action)
	{
		actions.add(action);
	}

	public boolean isFullscreen()
	{
		return fullscreen;
	}

	public void setFullscreen(boolean fullscreen)
	{
		this.fullscreen = fullscreen;
	}

	public boolean isShowSave()
	{
		return showSave;
	}

	public void setShowSave(boolean showSave)
	{
		this.showSave = showSave;
	}

	public boolean isShowAddReplace()
	{
		return showAddReplace;
	}

	public void setShowAddReplace(boolean showAddReplace)
	{
		this.showAddReplace = showAddReplace;
	}

	public JSHandler getSaveClickHandler()
	{
		return saveClickHandler;
	}

	public void setSaveClickHandler(JSHandler saveClickHandler)
	{
		this.saveClickHandler = saveClickHandler;
	}
}
