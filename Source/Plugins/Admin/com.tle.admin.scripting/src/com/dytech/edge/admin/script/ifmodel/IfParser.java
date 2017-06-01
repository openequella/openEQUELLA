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

package com.dytech.edge.admin.script.ifmodel;

import java.io.Reader;
import java.text.ParseException;

import com.dytech.common.text.AbstractTopDownParser;
import com.dytech.edge.admin.script.InvalidScriptException;
import com.dytech.edge.admin.script.model.Clause;
import com.dytech.edge.admin.script.model.OpTerm;
import com.dytech.edge.admin.script.model.Term;

/**
 * "If" Statement BNF: STATEMENT ::= PRE_TEXT BLOCK ELSE* POST_TEXT BLOCK ::=
 * "if (" CLAUSE ")" BODY ELSE ::= "else" BLOCK CLAUSE ::= TERM (OPERATOR TERM)*
 * TERM ::= BRACKETS | COMPARISON BRACKETS ::= "(" CLAUSE ")" COMPARISON ::= ???
 * XPATH ::= (ALPHA | NUMBER | "/" | "." | "_" | "@")+ EQUALITY ::= "!=" | "=="
 * | "<" | "<=" | ">" | ">=" VALUE ::= "'" STRING "'" OPERATOR ::= "&&" | "||"
 * PRE_TEXT ::= "var bRet = false;" POST_TEXT ::= "return bRet;" BODY ::=
 * "{ bRet = true; }" JAVA_IDENTIFIER ::= ALPHA [STRING]* STRING ::= (ALPHA |
 * [0-9] | ...)* ALPHA ::= [a-zA-Z] NUMBER ::= [0-9]
 */
public abstract class IfParser extends AbstractTopDownParser
{
	public IfParser(Reader in)
	{
		super(in);
	}

	/**
	 * Imports a statement. STATEMENT ::= PRE_TEXT BLOCK ELSE* POST_TEXT ELSE
	 * ::= "else" BLOCK PRE_TEXT ::= "var bRet = false;" POST_TEXT ::=
	 * "return bRet;"
	 */
	public Statement importScript() throws InvalidScriptException
	{
		try
		{
			getChar();

			match("var");
			match("bRet");
			match('=');
			match("false");
			match(';');

			Statement s = new Statement();
			Block b = getBlock();
			s.addBlock(b);

			while( look == 'e' )
			{
				match("else");
				b = getBlock();
				s.addBlock(b);
			}

			match("return");
			match("bRet");
			match(";");

			return s;
		}
		catch( ParseException ex )
		{
			throw new InvalidScriptException("Error parsing script", ex);
		}
	}

	/**
	 * BLOCK ::= "if (" CLAUSE ")" BODY BODY ::= "{ bRet = true; }"
	 */
	protected Block getBlock() throws InvalidScriptException, ParseException
	{
		match("if");
		match('(');

		Block b = new Block();
		b.setClause(getClause());

		match(')');
		match('{');
		match("bRet");
		match('=');
		match("true");
		match(';');
		match('}');

		return b;
	}

	/**
	 * CLAUSE ::= TERM (OPERATOR TERM)* OPERATOR ::= "&&" | "||"
	 */
	protected Clause getClause() throws InvalidScriptException, ParseException
	{
		Clause c = new Clause();
		c.setFirst(getTerm());

		while( look == '&' || look == '|' )
		{
			OpTerm o;

			if( look == '&' )
			{
				match("&&");
				o = new OpTerm(new AndOperator(), getTerm());
			}
			else
			{
				match("||");
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
	 * COMPARISON ::= ???
	 */
	protected abstract Comparison getComparison() throws InvalidScriptException, ParseException;

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
	 * EQUALITY ::= "!=" | "=="
	 */
	protected Equality getEquality() throws InvalidScriptException, ParseException
	{
		if( look == '!' )
		{
			match("!=");
			return new NotEquals();
		}
		else if( look == '=' )
		{
			match("==");
			return new Equals();
		}
		else if( look == '<' )
		{
			match('<');
			if( look == '=' )
			{
				match('=');
				return new LessThanOrEqualTo();
			}
			else
			{
				return new LessThan();
			}
		}
		else if( look == '>' )
		{
			match('>');
			if( look == '=' )
			{
				match('=');
				return new GreaterThanOrEqualTo();
			}
			else
			{
				return new GreaterThan();
			}
		}

		throw new InvalidScriptException("Could not read match an equality operator");
	}

	/**
	 * VALUE ::= "'" STRING "'"
	 */
	protected String getValue() throws InvalidScriptException, ParseException
	{
		StringBuilder s = new StringBuilder();

		if( look != '\'' )
		{
			throw new InvalidScriptException("Found '" + look + "' but expected a single quote");
		}
		getChar();

		while( look != '\'' )
		{
			if( look == '\\' )
			{
				getChar();
			}
			s.append(look);
			getChar();
		}

		match("'");

		return s.toString();
	}

	/**
	 * JAVA_IDENTIFIER ::= ALPHA [STRING]*
	 */
	protected String getJavaIdentifier() throws InvalidScriptException, ParseException
	{
		if( !Character.isLetter(look) )
		{
			throw new InvalidScriptException("Found '" + look + "' but expected a letter");
		}

		StringBuilder s = new StringBuilder();
		do
		{
			s.append(look);
			getChar();
		}
		while( Character.isLetterOrDigit(look) );

		return s.toString();
	}
}
