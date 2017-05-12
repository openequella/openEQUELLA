package com.tle.web.sections.js.generic.function;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.render.PreRenderable;

public class IncludeFile implements PreRenderable
{
	protected List<String> includes = new ArrayList<String>();
	protected List<PreRenderable> preRenderables = new ArrayList<PreRenderable>();

	public IncludeFile()
	{
		// nothing
	}

	public IncludeFile(String include, PreRenderable... preRenderables)
	{
		this.includes.add(include);
		this.preRenderables.addAll(Arrays.asList(preRenderables));
	}

	public IncludeFile(String[] includes)
	{
		this.includes.addAll(Arrays.asList(includes));
	}

	public void addPreRenderers(Collection<PreRenderable> preRenderers)
	{
		preRenderables.addAll(preRenderers);
	}

	public void addPreRenderer(PreRenderable preRenderer)
	{
		preRenderables.add(preRenderer);
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		info.preRender(preRenderables);
		for( String include : includes )
		{
			info.addJs(include);
		}
	}
}
