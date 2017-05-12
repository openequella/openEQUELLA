package com.tle.web.sections.render;

import com.tle.web.sections.SectionUtils;

public class AppendedLabel implements Label
{
	private Label first;
	private Label second;
	private boolean html;

	AppendedLabel(Label first, Label second)
	{
		this.first = first;
		this.second = second;
		html = first.isHtml() || second.isHtml();
	}

	@Override
	public String getText()
	{
		return textEnc(first) + textEnc(second);
	}

	private String textEnc(Label label)
	{
		if( html && !label.isHtml() )
		{
			return SectionUtils.ent(label.getText());
		}
		return label.getText();
	}

	@Override
	public boolean isHtml()
	{
		return html;
	}

	public static Label get(Label first, Label second, Label seperator)
	{
		if( first == null )
		{
			return second;
		}
		if( second == null )
		{
			return first;
		}
		if( seperator == null )
		{
			return new AppendedLabel(first, second);
		}
		return new AppendedLabel(first, new AppendedLabel(seperator, second));
	}

	public static Label get(Label first, Label second)
	{
		return get(first, second, null);
	}

}
