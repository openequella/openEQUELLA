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

package com.tle.admin.usermanagement.canvas;

import com.tle.admin.gui.EditorException;
import com.tle.admin.plugin.GeneralPlugin;
import com.tle.beans.usermanagement.canvas.CanvasWrapperSettings;

/**
 * @author aholland
 */
public class CanvasPlugin extends GeneralPlugin<CanvasWrapperSettings>
{
	private final CanvasSettingsPanel generalPanel;

	public CanvasPlugin()
	{
		generalPanel = new CanvasSettingsPanel();

		setup();
	}

	protected void setup()
	{
		addFillComponent(generalPanel);
	}

	@Override
	public void load(CanvasWrapperSettings settings)
	{
		generalPanel.load(settings);
	}

	@Override
	public boolean save(CanvasWrapperSettings settings) throws EditorException
	{
		generalPanel.save(settings);
		return true;
	}
}
