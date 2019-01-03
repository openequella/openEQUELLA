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

package com.tle.web.sections.js.generic.function;

import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSStatements;

public class SimpleFunctionDefinition implements FunctionDefinition
{
	private final String name;
	private final ElementId elementId;
	private final JSStatements body;
	private final JSExpression[] paramDefs;

	public SimpleFunctionDefinition(String name, ElementId elementId, JSStatements body, JSExpression[] paramdefs)
	{
		this.name = name;
		this.elementId = elementId;
		this.body = body;
		this.paramDefs = paramdefs;
	}

	@Override
	public String getFunctionName(RenderContext context)
	{
		if( elementId == null )
		{
			return name;
		}
		return name + elementId.getElementId(context);
	}

	@Override
	public JSStatements createFunctionBody(RenderContext context, JSExpression[] params)
	{
		return body;
	}

	@Override
	public JSExpression[] getFunctionParams(RenderContext context)
	{
		return paramDefs;
	}
}
