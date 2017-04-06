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
