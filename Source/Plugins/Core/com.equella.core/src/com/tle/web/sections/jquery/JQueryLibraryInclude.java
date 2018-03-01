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

package com.tle.web.sections.jquery;

import java.util.Arrays;

import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.jquery.libraries.JQueryCore;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.PreRenderable;

/**
 * Please note, if you add any additional javascript files, they must be
 * included in the JQueryLibrary modules list.
 */
public class JQueryLibraryInclude extends IncludeFile
{
	public static final PluginResourceHelper urlHelper = ResourcesService.getResourceHelper(JQueryLibraryInclude.class);

	private static String[] addLibFolder(String[] jses)
	{
		String[] fullJses = new String[jses.length];
		int i = 0;
		for( String js : jses )
		{
			if( js.startsWith("~") ) //$NON-NLS-1$
			{
				// strip the ~ and use the path as-is
				fullJses[i++] = urlHelper.url(js.substring(1));
			}
			else
			{
				fullJses[i++] = urlHelper.url("jquerylib/" + js); //$NON-NLS-1$
			}
		}
		return fullJses;
	}

	public JQueryLibraryInclude(String js, String css, boolean hasNew, PreRenderable... preRenderables)
	{
		this(js, preRenderables);
		addPreRenderer(CssInclude.include(urlHelper.url("css/jquerylib/" + css)).hasNew(hasNew).make()); //$NON-NLS-1$
	}

	public JQueryLibraryInclude(String js, String css, PreRenderable... preRenderables)
	{
		this(js, css, false, preRenderables);
	}

	public JQueryLibraryInclude(String[] jses, String css, PreRenderable... preRenderables)
	{
		this(jses, css, false, preRenderables);
	}

	public JQueryLibraryInclude(String js, PreRenderable... preRenderables)
	{
		super(urlHelper.url("jquerylib/" + js), preRenderables); //$NON-NLS-1$
		addPreRenderer(JQueryCore.PRERENDER);
	}

	/**
	 * @param jses A js file location can be prefixed with '~' if you don't want
	 *            it to look in the jquerylib folder
	 * @param css The css file can be null
	 * @param rtl Css file has an rtl equivelant
	 * @param preRenderables
	 */
	public JQueryLibraryInclude(String[] jses, String css, boolean rtl, PreRenderable... preRenderables)
	{
		super(addLibFolder(jses));
		addPreRenderers(Arrays.asList(preRenderables));
		if( css != null )
		{
			addPreRenderer(new CssInclude(urlHelper.url("css/jquerylib/" + css), rtl)); //$NON-NLS-1$
		}
		addPreRenderer(JQueryCore.PRERENDER);
	}

}
