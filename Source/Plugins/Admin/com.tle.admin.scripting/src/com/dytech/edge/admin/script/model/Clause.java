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

package com.dytech.edge.admin.script.model;

import java.util.ArrayList;
import java.util.List;

public class Clause
{
	protected Term first;
	protected List<OpTerm> opterms;

	public Clause()
	{
		first = null;
		opterms = new ArrayList<OpTerm>();
	}

	public void setFirst(Term term)
	{
		first = term;
	}

	public void add(OpTerm opterm)
	{
		opterms.add(opterm);
		opterm.setParent(this);
	}

	public void remove(OpTerm opterm)
	{
		opterms.remove(opterm);
	}

	public Term getFirst()
	{
		return first;
	}

	public void insert(OpTerm opterm, int index)
	{
		if( index >= opterms.size() )
		{
			add(opterm);
		}
		else
		{
			opterms.add(index, opterm);
			opterm.setParent(this);
		}
	}

	public int indexOf(OpTerm opterm)
	{
		return opterms.indexOf(opterm);
	}

	public List<OpTerm> getOpTerms()
	{
		return opterms;
	}

	public String toScript()
	{
		StringBuilder script = new StringBuilder();

		script.append(first.toScript());
		script.append(" ");

		for( OpTerm opterm : opterms )
		{
			script.append(opterm.getOperator().toScript());
			script.append(" ");
			script.append(opterm.getTerm().toScript());
			script.append(" ");
		}

		return script.toString();
	}
}
