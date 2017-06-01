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

import java.io.Reader;
import java.text.ParseException;

import com.dytech.common.text.AbstractTopDownParser;
import com.dytech.edge.admin.script.InvalidScriptException;
import com.dytech.edge.admin.script.model.Clause;
import com.dytech.edge.admin.script.model.OpTerm;
import com.dytech.edge.admin.script.model.Term;

/**
 * "Where" Statement BNF: STATEMENT ::= TERM (OPERATOR TERM)* TERM ::= BRACKETS
 * | COMPARISON BRACKETS ::= "(" STATEMENT ")" COMPARISON ::= XPATH EQUALITY
 * VALUE XPATH ::= (ALPHA | NUMBER | "/" | "." | "_" | "@")+ EQUALITY ::= "=" |
 * "<>" | "LIKE" VALUE ::= "'" STRING "'" OPERATOR ::= "&&" | "||" STRING ::=
 * (ALPHA | [0-9] | ...)* ALPHA ::= [a-zA-Z] NUMBER ::= [0-9]
 */
public class WhereParser extends AbstractTopDownParser
{
	// // CONSTRUCTORS
	// //////////////////////////////////////////////////////////

	public WhereParser(Reader in)
	{
		super(in);
	}

	// // STATEMENT PARSER METHODS
	// //////////////////////////////////////////////

	/**
	 * Imports a statement.
	 */
	@SuppressWarnings("nls")
	public Statement importScript() throws InvalidScriptException
	{
		try
		{
			getChar();

			// Sonar wants us to close after use, but seeing as it's the return
			// value from this method, closing it here doesn't sound logical
			Statement s = new Statement(); // NOSONAR
			s.setBlock(getBlock());

			return s;
		}
		catch( ParseException ex )
		{
			throw new InvalidScriptException("Error parsing script", ex);
		}
	}

	/**
	 * BLOCK ::= CLAUSE
	 */
	protected Block getBlock() throws InvalidScriptException, ParseException
	{
		Block b = new Block();
		b.setClause(getClause());
		return b;
	}

	/**
	 * CLAUSE ::= TERM (OPERATOR TERM)* OPERATOR ::= "&&" | "||"
	 */
	@SuppressWarnings("nls")
	protected Clause getClause() throws InvalidScriptException, ParseException
	{
		Clause c = new Clause();
		c.setFirst(getTerm());

		while( look == 'A' || look == 'O' )
		{
			OpTerm o;

			if( look == 'A' )
			{
				match("AND");
				o = new OpTerm(new AndOperator(), getTerm());
			}
			else
			{
				match("OR");
				o = new OpTerm(new OrOperator(), getTerm());
			}

			c.add(o);
		}

		return c;
	}

	/**
	 * TERM ::= BRACKETS | COMPARISON
	 */
	protected Term getTerm() throws InvalidScriptException, ParseException
	{
		if( look == '(' )
		{
			return getBrackets();
		}
		else
		{
			return getComparison();
		}
	}

	/**
	 * COMPARISON ::= XPATH EQUALITY VALUE
	 */
	protected Comparison getComparison() throws InvalidScriptException, ParseException
	{
		String xpath = getXpath();
		Equality op = getEquality();
		String value = getValue();
		return new Comparison(op, xpath, value);
	}

	/**
	 * BRACKETS ::= "(" CLAUSE ")"
	 */
	protected Brackets getBrackets() throws InvalidScriptException, ParseException
	{
		match('(');
		Brackets b = new Brackets();
		b.setClause(getClause());
		match(')');
		return b;
	}

	/**
	 * EQUALITY ::= "=" | "<>" | "LIKE"
	 */
	@SuppressWarnings("nls")
	protected Equality getEquality() throws InvalidScriptException, ParseException
	{
		switch( look )
		{
			case '=':
				match('=');
				return new Equals();

			case '<':
				match("<>");
				return new NotEquals();

			case 'L':
				match("LIKE");
				return new Like();

			default:
				throw new InvalidScriptException("Equality not found: Last character was '" + look + "'");
		}
	}

	/**
	 * XPATH ::= "/xml/"? (ALPHA | NUMBER | "/" | '.' | ':' | '_' | '@')+
	 */
	@SuppressWarnings("nls")
	protected String getXpath() throws InvalidScriptException, ParseException
	{
		// This is hack number 1. It should always match /xml
		// first, but some old scripts don't have it.
		if( look == '/' )
		{
			match("/xml");
		}

		// This is hack number 2. If the Xpath does not
		// begin with a slash, we have to add it.
		StringBuilder s = new StringBuilder();
		if( look != '/' )
		{
			s.append('/');
		}

		while( Character.isLetterOrDigit(look) || "/.:_@".indexOf(look) > 0 )
		{
			s.append(look);
			getChar();
		}

		// This is hack number 3 such that hack number 2 does not
		// cause problems. This used to check if length == 0, but
		// hack 2 may cause length == 1 such that the xpath is '/'.
		// This is not valid either :)
		if( s.length() <= 1 )
		{
			throw new InvalidScriptException("Not a valid Xpath");
		}
		skipWhiteSpace();
		return s.toString();
	}

	/**
	 * VALUE ::= STRING
	 */
	@SuppressWarnings("nls")
	protected String getValue() throws InvalidScriptException, ParseException
	{
		StringBuilder s = new StringBuilder();

		if( look != '\'' )
		{
			throw new InvalidScriptException("Found '" + look + "' but expected a single quote");
		}
		getChar();

		boolean finished = false;
		while( !finished )
		{
			while( look != '\'' )
			{
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

		// Since we are not finishing with a match(), then we
		// have to skip the whitespace manually.
		skipWhiteSpace();

		return s.toString();
	}
}
