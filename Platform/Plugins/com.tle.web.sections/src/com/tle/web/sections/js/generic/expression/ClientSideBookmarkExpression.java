package com.tle.web.sections.js.generic.expression;

import java.util.Map;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.BookmarkAndModify;
import com.tle.web.sections.js.JSBookmarkModifier;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSUtils;

@NonNullByDefault
public class ClientSideBookmarkExpression extends AbstractExpression
{
	private final Map<String, JSExpression> exprs;
	private final JSBookmarkModifier modifier;

	public ClientSideBookmarkExpression(JSBookmarkModifier modifier)
	{
		this.modifier = modifier;
		exprs = modifier.getClientExpressions();
	}

	@Override
	public String getExpression(RenderContext info)
	{
		StringBuilder sbuf = new StringBuilder();
		String href = new BookmarkAndModify(info, modifier).getHref();
		appendString(sbuf, href, false);
		for( String key : exprs.keySet() )
		{
			sbuf.append('+');
			appendString(sbuf, key, true);
			sbuf.append("+escape("); //$NON-NLS-1$
			JSExpression expr = exprs.get(key);
			sbuf.append(expr.getExpression(info));
			sbuf.append(")"); //$NON-NLS-1$
		}
		return sbuf.toString();
	}

	private void appendString(StringBuilder sbuf, String str, boolean param)
	{
		sbuf.append('\'');
		if( param )
		{
			sbuf.append('&');
		}
		sbuf.append(JSUtils.escape(str, false));
		if( param )
		{
			sbuf.append('=');
		}
		sbuf.append('\'');
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		SectionUtils.preRender(info, exprs.values());
	}

}
