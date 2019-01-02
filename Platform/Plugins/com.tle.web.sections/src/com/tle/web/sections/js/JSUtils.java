/*
 * Copyright 2019 Apereo
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

package com.tle.web.sections.js;

import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.convert.Conversion;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.generic.expression.FunctionCallExpression;
import com.tle.web.sections.js.generic.expression.ScriptVariable;

public final class JSUtils
{
	private JSUtils()
	{
		throw new Error();
	}

	private static void addHexValue(StringBuilder sbuf, int num)
	{
		String hex = Integer.toHexString(num);
		for( int len = hex.length(); len < 4; len++ )
		{
			sbuf.append('0');
		}
		sbuf.append(hex);
	}

	public static boolean needsEscape(String str)
	{
		if( str != null )
		{
			final int count = str.length();
			for( int i = 0; i < count; i++ )
			{
				char ch = str.charAt(i);
				if( !Character.isJavaIdentifierPart(ch) )
				{
					return true;
				}
			}
		}
		return false;

	}

	public static String toJSString(String str)
	{
		return escape(str, true);
	}

	@SuppressWarnings("nls")
	public static String escape(String str, boolean single)
	{
		StringBuilder szOut = new StringBuilder();

		if( single )
		{
			szOut.append('\'');
		}
		if( str != null )
		{
			final int count = str.length();
			for( int i = 0; i < count; i++ )
			{
				char ch = str.charAt(i);
				if( ch < 0x20 )
				{
					szOut.append("\\u");
					addHexValue(szOut, ch);
				}
				else
				{
					switch( ch )
					{
						case '\\':
							szOut.append("\\\\");
							break;
						case '"':
							if( single )
							{
								szOut.append(ch);
								break;
							}
						case '\'':
						case '<': // Don't let people open new HTML tags
							szOut.append("\\u");
							addHexValue(szOut, ch);
							break;
						default:
							szOut.append(ch);
					}
				}
			}
		}

		if( single )
		{
			szOut.append('\'');
		}

		return szOut.toString();
	}

	public static String createFunctionCall(RenderContext info, String callExpr, JSExpression[] params)
	{
		StringBuilder sbuf = new StringBuilder();
		sbuf.append(callExpr);
		sbuf.append('(');
		boolean first = true;
		if( params != null )
		{
			for( JSExpression expr : params )
			{
				if( !first )
				{
					sbuf.append(',');
				}
				first = false;
				sbuf.append(expr.getExpression(info));
			}
		}
		sbuf.append(")"); //$NON-NLS-1$
		return sbuf.toString();
	}

	public static FunctionCallExpression getElement(JSCallable elementFunction, String id)
	{
		return new FunctionCallExpression(elementFunction, id);
	}

	public static JSExpression convertExpression(Object obj)
	{
		return Conversion.inst().convertToJSExpression(obj);
	}

	public static JSExpression[] convertExpressions(Object... objExprs)
	{
		JSExpression[] exprs = new JSExpression[objExprs.length];
		for( int i = 0; i < objExprs.length; i++ )
		{
			exprs[i] = convertExpression(objExprs[i]);
		}
		return exprs;
	}

	public static ScriptVariable[] createParameters(int numParams)
	{
		ScriptVariable[] params = new ScriptVariable[numParams];
		for( int i = 0; i < numParams; i++ )
		{
			params[i] = new ScriptVariable("p" + (i + 1)); //$NON-NLS-1$
		}
		return params;
	}

	public static int cssToPixels(String css)
	{
		if( css.endsWith("px") ) //$NON-NLS-1$
		{
			css = css.substring(0, css.length() - 2);
		}
		if( css.indexOf('%') != -1 )
		{
			throw new SectionsRuntimeException("Can't use percentages here"); //$NON-NLS-1$
		}
		return Integer.parseInt(css);
	}
}
