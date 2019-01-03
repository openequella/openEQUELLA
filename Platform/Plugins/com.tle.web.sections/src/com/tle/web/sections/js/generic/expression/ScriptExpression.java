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

import java.util.Arrays;
import java.util.List;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.PreRenderable;

@NonNullByDefault
public class ScriptExpression extends AbstractExpression
{
	protected String expr;
	private List<PreRenderable> preRenderers;

	public ScriptExpression(String expr)
	{
		this.expr = expr;
	}

	public void setPreRenderers(PreRenderable... preRenderers)
	{
		this.preRenderers = Arrays.asList(preRenderers);
	}

	public ScriptExpression()
	{
		// dynamic it
	}

	@Override
	public String getExpression(@Nullable RenderContext info)
	{
		return expr;
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		info.preRender(preRenderers);
	}
}
