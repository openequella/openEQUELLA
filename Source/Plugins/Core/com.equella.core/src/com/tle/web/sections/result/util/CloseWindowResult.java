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

package com.tle.web.sections.result.util;

import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.statement.StatementBlock;
import com.tle.web.sections.render.PreRenderable;

public class CloseWindowResult implements PreRenderable
{
	private final JSStatements statements;

	public CloseWindowResult(JSStatements... statements)
	{
		this.statements = StatementBlock.get(statements);
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		info.addReadyStatements(statements);
	}
}
