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