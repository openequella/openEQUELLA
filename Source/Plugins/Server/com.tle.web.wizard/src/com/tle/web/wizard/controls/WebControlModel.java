/**
 * 
 */
package com.tle.web.wizard.controls;

import com.tle.web.sections.render.PreRenderOnly;
import com.tle.web.sections.render.TagRenderer;
import com.tle.web.sections.standard.js.impl.CombinedDisableable;

public class WebControlModel
{
	private PreRenderOnly readyScript;
	private CombinedDisableable disabler;
	private TagRenderer divContainer;

	public CombinedDisableable getDisabler()
	{
		return disabler;
	}

	public void setDisabler(CombinedDisableable disabler)
	{
		this.disabler = disabler;
	}

	public PreRenderOnly getReadyScript()
	{
		return readyScript;
	}

	public void setReadyScript(PreRenderOnly readyScript)
	{
		this.readyScript = readyScript;
	}

	public TagRenderer getDivContainer()
	{
		return divContainer;
	}

	public void setDivContainer(TagRenderer divContainer)
	{
		this.divContainer = divContainer;
	}

}