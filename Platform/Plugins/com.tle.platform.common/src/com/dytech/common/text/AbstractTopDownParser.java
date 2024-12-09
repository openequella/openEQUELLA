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

package com.dytech.common.text;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.text.ParseException;

/**
 * Implements the basics for a simple, fast, BNF-based, top-down parser. Examples of usage can be be
 * found in The Learning Edge where the parsers are used for basic XOQL and Javascript parsing.
 *
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public abstract class AbstractTopDownParser {
  private static final int PUSHBACK_BUFFER_SIZE = 32;

  private PushbackReader in;
  private int offset = 0;

  protected char look;

  /** Constructs a new AbstractTopDownParser. */
  public AbstractTopDownParser(Reader in) {
    this.in = new PushbackReader(in, PUSHBACK_BUFFER_SIZE);
  }

  // // COMMON HELPER METHODS ///////////////////////////////////////////////

  /** Retrieves an integer value from the stream. */
  protected int getIntegerValue() throws ParseException {
    if (!Character.isDigit(look)) {
      throw new ParseException("Digit value expected", getCurrentOffset());
    }

    // Read all the digits available
    StringBuilder result = new StringBuilder();
    do {
      result.append(look);
      getChar();
    } while (Character.isDigit(look));

    // If the digit is not followed by white space, it's screwed
    if (!Character.isWhitespace(look)) {
      throw new ParseException("Integer is followed by non-digits", getCurrentOffset());
    }

    // Because we didn't match anything, we have to skip whitespace.
    skipWhiteSpace();

    return Integer.parseInt(result.toString());
  }

  /** Retrieves a boolean value from the stream. */
  protected boolean getBooleanValue() throws ParseException {
    if (isLookAhead('t')) {
      match("true"); // $NON-NLS-1$
      return true;
    } else if (isLookAhead('f')) {
      match("false"); // $NON-NLS-1$
      return false;
    } else {
      throw new ParseException("Boolean value could not be read", getCurrentOffset());
    }
  }

  // // PARSER FUNCTIONS ////////////////////////////////////////////////////

  /** Matches the stream in the future, but maintains the existing parser state. */
  protected boolean matchTheFuture(String s) throws ParseException {
    // Save our current state
    char originalLook = look;
    StringBuilder fromStream = new StringBuilder();

    // Read the length of the string to match, or until the end of the
    // stream.
    final int count = s.length() - 1;
    for (int i = 0; i < count && look != -1; i++) {
      getChar();
      if (look != -1) {
        fromStream.append(look);
      }
    }

    boolean match = s.equalsIgnoreCase(originalLook + fromStream.toString());

    // Reset state
    look = originalLook;
    for (int i = fromStream.length() - 1; i >= 0; i--) {
      try {
        in.unread(fromStream.charAt(i));
      } catch (IOException ex) {
        ex.printStackTrace();
        throw new ParseException("Parser died while trying to see the future!", getCurrentOffset());
      }
    }

    return match;
  }

  /** Matches the given stream, moving us to the next token. */
  protected void match(String s) throws ParseException {
    int count = s.length();
    for (int i = 0; i < count; i++) {
      try {
        match(s.charAt(i));
      } catch (ParseException e) {
        throw new ParseException("Match Failed: Expected '" + s + "'", getCurrentOffset());
      }
    }
  }

  /** Matches the given character against the look-ahead, and moves to the next token. */
  protected void match(char c) throws ParseException {
    if (isLookAhead(c)) {
      skipWhiteSpace();
    } else {
      throw new ParseException("Expected '" + c + "' but found '" + look + "'", getCurrentOffset());
    }
  }

  /** Case insensitive check against the look-ahead character. */
  protected boolean isLookAhead(char c) {
    return Character.toLowerCase(look) == Character.toLowerCase(c);
  }

  /** The current single character look-ahead. */
  protected char look() {
    return look;
  }

  protected int getCurrentOffset() {
    return offset;
  }

  /**
   * @return true if at the end of the stream.
   */
  protected boolean isEOF() {
    return look == (char) -1;
  }

  /** Skips any whitespace. */
  protected void skipWhiteSpace() throws ParseException {
    skipWhiteSpace(true);
  }

  /** Skips any whitespace. */
  protected void skipWhiteSpace(boolean skipCurrentRegardless) throws ParseException {
    if (skipCurrentRegardless) {
      getChar();
    }

    while (Character.isWhitespace(look)) {
      getChar();
    }
  }

  /** Moves on to the next character in the stream. */
  protected void getChar() throws ParseException {
    try {
      look = (char) in.read();
      if (!isEOF()) {
        offset++;
      }
    } catch (IOException e) {
      throw new ParseException("Could not read stream", getCurrentOffset());
    }
  }
}
