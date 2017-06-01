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

package com.tle.core.scripting;

import java.util.Collections;
import java.util.Map;

import org.mozilla.javascript.ClassShutter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Scriptable;

import com.dytech.edge.common.PropBagWrapper;
import com.dytech.edge.common.ScriptContext;
import com.tle.common.scripting.ScriptObject;
import com.tle.common.util.Logger;

/**
 * @author aholland
 */
public class DefaultScriptContext implements ScriptContext
{
	private static final long serialVersionUID = 1L;

	private final Map<String, Object> scriptObjects;
	private final Map<String, Object> userScriptObjects;
	private final PropBagWrapper xml;

	private Logger logger;

	public DefaultScriptContext(Map<String, Object> scriptObjects, Map<String, Object> userScriptObjects,
		PropBagWrapper xml)
	{
		this.scriptObjects = scriptObjects;
		this.userScriptObjects = userScriptObjects;
		this.xml = xml;
	}

	@Override
	public PropBagWrapper getXml()
	{
		return xml;
	}

	@Override
	public Logger getLogger()
	{
		return logger;
	}

	@Override
	public void setLogger(Logger logger)
	{
		this.logger = logger;
	}

	public Scriptable getUserScriptScope(Context jsContext)
	{
		Scriptable scope = new ImporterTopLevel(jsContext);
		for( String name : userScriptObjects.keySet() )
		{
			Object obj = userScriptObjects.get(name);
			if( obj instanceof Boolean )
			{
				scope.put(name, scope, obj);
			}
			else if( obj != null )
			{
				Scriptable jsArgs = Context.toObject(obj, scope);
				scope.put(name, scope, jsArgs);
			}
		}

		return scope;
	}

	@SuppressWarnings("nls")
	public Scriptable getScope(Context jsContext)
	{
		Scriptable scope = new ImporterTopLevel(jsContext);

		for( String name : scriptObjects.keySet() )
		{
			Object obj = scriptObjects.get(name);
			if( obj instanceof Boolean )
			{
				scope.put(name, scope, obj);
			}
			else if( obj != null )
			{
				Scriptable jsArgs = Context.toObject(obj, scope);
				scope.put(name, scope, jsArgs);
			}
		}

		// Remove the ability to create new Java objects in the script. List
		// comes from https://bugzilla.mozilla.org/show_bug.cgi?id=468385
		scope.delete("Packages");
		scope.delete("JavaImporter");
		scope.delete("JavaAdapter");
		scope.delete("getClass");
		scope.delete("java");
		scope.delete("javax");
		scope.delete("com");
		scope.delete("net");
		scope.delete("edu");
		scope.delete("org");

		try
		{
			// Prevent existingObject.getClass().forName('...')
			jsContext.setClassShutter(new ClassShutter()
			{
				@Override
				public boolean visibleToScripts(String className)
				{
					return !className.equals("java.lang.Class");
				}
			});
		}
		catch( SecurityException se )
		{
			// ignore - there's no way to test presence of ClassShutter
			// (indicative of a Context provided by calling module: ReportEngine
			// for example) other than by attempting to set ClassShutter.
			// See #8135
		}
		return scope;
	}

	@Override
	public void scriptEnter()
	{
		for( Object object : scriptObjects.values() )
		{
			// it should be...
			if( object instanceof ScriptObject )
			{
				((ScriptObject) object).scriptEnter();
			}
		}
	}

	@Override
	public void scriptExit()
	{
		for( Object object : scriptObjects.values() )
		{
			// it should be...
			if( object instanceof ScriptObject )
			{
				((ScriptObject) object).scriptExit();
			}
		}
	}

	@Override
	public void addScriptObject(String name, Object object)
	{
		scriptObjects.put(name, object);
	}
	
	@Override
	public void addUserScriptObject(String moduleName, Object script)
	{
		userScriptObjects.put(moduleName, script);
	}

	@Override
	public Map<String, Object> getScriptObjects()
	{
		return Collections.unmodifiableMap(scriptObjects);
	}

	@Override
	public Map<String, Object> getUserScriptObjects()
	{
		return Collections.unmodifiableMap(userScriptObjects);
	}
}