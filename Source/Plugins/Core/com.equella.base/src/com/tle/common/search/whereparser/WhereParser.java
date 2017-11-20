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

package com.tle.common.search.whereparser;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.dytech.common.text.AbstractTopDownParser;
import com.dytech.edge.queries.FreeTextQuery;
import com.tle.common.util.DateHelper;
import com.tle.common.util.Dates;
import com.tle.common.util.UtcDate;
import com.tle.core.freetext.queries.FreeTextBooleanQuery;
import com.tle.core.freetext.queries.FreeTextDateQuery;
import com.tle.core.freetext.queries.FreeTextFieldExistsQuery;
import com.tle.core.freetext.queries.FreeTextFieldQuery;

/**
 * Basic "Where" Statement BNF: WHERE STATEMENT ::= "where"? BOOLEAN_EXPR
 * BOOLEAN_EXPR ::= OR_BOOLEAN_EXPR OR_BOOLEAN_EXPR ::= AND_BOOLEAN_EXPR ("or"
 * AND_BOOLEAN_EXPR)* AND_BOOLEAN_EXPR ::= CLAUSE ("and" CLAUSE)* CLAUSE ::=
 * "not" CLAUSE | BRACKETS | COMPARISON | EXISTS_CLAUSE BRACKETS ::= "("
 * BOOLEAN_EXPR ")" COMPARISON ::= XPATH COMPARISON_OP COMPARISON_RHS
 * EXISTS_CLAUSE ::= XPATH "exists" XPATH ::= "/" (ALPHA | NUMBER | [/._:@])+
 * COMPARISON_OP ::= "=" | "is" | "<>" | "is not" | "<" | "<=" | ">" | ">=" |
 * "like" | "not like" | "in" | "not in" COMPARISON_RHS ::= "null" |
 * NUMBER_VALUE | STRING_VALUE | GROUP_VALUE STRING_VALUE ::= "'" STRING "'"
 * NUMBER_VALUE ::= NUMBER+ GROUP_VALUE ::= "(" STRING_VALUE ("," STRING_VALUE)*
 * ")" STRING ::= (ALPHA | [0-9] | ...)* ALPHA ::= [a-zA-Z] NUMBER ::= [0-9]
 */
public final class WhereParser extends AbstractTopDownParser
{
	private static final String ALLOWED_XPATH_SYMBOLS = "/._:@"; //$NON-NLS-1$

	public static FreeTextBooleanQuery parse(String whereClause)
	{
		if( whereClause == null )
		{
			return null;
		}
		whereClause = whereClause.trim();
		if( whereClause.length() == 0 )
		{
			return null;
		}
		if( whereClause.length() > 5 && whereClause.substring(0, 5).equalsIgnoreCase("where") ) //$NON-NLS-1$
		{
			whereClause = whereClause.substring(5).trim();
		}

		try( Reader reader = new StringReader(whereClause) )
		{
			WhereParser parser = new WhereParser(reader);
			return parser.getFreeTextQuery();
		}
		catch( ParseException | IOException ex )
		{
			throw new InvalidWhereException("Error parsing where clause", ex);
		}
	}

	private WhereParser(Reader in)
	{
		super(in);
	}

	/**
	 * Imports a statement.
	 */
	private FreeTextBooleanQuery getFreeTextQuery() throws ParseException
	{
		getChar();

		FreeTextBooleanQuery result = getBooleanExpr();
		if( !isEOF() )
		{
			throw new InvalidWhereException("Where clause still has remaining text");
		}

		return result;
	}

	/**
	 * BOOLEAN_EXPR ::= OR_BOOLEAN_EXPR OR_BOOLEAN_EXPR ::= AND_BOOLEAN_EXPR
	 * ("or" AND_BOOLEAN_EXPR)*
	 */
	private FreeTextBooleanQuery getBooleanExpr() throws ParseException
	{
		FreeTextBooleanQuery result = new FreeTextBooleanQuery(false, false);

		result.add(getAndBooleanExpr());

		while( isLookAhead('O') )
		{
			match("OR"); //$NON-NLS-1$
			result.add(getAndBooleanExpr());
		}

		return result;
	}

	/**
	 * AND_BOOLEAN_EXPR ::= CLAUSE ("and" CLAUSE)*
	 */
	private FreeTextBooleanQuery getAndBooleanExpr() throws ParseException
	{
		FreeTextBooleanQuery result = new FreeTextBooleanQuery(false, true);

		result.add(getClause());

		while( isLookAhead('A') )
		{
			match("AND"); //$NON-NLS-1$
			result.add(getClause());
		}

		return result;
	}

	/**
	 * CLAUSE ::= "not" CLAUSE | BRACKETS | COMPARISON | EXISTS_CLAUSE
	 */
	private FreeTextQuery getClause() throws ParseException
	{
		if( isLookAhead('N') )
		{
			return getNotClause();
		}
		else if( isLookAhead('(') )
		{
			return getBrackets();
		}

		String field = getXpathAsField();
		if( isLookAhead('E') )
		{
			return getExistsTest(field);
		}
		else
		{
			return getComparison(field);
		}
	}

	/**
	 * NOT_CLAUSE ::= "not" CLAUSE
	 */
	private FreeTextQuery getNotClause() throws ParseException
	{
		match("NOT"); //$NON-NLS-1$
		return wrapWithNotQuery(getClause());
	}

	/**
	 * BRACKETS ::= "(" BOOLEAN_EXPR ")"
	 */
	private FreeTextQuery getBrackets() throws ParseException
	{
		match('(');
		FreeTextQuery query = getBooleanExpr();
		match(')');
		return query;
	}

	/**
	 * EXISTS_CLAUSE ::= XPATH "exists"
	 */
	private FreeTextQuery getExistsTest(String field) throws ParseException
	{
		match("EXISTS"); //$NON-NLS-1$
		return new FreeTextFieldExistsQuery(field);
	}

	/**
	 * COMPARISON ::= XPATH COMPARISON_OP COMPARISON_RHS
	 */
	private FreeTextQuery getComparison(String field) throws ParseException
	{
		Operator op = getComparisonOp();

		if( op == Operator.EQUALS || op == Operator.NOT_EQUALS )
		{
			if( isLookAhead('N') )
			{
				match("NULL"); //$NON-NLS-1$
				// We can't compare this in freetext at the moment, so we are
				// going
				// to simply ignore this clause. This is probably going to come
				// back
				// to kick our ass at some point.
				return null;
			}
			else
			{
				return getFieldQueryForValue(field, op, getValue(), false);
			}
		}
		else if( op == Operator.IN || op == Operator.NOT_IN )
		{
			return getFieldQueryForValues(field, op, getGroupValues());
		}
		else if( op == Operator.LIKE || op == Operator.NOT_LIKE )
		{
			String value = convertXoqlLikeToLucene(getValue());
			return getFieldQueryForValue(field, op, value, value.contains("*")); //$NON-NLS-1$
		}
		else if( op == Operator.LESS_THAN || op == Operator.LESS_THAN_OR_EQUAL_TO || op == Operator.GREATER_THAN
			|| op == Operator.GREATER_THAN_OR_EQUAL_TO )
		{
			String value = getValue();

			FreeTextQuery result = tryToCompareAsDate(field, op, value);
			if( result != null )
			{
				return result;
			}

			throw new InvalidWhereException("Operator currently only works with ISO dates: " + op.toString() + ' '
				+ value);
		}
		else
		{
			throw new InvalidWhereException("Operator is currently not supported: " + op.toString());
		}
	}

	/**
	 * COMPARISON_OP ::= "=" | "is" | "<>" | "is not" | "<" | "<=" | ">" | ">="
	 * | "like" | "not like" | "in" | "not in"
	 */
	private Operator getComparisonOp() throws ParseException
	{
		final String opTokens = "=<>"; //$NON-NLS-1$
		StringBuilder buffer = new StringBuilder();
		while( Character.isLetter(look) || opTokens.indexOf(look) >= 0 )
		{
			buffer.append(look);
			getChar();
		}

		skipWhiteSpace(false);

		String token = buffer.toString().toUpperCase();
		if( token.equals("=") ) //$NON-NLS-1$
		{
			return Operator.EQUALS;
		}
		else if( token.equals("<>") ) //$NON-NLS-1$
		{
			return Operator.NOT_EQUALS;
		}
		else if( token.equals("<") ) //$NON-NLS-1$
		{
			return Operator.LESS_THAN;
		}
		else if( token.equals("<=") ) //$NON-NLS-1$
		{
			return Operator.LESS_THAN_OR_EQUAL_TO;
		}
		else if( token.equals(">") ) //$NON-NLS-1$
		{
			return Operator.GREATER_THAN;
		}
		else if( token.equals(">=") ) //$NON-NLS-1$
		{
			return Operator.GREATER_THAN_OR_EQUAL_TO;
		}
		else if( token.equals("IN") ) //$NON-NLS-1$
		{
			return Operator.IN;
		}
		else if( token.equals("LIKE") ) //$NON-NLS-1$
		{
			return Operator.LIKE;
		}
		else if( token.equals("IS") ) //$NON-NLS-1$
		{
			if( matchTheFuture("NOT") ) //$NON-NLS-1$
			{
				match("NOT"); //$NON-NLS-1$
				return Operator.NOT_EQUALS;
			}
			else
			{
				return Operator.EQUALS;
			}
		}
		else if( token.equals("NOT") ) //$NON-NLS-1$
		{
			if( isLookAhead('L') )
			{
				match("LIKE"); //$NON-NLS-1$
				return Operator.NOT_LIKE;
			}
			else if( isLookAhead('I') )
			{
				match("IN"); //$NON-NLS-1$
				return Operator.NOT_IN;
			}
		}

		throw new InvalidWhereException("Unknown comparison operator '" + token + '\'');
	}

	/**
	 * XPATH ::= "/" (ALPHA | NUMBER | [/._:@])+
	 */
	private String getXpathAsField() throws ParseException
	{
		// The first character should always be a slash.
		match("/xml"); //$NON-NLS-1$

		StringBuilder field = new StringBuilder();
		while( Character.isLetterOrDigit(look) || ALLOWED_XPATH_SYMBOLS.indexOf(look) >= 0 )
		{
			field.append(look);
			getChar();
		}

		skipWhiteSpace(false);

		return field.toString();
	}

	/**
	 * GROUP_VALUE ::= "(" STRING_VALUE ("," STRING_VALUE)* ")"
	 */
	private List<String> getGroupValues() throws ParseException
	{
		List<String> results = new ArrayList<String>();

		match('(');
		results.add(getValue());
		while( isLookAhead(',') )
		{
			match(',');
			results.add(getValue());
		}
		match(')');

		return results;
	}

	/**
	 * Delegates between NUMBER_VALUE | STRING_VALUE
	 */
	private String getValue() throws ParseException
	{
		if( Character.isDigit(look) )
		{
			return getNumberValue();
		}
		else
		{
			return getStringValue();
		}
	}

	/**
	 * NUMBER_VALUE ::= NUMBER+
	 */
	private String getNumberValue() throws ParseException
	{
		StringBuilder s = new StringBuilder();

		if( !Character.isDigit(look) )
		{
			throw new InvalidWhereException("Found '" + look + "' but expected digit");
		}

		do
		{
			s.append(look);
			getChar();
		}
		while( Character.isDigit(look) );

		// Since we are not finishing with a match(),
		// we have to skip the whitespace manually.
		if( Character.isWhitespace(look) )
		{
			skipWhiteSpace();
		}

		return s.toString();
	}

	/**
	 * STRING_VALUE ::= "'" STRING "'"
	 */
	private String getStringValue() throws ParseException
	{
		StringBuilder s = new StringBuilder();

		// We don't want to 'match' the following character, because that would
		// also
		// skip any following white-space that is part of the value.
		if( look != '\'' )
		{
			throw new InvalidWhereException("Found '" + look + "' but expected a single quote");
		}
		getChar();

		boolean finished = false;
		while( !finished )
		{
			while( look != '\'' )
			{
				if( isEOF() )
				{
					throw new InvalidWhereException("Value did not have closing single quote");
				}

				s.append(look);
				getChar();
			}

			getChar();
			if( look == '\'' )
			{
				s.append(look);
				getChar();
			}
			else
			{
				finished = true;
			}
		}

		// Since we are not finishing with a match(),
		// we have to skip the whitespace manually.
		if( Character.isWhitespace(look) )
		{
			skipWhiteSpace();
		}

		return s.toString();
	}

	// // HELPER METHODS //////////////////////////////////////////////////////

	private String convertXoqlLikeToLucene(String likeQuery)
	{
		String value = likeQuery.replace('%', '*').trim();
		if( value.length() == 0 )
		{
			throw new InvalidWhereException("Like queries must not be empty");
		}
		if( value.charAt(0) == '*' || value.charAt(0) == '?' )
		{
			throw new InvalidWhereException("Like queries must not start with wildcard");
		}
		return value;
	}

	private FreeTextQuery wrapWithNotQuery(FreeTextQuery query)
	{
		FreeTextBooleanQuery wrapper = new FreeTextBooleanQuery(true, true);
		wrapper.add(query);
		return wrapper;
	}

	private FreeTextQuery getFieldQueryForValue(String field, Operator op, String value, boolean possibleWildcard)
	{
		FreeTextFieldQuery query = new FreeTextFieldQuery(field, value, true);
		query.setPossibleWildcard(possibleWildcard);

		if( op.isNot() )
		{
			return wrapWithNotQuery(query);
		}
		else
		{
			return query;
		}
	}

	private FreeTextQuery getFieldQueryForValues(String field, Operator op, List<String> values)
	{
		switch( values.size() )
		{
			case 0:
				return null;

			case 1:
				return getFieldQueryForValue(field, op, values.get(0), false);

			default:
				FreeTextBooleanQuery boolquery = new FreeTextBooleanQuery(op.isNot(), false);
				for( String value : values )
				{
					FreeTextQuery query = new FreeTextFieldQuery(field, value, true);
					boolquery.add(query);
				}
				return boolquery;
		}
	}

	private FreeTextDateQuery tryToCompareAsDate(String field, Operator op, String value)
	{
		UtcDate date = DateHelper.parseOrNull(value, Dates.ISO);

		if( date == null )
		{
			date = DateHelper.parseOrNull(value, Dates.ISO_DATE_ONLY);
		}

		if( date == null )
		{
			return null;
		}
		else if( op == Operator.GREATER_THAN || op == Operator.GREATER_THAN_OR_EQUAL_TO )
		{
			return new FreeTextDateQuery(field, date, null, op == Operator.GREATER_THAN_OR_EQUAL_TO, false);
		}
		else
		{
			return new FreeTextDateQuery(field, null, date, false, op == Operator.LESS_THAN_OR_EQUAL_TO);
		}
	}
}
