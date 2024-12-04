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

package com.dytech.gui;

import java.awt.Toolkit;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class JSmartTextField extends JTextField {
  public static final int I_ALPHABET = 1;
  public static final int I_NUMERIC = 2;
  public static final int I_WHITESPACE = 4;
  public static final int I_UNDERSCORE = 8;
  public static final int I_HYPHEN = 16;
  public static final int I_COLON = 32;
  public static final int I_SEMICOLON = 64;
  public static final int I_PERIOD = 128;
  public static final int I_COMMA = 256;
  public static final int I_ANYTHING = 512;
  public static final int I_FORWARD_SLASH = 1024;

  public static final int I_ALPHANUMERIC = I_ALPHABET | I_NUMERIC;
  public static final int I_SYMBOLS =
      I_UNDERSCORE | I_HYPHEN | I_COLON | I_SEMICOLON | I_PERIOD | I_COMMA;
  public static final int I_MOST_STUFF = I_ALPHANUMERIC | I_WHITESPACE | I_SYMBOLS;
  public static final int I_ALRIGHT_IN_URLS =
      I_ALPHANUMERIC | I_UNDERSCORE | I_PERIOD | I_FORWARD_SLASH;

  public static final int C_LOWER = 1;
  public static final int C_UPPER = 2;
  public static final int C_ANY_CASE = 3;

  private SmarterDocument document = new SmarterDocument();

  public JSmartTextField() {
    this(-1);
  }

  public JSmartTextField(int maxLength) {
    this(maxLength, I_ANYTHING, C_ANY_CASE);
  }

  public JSmartTextField(int maxLength, int inputType, int caseType) {
    super();

    setDocument(document);

    setMaxLength(maxLength);
    setInputType(inputType);
    setCaseType(caseType);
  }

  /**
   * @return Returns the caseType.
   */
  public int getCaseType() {
    return document.getCaseType();
  }

  /**
   * @param caseType The caseType to set.
   */
  public void setCaseType(int caseType) {
    document.setCaseType(caseType);
  }

  /**
   * @return Returns the inputType.
   */
  public int getInputType() {
    return document.getInputType();
  }

  /**
   * @param inputType The inputType to set.
   */
  public void setInputType(int inputType) {
    document.setInputType(inputType);
  }

  /**
   * @return Returns the maxLength.
   */
  public int getMaxLength() {
    return document.getMaxLength();
  }

  /**
   * @param maxLength The maxLength to set. Any length less than zero indicates an unlimited length.
   */
  public void setMaxLength(int maxLength) {
    document.setMaxLength(maxLength);
  }

  public boolean isStartWithAlphaCharacter() {
    return document.isStartWithAlphaCharacter();
  }

  public void setStartWithAlphaCharacter(boolean b) {
    document.setStartWithAlphaCharacter(b);
  }

  private static class SmarterDocument extends PlainDocument {
    private int maxLength;
    private int inputType;
    private int caseType;
    private boolean startWithAlphaCharacter;

    public SmarterDocument() {
      super();

      setMaxLength(-1);
      setInputType(I_ANYTHING);
      setCaseType(C_ANY_CASE);
    }

    @Override
    public void insertString(int offset, String s, AttributeSet attributeSet)
        throws BadLocationException {
      if (maxLength < 0 || getLength() + s.length() <= maxLength) {
        boolean allowInsert = true;
        for (int i = 0; i < s.length() && allowInsert; i++) {
          allowInsert = checkCharacter(s.charAt(i));
        }

        if (allowInsert && offset == 0 && s.length() > 0 && isStartWithAlphaCharacter()) {
          allowInsert = Character.isLetter(s.charAt(0));
        }

        if (allowInsert) {
          if (caseType == C_LOWER) {
            super.insertString(offset, s.toLowerCase(), attributeSet);
          } else if (caseType == C_UPPER) {
            super.insertString(offset, s.toUpperCase(), attributeSet);
          } else {
            super.insertString(offset, s, attributeSet);
          }
        } else {
          Toolkit.getDefaultToolkit().beep();
        }
      } else {
        Toolkit.getDefaultToolkit().beep();
      }
    }

    private boolean checkCharacter(char c) {
      if ((inputType & I_ANYTHING) > 0) {
        return true;
      } else if (Character.isLetter(c)) {
        return (inputType & I_ALPHABET) > 0;
      } else if (Character.isDigit(c)) {
        return (inputType & I_NUMERIC) > 0;
      } else if (Character.isWhitespace(c)) {
        return (inputType & I_WHITESPACE) > 0;
      } else if (c == '_') {
        return (inputType & I_UNDERSCORE) > 0;
      } else if (c == '-') {
        return (inputType & I_HYPHEN) > 0;
      } else if (c == '/') {
        return (inputType & I_FORWARD_SLASH) > 0;
      } else if (c == ':') {
        return (inputType & I_COLON) > 0;
      } else if (c == ';') {
        return (inputType & I_SEMICOLON) > 0;
      } else if (c == '.') {
        return (inputType & I_PERIOD) > 0;
      } else if (c == ',') {
        return (inputType & I_COMMA) > 0;
      } else {
        return false;
      }
    }

    public int getCaseType() {
      return caseType;
    }

    public void setCaseType(int caseType) {
      if (caseType != C_LOWER && caseType != C_UPPER && caseType != C_ANY_CASE) {
        throw new IllegalArgumentException("Case type must be specified by one of C_* variables");
      } else {
        this.caseType = caseType;
      }
    }

    public int getInputType() {
      return inputType;
    }

    public void setInputType(int inputType) {
      if (inputType < 1) {
        throw new IllegalArgumentException(
            "Input type must be specified by a logical 'or'ing of the I_* variables");
      } else {
        this.inputType = inputType;
      }
    }

    public int getMaxLength() {
      return maxLength;
    }

    public void setMaxLength(int maxLength) {
      this.maxLength = maxLength;
    }

    public void setStartWithAlphaCharacter(boolean startWithAlphaCharacter) {
      this.startWithAlphaCharacter = startWithAlphaCharacter;
    }

    public boolean isStartWithAlphaCharacter() {
      return startWithAlphaCharacter;
    }
  }
}
