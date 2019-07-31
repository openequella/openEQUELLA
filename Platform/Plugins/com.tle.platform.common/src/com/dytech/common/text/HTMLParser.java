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

import java.io.Reader;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This is an example of using the top-down parser as a streaming HTML parser to find tags and
 * attribute values.
 *
 * @author Nicholas Read
 */
public class HTMLParser extends AbstractTopDownParser {
  private static final String ALLOWED_NAME_SYMBOLS = ":-"; // $NON-NLS-1$
  private static final String ALLOWED_NAME_END_SYMBOLS = "/>="; // $NON-NLS-1$

  private Tag currentTag = null;

  public HTMLParser(Reader in) throws ParseException {
    super(in);

    getChar();
  }

  // // PUBLIC API //////////////////////////////////////////////////////////

  public Tag getNextTagForName(String tagName) throws ParseException {
    currentTag = getNextTag();
    while (currentTag != null) {
      if (currentTag.getName().equalsIgnoreCase(tagName)) {
        return currentTag;
      }
      currentTag = getNextTag();
    }
    return null;
  }

  public Tag getNextTagWithAttribute(String attributeName) throws ParseException {
    currentTag = getNextTag();
    while (currentTag != null) {
      if (currentTag.getAttribute(attributeName) != null) {
        return currentTag;
      }
      currentTag = getNextTag();
    }
    return null;
  }

  public Tag getNextTag() throws ParseException {
    currentTag = searchForNextTag();
    return currentTag;
  }

  // // IMPLEMENTATION //////////////////////////////////////////////////////

  /** Searches for the next opening tag. */
  private Tag searchForNextTag() throws ParseException {
    boolean finished = false;
    while (!finished) {
      while (!isLookAhead('<')) {
        getChar();

        // Check if we are at the end of the stream
        if (isEOF()) {
          return null;
        }
      }

      match('<');

      // Check if it's an ending tag or directive
      if (!isLookAhead('/') && !isLookAhead('?')) {
        finished = true;
      }
    }

    return matchTag();
  }

  /** Matches the current tag and returns a tag object with all of the particulars. */
  private Tag matchTag() throws ParseException {
    String tagName = getName();
    Map<String, String> attributes = new HashMap<String, String>();
    boolean selfClosing = false;

    // Retrieve each of the attributes
    while (!isLookAhead('/') && !isLookAhead('>')) {
      String attrName = getName();

      String attrValue;
      if (isLookAhead('=')) {
        match('=');
        attrValue = getAttributeValue();
      } else {
        attrValue = Boolean.toString(true);
      }

      attributes.put(attrName, attrValue);
    }

    // Check if a self closing tag
    if (isLookAhead('/')) {
      match('/');
      selfClosing = true;
    }

    // Match the end of the tag
    match('>');

    return new Tag(tagName, attributes, selfClosing);
  }

  /** Retrieves a tag or attribute name, which may include namespaces. */
  private String getName() throws ParseException {
    StringBuilder result = new StringBuilder();

    while (Character.isLetterOrDigit(look()) || ALLOWED_NAME_SYMBOLS.indexOf(look()) >= 0) {
      result.append(look());
      getChar();
    }

    if (ALLOWED_NAME_END_SYMBOLS.indexOf(look()) < 0 && !Character.isWhitespace(look())) {
      throw new ParseException(
          "Tag or attribute name is not valid.  Found '" + look() + "': " + getCurrentOffset(),
          getCurrentOffset());
    }

    skipWhiteSpace(false);

    return result.toString();
  }

  /**
   * Attribute values can vary quite a bit from the official standard. For example,
   *
   * <ul>
   *   <li>name = "value"
   *   <li>name = 'value'
   *   <li>name = value
   * </ul>
   */
  private String getAttributeValue() throws ParseException {
    StringBuilder result = new StringBuilder();

    char stopChar = 0;
    if (isLookAhead('"') || isLookAhead('\'')) {
      stopChar = look();
      getChar();
    }

    while ((stopChar != 0)
        ? !isLookAhead(stopChar)
        : !Character.isWhitespace(look()) && !isLookAhead('>') && !isLookAhead('/')) {
      result.append(look());
      getChar();
    }

    if (stopChar == 0) {
      skipWhiteSpace(false);
    } else {
      match(stopChar);
    }

    return result.toString();
  }

  public static class Tag {
    private final String name;
    private final Map<String, String> attributes;
    private final boolean selfClosing;

    public Tag(String name, Map<String, String> attributes, boolean selfClosing) {
      this.name = name;
      this.attributes = attributes;
      this.selfClosing = selfClosing;
    }

    public String getName() {
      return name;
    }

    public Set<String> getAttribues() {
      return Collections.unmodifiableSet(attributes.keySet());
    }

    public String getAttribute(String name) {
      return attributes.get(name);
    }

    public boolean isSelfClosing() {
      return selfClosing;
    }
  }
}
