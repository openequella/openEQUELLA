package com.tle.web.discoverability.scripting;

import java.util.Map;

import javax.inject.Singleton;

import com.tle.common.scripting.service.ScriptContextCreationParams;
import com.tle.core.guice.Bind;
import com.tle.core.scripting.service.ScriptObjectContributor;
import com.tle.web.discoverability.scripting.impl.MetaScriptWrapper;
import com.tle.web.discoverability.scripting.objects.MetaScriptObject;
import com.tle.web.sections.events.PreRenderContext;

/**
 * @author aholland
 */
@Bind
@Singleton
public class DiscoverabilityScriptObjectContributor implements ScriptObjectContributor
{
	@Override
	public void addScriptObjects(Map<String, Object> objects, ScriptContextCreationParams params)
	{

		final PreRenderContext render = (PreRenderContext) params.getAttributes().get("context"); //$NON-NLS-1$
		if( render != null )
		{
			objects.put(MetaScriptObject.DEFAULT_VARIABLE, new MetaScriptWrapper(render));
		}
	}
}
