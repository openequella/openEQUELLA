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
