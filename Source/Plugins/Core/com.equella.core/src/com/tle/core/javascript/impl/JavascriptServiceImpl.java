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

package com.tle.core.javascript.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.common.NameValue;
import com.tle.core.guice.Bind;
import com.tle.core.javascript.JavascriptLibrary;
import com.tle.core.javascript.JavascriptModule;
import com.tle.core.javascript.JavascriptService;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;

/**
 * @author aholland
 */
@Bind(JavascriptService.class)
@Singleton
public class JavascriptServiceImpl implements JavascriptService
{
	private PluginTracker<JavascriptLibrary> tracker;

	@Override
	public List<JavascriptLibrary> getAllJavascriptLibraries()
	{
		return tracker.getBeanList();
	}

	@Override
	public JavascriptLibrary getJavascriptLibrary(String libraryId)
	{
		return tracker.getBeanMap().get(libraryId);
	}

	@Override
	public JavascriptModule getJavascriptModule(String libraryId, String moduleId)
	{
		JavascriptLibrary lib = getJavascriptLibrary(libraryId);
		if( lib != null )
		{
			return lib.getModules().get(moduleId);
		}
		return null;
	}

	@Override
	public List<NameValue> getAllJavascriptLibraryNames()
	{
		List<JavascriptLibrary> libs = tracker.getBeanList();

		List<NameValue> names = new ArrayList<NameValue>();
		for( JavascriptLibrary lib : libs )
		{
			names.add(new NameValue(lib.getDisplayName(), lib.getId()));
		}
		Collections.sort(names, new Comparator<NameValue>()
		{
			@Override
			public int compare(NameValue lib1, NameValue lib2)
			{
				return lib1.getName().compareToIgnoreCase(lib2.getName());
			}
		});
		return names;
	}

	@Override
	public List<NameValue> getAllJavascriptModuleNames(String libraryId)
	{
		JavascriptLibrary lib = tracker.getBeanMap().get(libraryId);
		List<NameValue> names = new ArrayList<NameValue>();
		for( JavascriptModule module : lib.getModules().values() )
		{
			names.add(new NameValue(module.getDisplayName(), module.getId()));
		}
		Collections.sort(names, new Comparator<NameValue>()
		{
			@Override
			public int compare(NameValue mod1, NameValue mod2)
			{
				return mod1.getName().compareToIgnoreCase(mod2.getName());
			}
		});
		return names;
	}

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		tracker = new PluginTracker<JavascriptLibrary>(pluginService, "com.tle.core.javascript",
			"javascriptLibrary", "id"); //$NON-NLS-1$ //$NON-NLS-2$
		tracker.setBeanKey("class"); //$NON-NLS-1$
	}
}
