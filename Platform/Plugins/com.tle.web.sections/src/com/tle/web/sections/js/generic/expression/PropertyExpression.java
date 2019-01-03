/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.web.sections.js.generic.expression;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSPropertyExpression;

@NonNullByDefault
public class PropertyExpression extends AbstractExpression implements JSPropertyExpression
{
	private final JSExpression property;

	public PropertyExpression(String property)
	{
		this.property = new ScriptExpression(property);
	}

	public PropertyExpression(JSExpression property)
	{
		this.property = property;
	}

	@Override
	public String getExpression(RenderContext info)
	{
		return '.' + property.getExpression(info);
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		info.preRender(property);
	}

	public static CombinedExpression create(JSExpression expr, String prop)
	{
		return new CombinedExpression(expr, new PropertyExpression(prop));
	}

	public static CombinedExpression create(JSExpression expr, JSExpression prop)
	{
		return new CombinedExpression(expr, new PropertyExpression(prop));
	}
}
