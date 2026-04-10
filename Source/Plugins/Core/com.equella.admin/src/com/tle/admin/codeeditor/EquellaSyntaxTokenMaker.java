/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.text.Segment;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenTypes;
import org.fife.ui.rsyntaxtextarea.modes.JavaScriptTokenMaker;

/**
 * A custom token maker for openEQUELLA scripts. This class extends the JavaScriptTokenMaker to
 * highlight openEQUELLA-specific variables as preprocessor directives.
 *
 * <p>Although JavaScriptTokenMaker is a JFlex-based token maker, we can use the code here to extend
 * the keywords to be highlighted. This is because the JavaScriptTokenMaker uses a JavaScriptParser,
 * which is a subclass of the Parser class. The Parser class has a method called getTokenList that
 * returns a list of tokens. We can override this method to add our own keywords.
 *
 * <p>Performance wise this is not as good as a JFlex-based token maker, but it is much easier to
 * implement. Testing has shown though that it is still fast enough for our needs.
 */
public class EquellaSyntaxTokenMaker extends JavaScriptTokenMaker {
  /** Usually seen in source files as String DEFAULT_VARIABLE */
  private static final String[] EQUELLA_VARS =
      new String[] {
        "attachments",
        "attributes",
        "catalogue",
        "ctrl",
        "currentItem",
        "data",
        "drm",
        "file",
        "images",
        "items",
        "lang",
        "logger",
        "meta",
        "metadata",
        "mime",
        "nav",
        "page",
        "request",
        "staging",
        "status",
        "system",
        "tier",
        "user",
        "utils",
        "xml",
        "workflowstep"
      };

  private final Set<String> keywords;

  public EquellaSyntaxTokenMaker(String... extendedKeywords) {
    keywords = new HashSet<>(Arrays.asList(EQUELLA_VARS));

    // Add any extended keywords
    if (extendedKeywords != null && extendedKeywords.length > 0) {
      keywords.addAll(Arrays.asList(extendedKeywords));
    }
  }

  @Override
  public Token getTokenList(Segment text, int initialTokenType, int startOffset) {
    Token tokens = super.getTokenList(text, initialTokenType, startOffset);

    Token current = tokens;
    while (current != null) {
      flagKeywords(current);
      current = current.getNextToken();
    }

    return tokens;
  }

  private void flagKeywords(Token current) {
    if (current.getType() == TokenTypes.IDENTIFIER && keywords.contains(current.getLexeme())) {
      current.setType(TokenTypes.PREPROCESSOR);
    }
  }
}
