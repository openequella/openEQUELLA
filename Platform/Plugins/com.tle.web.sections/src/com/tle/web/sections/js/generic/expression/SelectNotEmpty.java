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

package com.tle.web.sections.js.generic.expression;

import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.ElementId;

public class SelectNotEmpty extends CombinedExpression
{
	private boolean skipFirstIndex = false;

	public SelectNotEmpty(ElementId id)
	{
		super(new ElementByIdExpression(id), new PropertyExpression("selectedIndex")); //$NON-NLS-1$
	}

	public SelectNotEmpty(ElementId id, boolean skipFirstIndex)
	{
		super(new ElementByIdExpression(id), new PropertyExpression("selectedIndex")); //$NON-NLS-1$
		this.skipFirstIndex = skipFirstIndex;
	}

	@Override
	public String getExpression(RenderContext info)
	{
		if( skipFirstIndex )
		{
			return super.getExpression(info) + " >= 1"; //$NON-NLS-1$
		}
		else
		{
			return super.getExpression(info) + " >= 0"; //$NON-NLS-1$
		}
	}
}
