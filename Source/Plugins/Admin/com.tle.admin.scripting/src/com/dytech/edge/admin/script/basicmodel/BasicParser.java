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

import java.io.Reader;
import java.text.ParseException;

import com.dytech.edge.admin.script.InvalidScriptException;
import com.dytech.edge.admin.script.ifmodel.Comparison;
import com.dytech.edge.admin.script.ifmodel.Equality;
import com.dytech.edge.admin.script.ifmodel.Equals;
import com.dytech.edge.admin.script.ifmodel.IfParser;
import com.dytech.edge.admin.script.options.ScriptOptions;

/**
 * COMPARISON ::= XPATH_COMPARISON | CONTAINS_COMPARISON | STATUS_COMPARISON |
 * TYPE_COMPARISON XPATH_COMPARISON ::= "xml.get('" XPATH "')" EQUALITY "'"
 * VALUE "'" CONTAINS_COMPARISON ::= "xml.contains('" XPATH "', '" VALUE "')"
 * STATUS_COMPARISON ::= "status" EQUALITY "'" VALUE "'" TYPE_COMPARISON ::=
 * "user." METHOD "('" VALUE "')"
 */
public class BasicParser extends IfParser
{
	private final ScriptOptions options;

	public BasicParser(ScriptOptions options, Reader in)
	{
		super(in);
		this.options = options;
	}

	/**
	 * COMPARISON ::= XPATH_COMPARISON | CONTAINS_COMPARISON |
	 * MODERATION_COMPARISON | STATUS_COMPARISON | TYPE_COMPARISON
	 */
	@Override
	protected Comparison getComparison() throws InvalidScriptException, ParseException
	{
		if( look == 'x' )
		{
			match("xml.");
			if( look == 'g' )
			{
				return getXpathComparison();
			}
			else if( look == 'c' )
			{
				return getContainsComparison();
			}
			else
			{
				throw new InvalidScriptException("Invalid use of XML object");
			}
		}
		else if( look == 's' )
		{
			return getStatusComparison();
		}
		else if( look == 'u' )
		{
			return getTypeComparison();
		}
		if( look == 'm' )
		{
			return getModerationComparison();
		}
		else if( look == 'w' )
		{
			return getWorkflowStepComparison();
		}
		else
		{
			throw new InvalidScriptException("Invalid use of XML object");
		}
	}

	/**
	 * XPATH_COMPARISON ::= "xml.get('" XPATH "')" EQUALITY "'" VALUE "'" There
	 * is now the special case, that if an "equals" is read in, it actually
	 * creates a "comparison" instead. This should allow scripts to change to
	 * using contains without the need to fix everything in the database.
	 */
	protected Comparison getXpathComparison() throws InvalidScriptException, ParseException
	{
		match("get(");
		match("'");
		String xpath = getXpath();
		match("'");
		match(')');
		Equality op = getEquality();
		String value = getValue();

		if( op instanceof Equals )
		{
			return new ContainsComparison(xpath, value);
		}
		else
		{
			return new XpathComparison(op, xpath, value);
		}
	}

	/**
	 * CONTAINS_COMPARISON ::= "xml.contains('" XPATH "', '" VALUE "')"
	 */
	protected ContainsComparison getContainsComparison() throws InvalidScriptException, ParseException
	{
		match("contains(");
		match("'");
		String xpath = getXpath();
		match("'");
		match(',');
		String value = getValue();
		match(')');
		return new ContainsComparison(xpath, value);
	}

	/**
	 * STATUS_COMPARISON ::= "status" EQUALITY "'" VALUE "'"
	 */
	protected StatusComparison getStatusComparison() throws InvalidScriptException, ParseException
	{
		match("status");
		Equality op = getEquality();
		String value = getValue();
		return new StatusComparison(op, value);
	}

	/**
	 * TYPE_COMPARISON ::= "type." METHOD "('" VALUE "')"
	 */
	protected TypeComparison getTypeComparison() throws InvalidScriptException, ParseException
	{
		match("user");
		match(".");
		UserMethodMethod op = getUserMethod();
		match("(");
		String value = getValue();
		match(")");
		return new TypeComparison(op, value);
	}

	/**
	 * MODERATION_COMPARISON ::= "moderationallowed" EQUALITY VALUE
	 */
	protected ModerationComparison getModerationComparison() throws InvalidScriptException, ParseException
	{
		match("moderationallowed");
		Equality op = getEquality();
		boolean value = getBooleanValue();
		return new ModerationComparison(op, value);
	}

	/**
	 * WORKFLOWSTEP_COMPARISON ::= "workflowstep" EQUALITY "'" VALUE "'"
	 */
	protected WorkflowStepComparison getWorkflowStepComparison() throws InvalidScriptException, ParseException
	{
		match("workflowstep");
		Equality op = getEquality();
		String value = getValue();
		String name = options.getWorkflowStepName(value);
		return new WorkflowStepComparison(op, value, name);
	}

	protected UserMethodMethod getUserMethod() throws ParseException
	{
		UserMethodMethod m = null;
		if( look == 'h' )
		{
			m = new UserMethodMethod.HasRole();
		}
		else if( look == 'd' )
		{
			m = new UserMethodMethod.DoesntHasRole();
		}
		else
		{
			throw new ParseException("Unknown user method starting with '" + look + "'", getCurrentOffset());
		}
		match(m.toScript());
		return m;
	}

	/**
	 * XPATH ::= (ALPHA | "/" | "." | ":" )+
	 */
	protected String getXpath() throws InvalidScriptException, ParseException
	{
		StringBuilder s = new StringBuilder();
		while( Character.isLetter(look) || Character.isDigit(look) || look == '/' || look == '.' || look == ':'
			|| look == '_' || look == '@' )
		{
			s.append(look);
			getChar();
		}

		if( s.length() == 0 )
		{
			throw new InvalidScriptException("Not a valid Xpath");
		}

		return s.toString();
	}
}
