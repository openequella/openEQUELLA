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

package com.tle.web.sections.standard.js.impl;

import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.expression.ScriptVariable;
import com.tle.web.sections.js.generic.function.SimpleFunction;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.js.generic.statement.StatementBlock;
import com.tle.web.sections.standard.js.JSDisableable;

public class DisableComponentsFunction extends SimpleFunction
{
	private static final ScriptVariable DISABLE_PARAM = new ScriptVariable("disable"); //$NON-NLS-1$

	public DisableComponentsFunction(String name, JSDisableable... components)
	{
		super(name, createScript(components), DISABLE_PARAM);
	}

	private static JSStatements createScript(JSDisableable... components)
	{
		StatementBlock block = new StatementBlock();
		for( JSDisableable disableable : components )
		{
			block.addStatements(new FunctionCallStatement(disableable.createDisableFunction(), DISABLE_PARAM));
		}
		return block;
	}
}
