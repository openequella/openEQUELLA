package com.tle.web.sections.js.generic.function;

import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.JSExpression;

@SuppressWarnings("nls")
public final class DoNothing
{
	public static final JSCallAndReference FUNCTION = new JSCallAndReference()
	{
		@Override
		public String getExpression(RenderContext info)
		{
			return "(function(){})";
		}

		@Override
		public void preRender(PreRenderContext info)
		{
			// nothing
		}

		@Override
		public int getNumberOfParams(RenderContext context)
		{
			return 0;
		}

		@Override
		public boolean isStatic()
		{
			return true;
		}

		@Override
		public String getExpressionForCall(RenderContext info, JSExpression... params)
		{
			return "";
		}
	};

	private DoNothing()
	{
		throw new Error();
	}
}
