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

package com.tle.admin.search.searchset.scripting;

import com.dytech.edge.admin.script.model.Clause;
import com.dytech.edge.admin.script.model.Node;

public class Block extends Node
{
	protected Clause clause;
	protected Node parent;

	public Clause getClause()
	{
		return clause;
	}

	public void setClause(Clause clause)
	{
		this.clause = clause;
	}

	public String toScript()
	{
		return clause.toScript();
	}

	public String toEasyRead()
	{
		return "<b>where</b> ";
	}
}
