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

package com.tle.core.remoterepo.z3950;

import java.util.ArrayList;
import java.util.List;

import com.tle.core.remoterepo.z3950.Z3950Constants.Operator;

/**
 * @author Aaron
 */
public class AdvancedSearchOptions
{
	private final List<ExtraQuery> extra = new ArrayList<ExtraQuery>();

	public void addExtra(String attributes, String term, Operator operator)
	{
		ExtraQuery e = new ExtraQuery();
		e.setAttributes(attributes);
		e.setTerm(term);
		e.setOperator(operator);
		extra.add(e);
	}

	public List<ExtraQuery> getExtra()
	{
		return extra;
	}

	public static class ExtraQuery
	{
		private String attributes;
		private String term;
		private Operator operator;

		public String getAttributes()
		{
			return attributes;
		}

		public void setAttributes(String attributes)
		{
			this.attributes = attributes;
		}

		public String getTerm()
		{
			return term;
		}

		public void setTerm(String term)
		{
			this.term = term;
		}

		public Operator getOperator()
		{
			return operator != null ? operator : Operator.AND;
		}

		public void setOperator(Operator operator)
		{
			this.operator = operator;
		}
	}
}
