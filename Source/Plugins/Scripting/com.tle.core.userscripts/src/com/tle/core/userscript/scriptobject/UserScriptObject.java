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
