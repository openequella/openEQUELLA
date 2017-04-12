package com.tle.web.sections.standard.dialog.model;

import java.util.List;

import com.tle.web.sections.standard.RendererConstants;
import com.tle.web.sections.standard.model.HtmlComponentState;

public class ControlsState extends HtmlComponentState
{
	private List<DialogControl> controls;

	public ControlsState()
	{
		super(RendererConstants.CONTROLS);
	}

	public List<DialogControl> getControls()
	{
		return controls;
	}

	public void setControls(List<DialogControl> controls)
	{
		this.controls = controls;
	}
}