package com.tle.web.sections.render;

import java.io.IOException;

import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;

public class LabelRenderer implements SectionRenderable
{
	protected final Label label;

	public LabelRenderer(Label label)
	{
		this.label = label;
		if( label == null )
		{
			throw new NullPointerException();
		}
	}

	@Override
	public void realRender(SectionWriter writer) throws IOException
	{
		String text = label.getText();
		if( text != null )
		{
			if( label.isHtml() )
			{
				writer.write(text);
			}
			else
			{
				writer.writeText(text);
			}
		}
	}

	@Override
	public String toString()
	{
		if( label.isHtml() )
		{
			return label.getText();
		}
		return SectionUtils.ent(label.getText());
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		// nothing
	}
}
