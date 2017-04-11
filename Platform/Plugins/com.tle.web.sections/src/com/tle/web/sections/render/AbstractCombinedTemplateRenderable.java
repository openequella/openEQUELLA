/**
 *
 */
package com.tle.web.sections.render;

import java.util.ArrayList;
import java.util.List;

import com.tle.web.sections.events.RenderContext;

public abstract class AbstractCombinedTemplateRenderable implements TemplateRenderable
{
	private List<TemplateRenderable> templateRenderers;
	private Boolean exists;

	protected List<TemplateRenderable> getRenderers(RenderContext context)
	{
		if( templateRenderers == null )
		{
			templateRenderers = new ArrayList<TemplateRenderable>();
			setupTemplateRenderables(context);
		}
		return templateRenderers;
	}

	protected void addTemplateRenderable(TemplateRenderable renderable)
	{
		templateRenderers.add(renderable);
	}

	protected abstract void setupTemplateRenderables(RenderContext context);

	@Override
	public boolean exists(RenderContext context)
	{
		if( exists == null )
		{
			List<TemplateRenderable> renderers = getRenderers(context);
			for( TemplateRenderable templateRenderable : renderers )
			{
				if( templateRenderable != null && templateRenderable.exists(context) )
				{
					exists = true;
					return true;
				}
			}
			exists = false;
		}
		return exists;
	}

}