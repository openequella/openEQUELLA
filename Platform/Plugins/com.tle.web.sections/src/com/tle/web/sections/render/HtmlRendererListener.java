package com.tle.web.sections.render;

import com.tle.web.sections.Section;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.RenderEventListener;
import com.tle.web.sections.registry.handler.TargetedListener;

public class HtmlRendererListener extends TargetedListener implements RenderEventListener
{

	private HtmlRenderer renderer;

	public HtmlRendererListener(String id, Section section, SectionTree tree)
	{
		super(id, section, tree);
		this.renderer = (HtmlRenderer) section;
	}

	@Override
	public void render(RenderEventContext context)
	{
		try
		{
			if( renderer instanceof ModalRenderer )
			{
				if( ((ModalRenderer) renderer).isModal(context) )
				{
					return;
				}
			}
			SectionResult result = renderer.renderHtml(context);
			if( result != null )
			{
				context.getRenderEvent().returnResult(result);
			}
		}
		catch( Exception e )
		{
			SectionUtils.throwRuntime(e);
		}
	}
}
