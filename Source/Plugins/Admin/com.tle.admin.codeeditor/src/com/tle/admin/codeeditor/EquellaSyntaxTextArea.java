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

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

import org.fife.ui.rsyntaxtextarea.AbstractTokenMakerFactory;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Style;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMaker;

/**
 * @author Aaron
 */
public class EquellaSyntaxTextArea extends RSyntaxTextArea
{
	@SuppressWarnings("nls")
	public static final String SYNTAX_EQUELLA = "text/equellascript";

	public EquellaSyntaxTextArea(String syntax, int rows, int columns, String... extendedKeywords)
	{
		super(rows, columns);
		if( syntax.equals(SYNTAX_EQUELLA) )
		{
			((RSyntaxDocument) getDocument()).setTokenMakerFactory(new EquellaTokenMakerFactory(extendedKeywords));
		}
		setSyntaxEditingStyle(syntax);

		SyntaxScheme ss = getSyntaxScheme();
		Style style = ss.styles[Token.PREPROCESSOR];
		style.foreground = new Color(20, 80, 20);
		style.font = getFont().deriveFont(Font.BOLD);
	}

	public EquellaSyntaxTextArea(int rows, int columns, String... extendedKeywords)
	{
		this(SYNTAX_EQUELLA, rows, columns, extendedKeywords);
	}

	@Override
	public void setText(String t)
	{
		super.setText(t);
		discardAllEdits();
	}

	public static class EquellaTokenMakerFactory extends AbstractTokenMakerFactory
	{
		private final String[] extendedKeywords;

		protected EquellaTokenMakerFactory(String... extendedKeywords)
		{
			this.extendedKeywords = extendedKeywords;
		}

		@Override
		protected Map createTokenMakerKeyToClassNameMap()
		{
			Map highlighters = new HashMap();
			highlighters.put("text/equellascript", EquellaSyntaxTokenMaker.class.getName());
			return highlighters;
		}

		@Override
		protected TokenMaker getTokenMakerImpl(String key)
		{
			if( key != null && key.equals(SYNTAX_EQUELLA) )
			{
				return new EquellaSyntaxTokenMaker(extendedKeywords);
			}
			return super.getTokenMakerImpl(key);
		}
	}
}
