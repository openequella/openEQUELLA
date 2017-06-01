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
import com.tle.common.i18n.CurrentLocale;

public class ModerationComparison implements Comparison
{
	protected Equality op;
	protected boolean value;

	public ModerationComparison(Equality op, boolean value)
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

	public boolean getValue()
	{
		return value;
	}

	public void setValue(boolean value)
	{
		this.value = value;
	}

	@Override
	public String toScript()
	{
		return "moderationallowed " + op.toScript() + " " + value;
	}

	@Override
	public String toEasyRead()
	{
		return CurrentLocale.get("com.dytech.edge.admin.script.target.moderator") + " " + op.toEasyRead() + " " + value;
	}
}
