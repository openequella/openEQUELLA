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

public class XpathComparison implements Comparison
{
	protected String lhs;
	protected String rhs;
	protected Equality operator;

	public XpathComparison(Equality operator, String lhs, String rhs)
	{
		setLHS(lhs);
		setRHS(rhs);
		setOperator(operator);
	}

	public String getLHS()
	{
		return lhs;
	}

	public void setLHS(String lhs)
	{
		this.lhs = lhs;
	}

	public String getRHS()
	{
		return rhs;
	}

	public void setRHS(String rhs)
	{
		this.rhs = rhs;
	}

	public Equality getOperator()
	{
		return operator;
	}

	public void setOperator(Equality operator)
	{
		this.operator = operator;
	}

	@Override
	public String toScript()
	{
		return "xml.get('" + lhs + "') " + operator.toScript() + " '" + IfModel.encode(rhs) + "'";
	}

	@Override
	public String toEasyRead()
	{
		return lhs + " " + operator.toEasyRead() + " '" + rhs + "'";
	}
}
