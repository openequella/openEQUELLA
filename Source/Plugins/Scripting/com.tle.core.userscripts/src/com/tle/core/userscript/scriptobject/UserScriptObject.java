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

package com.tle.core.userscript.scriptobject;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.common.Check;
import com.tle.common.userscripts.UserScriptsConstants.ScriptTypes;
import com.tle.common.userscripts.entity.UserScript;
import com.tle.core.guice.Bind;
import com.tle.core.scripting.service.UserScriptObjectContributor;
import com.tle.core.userscripts.dao.UserScriptsDao;

@Bind
@Singleton
public class UserScriptObject implements UserScriptObjectContributor
{
	@Inject
	private UserScriptsDao userScriptDao;

	@Override
	public void addUserScriptObject(Map<String, Object> objects)
	{
		List<UserScript> scriptList = userScriptDao.enumerateForType(ScriptTypes.EXECUTABLE);
		for( UserScript userScript : scriptList )
		{
			String name = userScript.getModuleName();
			if( !Check.isEmpty(name) )
			{
				objects.put(name, userScript.getScript());
			}
		}
	}
}
