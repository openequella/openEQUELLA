package com.tle.web.sections.standard.renderers;

import java.io.IOException;
import java.util.Map;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.js.ElementId;

@SuppressWarnings("nls")
@NonNullByDefault
public class LabelTagRenderer extends DivRenderer
{
	@Nullable
	private final ElementId labelFor;

	public LabelTagRenderer(@Nullable ElementId labelFor, @Nullable String styleClass, Object text)
	{
		super("label", styleClass, text);
		this.labelFor = labelFor;
	}

	@Override
	protected Map<String, String> prepareAttributes(SectionWriter writer) throws IOException
	{
		Map<String, String> attrs = super.prepareAttributes(writer);
		if( labelFor != null )
		{
			attrs.put("for", labelFor.getElementId(writer));
		}
		return attrs;
	}
}
