package com.tle.web.sections.header;

import java.io.IOException;

import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.render.NestedRenderable;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TagState;
import com.tle.web.sections.render.TemplateRenderable;

@SuppressWarnings("nls")
public class BodyTag extends TagState implements NestedRenderable
{
	private NestedRenderable renderer = new BufferedTagRenderer("body", this)
	{
		@Override
		protected void writeMiddle(SectionWriter writer) throws IOException
		{
			super.writeMiddle(writer);
			writer.render(postMarkup);
		}
	};

	private TemplateRenderable postMarkup;

	@Override
	public NestedRenderable setNestedRenderable(SectionRenderable nested)
	{
		renderer.setNestedRenderable(nested);
		return this;
	}

	@Override
	public SectionRenderable getNestedRenderable()
	{
		return renderer.getNestedRenderable();
	}

	@Override
	public void realRender(SectionWriter writer) throws IOException
	{
		renderer.realRender(writer);
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		renderer.preRender(info);
	}

	public NestedRenderable getRenderer()
	{
		return renderer;
	}

	public void setRenderer(NestedRenderable renderer)
	{
		this.renderer = renderer;
	}

	public void setPostmarkup(TemplateRenderable postMarkup)
	{
		this.postMarkup = postMarkup;
	}
}
