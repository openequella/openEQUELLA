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

package com.tle.core.userscripts.service.session;

import com.tle.common.userscripts.UserScriptsConstants.ScriptTypes;
import com.tle.core.entity.EntityEditingBean;

public class UserScriptEditingBean extends EntityEditingBean
{
	private static final long serialVersionUID = 1L;

	private ScriptTypes selection;
	private String script;
	private String moduleName;

	public String getScript()
	{
		return script;
	}

	public void setScript(String script)
	{
		this.script = script;
	}

	public ScriptTypes getSelection()
	{
		return selection;
	}

	public void setSelection(ScriptTypes selection)
	{
		this.selection = selection;
	}

	public String getModuleName()
	{
		return moduleName;
	}

	public void setModuleName(String moduleName)
	{
		this.moduleName = moduleName;
	}
}
