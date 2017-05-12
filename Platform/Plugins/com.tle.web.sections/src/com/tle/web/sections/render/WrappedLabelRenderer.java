package com.tle.web.sections.render;

import java.io.IOException;
import java.util.Map;

import com.tle.common.Check;
import com.tle.web.sections.SectionWriter;

public class WrappedLabelRenderer extends TagRenderer
{
	private final WrappedLabel label;
	private String text;

	public WrappedLabelRenderer(WrappedLabel label)
	{
		super(label.isInline() ? "span" : "div", new TagState());
		this.label = label;
		addClass("wrapped");
	}

	@Override
	protected void prepareFirstAttributes(SectionWriter writer, Map<String, String> attrs) throws IOException
	{
		if( label.isShowAltText() )
		{
			attrs.put("title", label.getUnprocessedLabel().getText());
		}
		super.prepareFirstAttributes(writer, attrs);
	}

	@Override
	protected void writeStart(SectionWriter writer, Map<String, String> attrs) throws IOException
	{
		text = label.getText();
		if( !Check.isEmpty(text) )
		{
			super.writeStart(writer, attrs);
		}
	}

	@Override
	protected void writeMiddle(SectionWriter writer) throws IOException
	{
		if( !Check.isEmpty(text) )
		{
			if( label.isHtml() )
			{
				writer.write(label.getText());
			}
			else
			{
				writer.writeText(label.getText());
			}
		}
	}

	@Override
	protected void writeEnd(SectionWriter writer) throws IOException
	{
		if( !Check.isEmpty(text) )
		{
			super.writeEnd(writer);
		}
	}
}
