package com.tle.web.sections.standard.renderers.list;

import java.io.IOException;
import java.util.Map;

import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.jquery.JQuerySelector;
import com.tle.web.sections.jquery.libraries.JQueryStarRating;
import com.tle.web.sections.js.generic.expression.ObjectExpression;
import com.tle.web.sections.js.generic.expression.PropertyExpression;
import com.tle.web.sections.js.generic.expression.ScriptExpression;
import com.tle.web.sections.standard.model.HtmlListState;

@SuppressWarnings("nls")
public class StarRatingListRenderer extends DropDownRenderer
{
	public StarRatingListRenderer(HtmlListState listState)
	{
		super(listState);
	}

	@Override
	protected void writeStart(SectionWriter writer, Map<String, String> attrs) throws IOException
	{
		// The JQuery library requires a DIV wrapper
		writer.writeTag("div");
		super.writeStart(writer, attrs);
	}

	@Override
	protected void writeEnd(SectionWriter writer) throws IOException
	{
		super.writeEnd(writer);
		writer.endTag("div");
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		super.preRender(info);

		final ObjectExpression oe = new ObjectExpression();
		oe.put("inputType", "select");

		JQueryStarRating.starRating(info,
			PropertyExpression.create(new JQuerySelector(this), new ScriptExpression("parent()")), oe);
	}
}
