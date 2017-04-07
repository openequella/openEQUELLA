package com.tle.web.sections.js.generic.expression;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.Check;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSUtils;
import com.tle.web.sections.render.Label;

@NonNullByDefault
public class LabelExpression extends AbstractExpression
{
	protected final Label label;
	protected final boolean undefinedIfEmpty;

	public LabelExpression(Label label)
	{
		this(label, false);
	}

	public LabelExpression(Label label, boolean undefinedIfEmpty)
	{
		this.label = label;
		this.undefinedIfEmpty = undefinedIfEmpty;
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		// nothing
	}

	@SuppressWarnings("nls")
	@Override
	public String getExpression(@Nullable RenderContext info)
	{
		String text = label.getText();
		if( undefinedIfEmpty && Check.isEmpty(text) )
		{
			return "undefined";
		}
		return JSUtils.toJSString(text);
	}
}
