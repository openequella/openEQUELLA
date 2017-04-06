/*
 * Created on Aug 11, 2005
 */
package com.tle.common.search.whereparser;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import junit.framework.TestCase;

import com.tle.common.Pair;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.CurrentLocale.AbstractCurrentLocale;
import com.tle.common.util.Dates;
import com.tle.common.util.UtcDate;
import com.tle.core.freetext.queries.FreeTextBooleanQuery;
import com.tle.core.freetext.queries.FreeTextDateQuery;
import com.tle.core.freetext.queries.FreeTextFieldQuery;

@SuppressWarnings("nls")
public class WhereParserTest extends TestCase
{
	public WhereParserTest()
	{
		CurrentLocale.initialise(new AbstractCurrentLocale()
		{
			@Override
			public Locale getLocale()
			{
				return Locale.ENGLISH;
			}

			@Override
			public ResourceBundle getResourceBundle()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			protected Pair<Locale, String> resolveKey(String key)
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean isRightToLeft()
			{
				throw new UnsupportedOperationException();
			}

		});
	}

	public void testQuiteBigWhereClause() throws Exception
	{
		WhereParser.parse("WHERE /xml/item/@id = 'blah' AND /xml/item/something = 'blah2'"
			+ " OR /xml/item/another NOT IN ('v1', 'v2','v3') AND (NOT /xml/item/a LIKE 'part%'"
			+ " OR ((((/xml/item/who.cares EXISTS)))) AND /xml/item/null IS NULL)"
			+ " AND /xml/item/notnull IS NOT NULL");
	}

	public void testTooManyClosingBrackets() throws Exception
	{
		try
		{
			WhereParser.parse("where (/xml/blah EXISTS))");
			assertTrue("The where clause is invalid, yet the parse still liked it", false);
		}
		catch( InvalidWhereException ex )
		{
			// This is supposed to happen
		}
	}

	public void testSimpleOpEquals() throws Exception
	{
		simpleOpHelper("=", Operator.EQUALS);
	}

	public void testSimpleOpIs() throws Exception
	{
		simpleOpHelper("is", Operator.EQUALS);
	}

	public void testSimpleOpNotEquals() throws Exception
	{
		simpleOpHelper("<>", Operator.NOT_EQUALS);
	}

	public void testSimpleOpIsNot() throws Exception
	{
		simpleOpHelper("is not", Operator.NOT_EQUALS);
	}

	public void testSimpleOpLessThan() throws Exception
	{
		simpleOpHelper("<", Operator.LESS_THAN);
	}

	public void testSimpleOpLessThanOrEqualTo() throws Exception
	{
		simpleOpHelper("<=", Operator.LESS_THAN_OR_EQUAL_TO);
	}

	public void testSimpleOpGreaterThan() throws Exception
	{
		simpleOpHelper(">", Operator.GREATER_THAN);
	}

	public void testSimpleOpGreaterThanOrEqualTo() throws Exception
	{
		simpleOpHelper(">=", Operator.GREATER_THAN_OR_EQUAL_TO);
	}

	public void testSimpleOpLike() throws Exception
	{
		simpleOpHelper("like", Operator.LIKE);
	}

	public void testSimpleOpNotLike() throws Exception
	{
		simpleOpHelper("not like", Operator.NOT_LIKE);
	}

	public void testSimpleOpIn() throws Exception
	{
		simpleOpHelper("in", Operator.IN);
	}

	public void testSimpleOpNotIn() throws Exception
	{
		simpleOpHelper("not in", Operator.NOT_IN);
	}

	public void simpleOpHelper(String strOp, Operator operator) throws Exception
	{
		String field = "/item/metadata";
		String node = "/xml" + field;
		String singleValue = "2005-10-03T00:00:00";
		List<String> groupValues = new ArrayList<String>();
		groupValues.add("aaa");
		groupValues.add("bbb");
		groupValues.add("ccc");

		StringBuilder where = new StringBuilder();
		where.append("where ");
		where.append(node);
		where.append(' ');
		where.append(strOp);
		where.append(' ');
		if( operator != Operator.IN && operator != Operator.NOT_IN )
		{
			where.append('\'');
			where.append(singleValue);
			where.append('\'');
		}
		else
		{
			where.append('(');
			for( Iterator<String> iter = groupValues.iterator(); iter.hasNext(); )
			{
				where.append('\'');
				where.append(iter.next());
				where.append('\'');

				if( iter.hasNext() )
				{
					where.append(", ");
				}
			}
			where.append(')');
		}

		// Get the 'or' top level
		FreeTextBooleanQuery result = WhereParser.parse(where.toString());
		assertEquals(result.getClauses().size(), 1);
		assertFalse(result.isAnd());
		assertFalse(result.isNot());

		// Get the 'and' child
		assertTrue(result.getClauses().get(0) instanceof FreeTextBooleanQuery);
		result = (FreeTextBooleanQuery) result.getClauses().get(0);
		assertTrue(result.isAnd());
		assertFalse(result.isNot());
		assertEquals(result.getClauses().size(), 1);

		if( operator != Operator.IN && operator != Operator.NOT_IN )
		{
			if( operator.isNot() )
			{
				// Get the 'not' child
				assertTrue(result.getClauses().get(0) instanceof FreeTextBooleanQuery);
				result = (FreeTextBooleanQuery) result.getClauses().get(0);
				assertTrue(result.isNot());
				assertEquals(result.getClauses().size(), 1);
			}

			// Get the 'field' child
			if( operator == Operator.GREATER_THAN || operator == Operator.GREATER_THAN_OR_EQUAL_TO
				|| operator == Operator.LESS_THAN || operator == Operator.LESS_THAN_OR_EQUAL_TO )
			{
				assertTrue(result.getClauses().get(0) instanceof FreeTextDateQuery);
				FreeTextDateQuery clause = (FreeTextDateQuery) result.getClauses().get(0);
				assertEquals(clause.getField(), field);

				Date date = new UtcDate(singleValue, Dates.ISO).toDate();
				if( operator == Operator.GREATER_THAN || operator == Operator.GREATER_THAN_OR_EQUAL_TO )
				{
					assertEquals(clause.getStart().toDate(), date);
					assertNull(clause.getEnd());
				}
				else
				{
					assertNull(clause.getStart());
					assertEquals(clause.getEnd().toDate(), date);
				}
			}
			else
			{
				assertTrue(result.getClauses().get(0) instanceof FreeTextFieldQuery);
				FreeTextFieldQuery clause = (FreeTextFieldQuery) result.getClauses().get(0);
				assertEquals(clause.getField(), field);
				assertEquals(clause.getValue(), singleValue);
			}
		}
		else
		{
			// Get the 'parent for the fields' child
			assertTrue(result.getClauses().get(0) instanceof FreeTextBooleanQuery);
			result = (FreeTextBooleanQuery) result.getClauses().get(0);
			assertEquals(result.isNot(), operator.isNot());
			assertEquals(result.getClauses().size(), groupValues.size());

			for( int i = 0; i < groupValues.size(); i++ )
			{
				assertTrue(result.getClauses().get(i) instanceof FreeTextFieldQuery);
				FreeTextFieldQuery clause = (FreeTextFieldQuery) result.getClauses().get(i);
				assertEquals(clause.getField(), field);
				assertEquals(clause.getValue(), groupValues.get(i));
			}
		}
	}
}
