package com.tle.web.sections.render;

import java.util.HashMap;
import java.util.Map;

import com.tle.web.sections.NamedSectionResult;
import com.tle.web.sections.events.RenderContext;

public class GenericTemplateResult implements TemplateResult
{
	private final Map<String, SectionRenderable> results = new HashMap<String, SectionRenderable>();

	public GenericTemplateResult()
	{
		// nothing
	}

	public GenericTemplateResult(NamedSectionResult... results)
	{
		for( NamedSectionResult result : results )
		{
			addNamedResult(result);
		}
	}

	public void addNamedResult(NamedSectionResult result)
	{
		addNamedResult(result.getName(), result);
	}

	public GenericTemplateResult addNamedResult(String name, SectionRenderable result)
	{
		SectionRenderable current = results.get(name);
		if( current != null )
		{
			result = CombinedRenderer.combineResults(current, result);
		}
		results.put(name, result);
		return this;
	}

	@Override
	public TemplateRenderable getNamedResult(RenderContext info, String name)
	{
		SectionRenderable renderable = results.get(name);
		if( renderable != null )
		{
			return new WrappedTemplateRenderable(renderable);
		}
		return null;
	}

	@Override
	public String toString()
	{
		return results.toString();
	}
}
