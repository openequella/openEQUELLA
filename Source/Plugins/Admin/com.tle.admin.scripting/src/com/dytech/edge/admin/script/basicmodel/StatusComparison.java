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

public class StatusComparison implements Comparison
{
	protected Equality op;
	protected String value;

	public StatusComparison(Equality op, String value)
	{
		this.op = op;
		this.value = value;
	}

	public Equality getOperation()
	{
		return op;
	}

	public void setOp(Equality op)
	{
		this.op = op;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	@Override
	public String toScript()
	{
		return "status " + op.toScript() + " '" + IfModel.encode(value) + "'";
	}

	@Override
	public String toEasyRead()
	{
		return CurrentLocale.get("com.dytech.edge.admin.script.target.status") + " " + op.toEasyRead() + " '" + value
			+ "'";
	}
}
