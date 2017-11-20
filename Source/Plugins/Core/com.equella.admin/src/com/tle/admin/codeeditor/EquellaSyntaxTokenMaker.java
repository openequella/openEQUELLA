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

package com.tle.admin.codeeditor;

import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMap;
import org.fife.ui.rsyntaxtextarea.modes.JavaScriptTokenMaker;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
public class EquellaSyntaxTokenMaker extends JavaScriptTokenMaker
{
	/**
	 * Usually seen in source files as String DEFAULT_VARIABLE
	 */
	private static final String[] EQUELLA_VARS = new String[]{"attachments", "attributes", "catalogue", "ctrl",
			"currentItem", "data", "drm", "file", "images", "items", "lang", "logger", "meta", "metadata", "mime",
			"nav", "page", "request", "staging", "status", "system", "tier", "user", "utils", "xml", "workflowstep"};

	private final String[] extendedKeywords;

	public EquellaSyntaxTokenMaker(String... extendedKeywords)
	{
		this.extendedKeywords = extendedKeywords;
	}

	@Override
	public TokenMap getWordsToHighlight()
	{
		TokenMap tokenMap = new TokenMap(52 + EQUELLA_VARS.length
			+ (extendedKeywords == null ? 0 : extendedKeywords.length));

		int reservedWord = Token.RESERVED_WORD;
		tokenMap.put("abstract", reservedWord);
		tokenMap.put("as", reservedWord);
		tokenMap.put("break", reservedWord);
		tokenMap.put("case", reservedWord);
		tokenMap.put("catch", reservedWord);
		tokenMap.put("class", reservedWord);
		tokenMap.put("const", reservedWord);
		tokenMap.put("continue", reservedWord);
		tokenMap.put("debugger", reservedWord);
		tokenMap.put("default", reservedWord);
		tokenMap.put("delete", reservedWord);
		tokenMap.put("do", reservedWord);
		tokenMap.put("else", reservedWord);
		tokenMap.put("enum", reservedWord);
		tokenMap.put("export", reservedWord);
		tokenMap.put("extends", reservedWord);
		tokenMap.put("final", reservedWord);
		tokenMap.put("finally", reservedWord);
		tokenMap.put("for", reservedWord);
		tokenMap.put("function", reservedWord);
		tokenMap.put("goto", reservedWord);
		tokenMap.put("if", reservedWord);
		tokenMap.put("implements", reservedWord);
		tokenMap.put("import", reservedWord);
		tokenMap.put("in", reservedWord);
		tokenMap.put("instanceof", reservedWord);
		tokenMap.put("interface", reservedWord);
		tokenMap.put("item", reservedWord);
		tokenMap.put("namespace", reservedWord);
		tokenMap.put("native", reservedWord);
		tokenMap.put("new", reservedWord);
		tokenMap.put("null", reservedWord);
		tokenMap.put("package", reservedWord);
		tokenMap.put("private", reservedWord);
		tokenMap.put("protected", reservedWord);
		tokenMap.put("public", reservedWord);
		tokenMap.put("return", reservedWord);
		tokenMap.put("static", reservedWord);
		tokenMap.put("super", reservedWord);
		tokenMap.put("switch", reservedWord);
		tokenMap.put("synchronized", reservedWord);
		tokenMap.put("this", reservedWord);
		tokenMap.put("throw", reservedWord);
		tokenMap.put("throws", reservedWord);
		tokenMap.put("transient", reservedWord);
		tokenMap.put("try", reservedWord);
		tokenMap.put("typeof", reservedWord);
		tokenMap.put("var", reservedWord);
		tokenMap.put("void", reservedWord);
		tokenMap.put("while", reservedWord);
		tokenMap.put("with", reservedWord);

		int literalBoolean = Token.LITERAL_BOOLEAN;
		tokenMap.put("false", literalBoolean);
		tokenMap.put("true", literalBoolean);

		int dataType = Token.DATA_TYPE;
		tokenMap.put("boolean", dataType);
		tokenMap.put("byte", dataType);
		tokenMap.put("char", dataType);
		tokenMap.put("double", dataType);
		tokenMap.put("float", dataType);
		tokenMap.put("int", dataType);
		tokenMap.put("long", dataType);
		tokenMap.put("short", dataType);

		// int eq = Token.PREPROCESSOR;
		for( String var : EQUELLA_VARS )
		{
			tokenMap.put(var, Token.PREPROCESSOR);
		}
		if( extendedKeywords != null )
		{
			for( String var : extendedKeywords )
			{
				tokenMap.put(var, Token.PREPROCESSOR);
			}
		}

		return tokenMap;
	}
}
