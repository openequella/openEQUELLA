package com.tle.web.wizard.render;

import java.io.IOException;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.render.AbstractBufferedRenderable;
import com.tle.web.sections.render.SectionRenderable;

@NonNullByDefault
public class DefaultWizardResult extends AbstractBufferedRenderable implements WizardSectionResult
{
	private SectionRenderable title;
	private SectionRenderable html;
	private SectionRenderable tail;

	public DefaultWizardResult(SectionRenderable title, SectionRenderable html, SectionRenderable tail)
	{
		this.title = title;
		this.html = html;
		this.tail = tail;
	}

	@Override
	public SectionRenderable getTitle()
	{
		return title;
	}

	public void setTitle(SectionRenderable title)
	{
		this.title = title;
	}

	@Override
	public SectionRenderable getHtml()
	{
		return html;
	}

	public void setHtml(SectionRenderable html)
	{
		this.html = html;
	}

	public SectionRenderable getTail()
	{
		return tail;
	}

	public void setTail(SectionRenderable tail)
	{
		this.tail = tail;
	}

	@Override
	public void render(SectionWriter writer) throws IOException
	{
		title.realRender(writer);
		html.realRender(writer);
		tail.realRender(writer);
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		info.preRender(title, html, tail);
		super.preRender(info);
	}
}
