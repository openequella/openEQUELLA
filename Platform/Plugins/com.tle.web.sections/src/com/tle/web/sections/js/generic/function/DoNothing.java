/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
