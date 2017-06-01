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

package com.dytech.edge.admin.script.basicmodel;

import com.dytech.edge.admin.script.ifmodel.Comparison;
import com.dytech.edge.admin.script.ifmodel.Equality;
import com.dytech.edge.admin.script.ifmodel.IfModel;
import com.tle.common.i18n.CurrentLocale;

public class WorkflowStepComparison implements Comparison
{
	private final Equality op;
	private final String value;
	private final String name;

	public WorkflowStepComparison(Equality op, String value, String name)
	{
		this.op = op;
		this.value = value;
		this.name = name;
	}

	public Equality getOperation()
	{
		return op;
	}

	public String getValue()
	{
		return value;
	}

	@Override
	public String toScript()
	{
		return "workflowstep " + op.toScript() + " '" + IfModel.encode(value) + "'";
	}

	@Override
	public String toEasyRead()
	{
		return CurrentLocale.get("com.dytech.edge.admin.script.target.workflow") + " " + op.toEasyRead() + " '" + name
			+ "'";
	}
}
