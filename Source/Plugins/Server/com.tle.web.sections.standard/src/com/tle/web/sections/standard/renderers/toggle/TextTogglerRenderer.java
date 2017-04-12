package com.tle.web.sections.standard.renderers.toggle;

import java.io.IOException;

import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.standard.model.HtmlBooleanState;

public class TextTogglerRenderer extends AbstractHiddenToggler
{
	private String checkedText;
	private String uncheckedText;

	public TextTogglerRenderer(HtmlBooleanState state)
	{
		super(state);
	}

	public void setCheckedText(String checkedText)
	{
		this.checkedText = checkedText;
	}

	public void setUncheckedText(String uncheckedText)
	{
		this.uncheckedText = uncheckedText;
	}

	@Override
	protected void writeMiddle(SectionWriter writer) throws IOException
	{
		if( bstate.isChecked() )
		{
			writer.write(checkedText);
		}
		else
		{
			writer.write(uncheckedText);
		}
	}
}
