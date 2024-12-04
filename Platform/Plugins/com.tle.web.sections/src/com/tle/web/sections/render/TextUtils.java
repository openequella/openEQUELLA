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

package com.tle.web.sections.render;

import com.tle.common.Check;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class TextUtils {
  private static final String DEFAULT_STYLE_CLASS = "highlight"; // $NON-NLS-1$
  private static final String ELLIPSIS_ENTITY = "&hellip;"; // $NON-NLS-1$
  public static final int DESCRIPTION_LENGTH = 1024;
  public static final int TITLE_LENGTH = 255;
  public static final int WORDS_SPACE_LENGTH = 50;

  public static final TextUtils INSTANCE = new TextUtils();

  private TextUtils() {
    // Nothing to do here
  }

  public String wrap(String text, int maxBodyLength) {
    return wrap(text, maxBodyLength, -1);
  }

  @SuppressWarnings("nls")
  public String wrap(String text, int maxBodyLength, int maxWordLength) {
    try {
      return ensureWrap(text, maxBodyLength, maxWordLength, true);
    } catch (Exception ex) {
      return "<span style=\"color: red\">Error wrapping the following text - please notify"
                 + " administrator</span>"
          + text;
    }
  }

  public String highlight(String text, Collection<String> terms) {
    return highlightWords(text, DEFAULT_STYLE_CLASS, terms);
  }

  public String highlight(String text, String styleClass, Collection<String> terms) {
    return highlightWords(text, styleClass, terms);
  }

  public String mostOccurences(String text, int maxLength, Collection<String> terms) {
    if (text.length() > maxLength) {
      return returnMatchingFraction(text, terms, maxLength);
    }

    return text;
  }

  /**
   * Tokenize a text by regex and put each split text into an array. And return a Stream whose
   * source is the array.
   *
   * @param text Text to be processed.
   * @param regex Regex used to tokenize the text.
   * @return A Stream containing an array created by tokenizing the provided text.
   */
  public static Stream<String> tokenizeString(String text, String regex) {
    return Arrays.stream(text.split(regex)).filter(s -> !Check.isEmpty(s));
  }

  private String returnMatchingFraction(String text, Collection<String> terms, int maxLength) {
    ArrayList<Integer> positions = new ArrayList<Integer>();

    String wordsOR = queryWords(terms);
    if (wordsOR.length() == 0) {
      return text.substring(0, maxLength) + (char) 0x2026;
    }

    Pattern p = Pattern.compile(wordsOR, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    Matcher m = p.matcher(text);
    while (m.find()) {
      // record all start and end positions for all found terms
      positions.add(m.start());
      positions.add(m.end());
    }

    StringBuilder sub = new StringBuilder();
    for (int index = 0; index < positions.size(); index++) {
      int curPosition = positions.get(index);

      // concatenate the beginning of the text
      if (index == 1) {
        int prePosition = positions.get(index - 1);
        // first found term is too far from beginning then add '...' in
        // the beginning
        if (prePosition > 2 * WORDS_SPACE_LENGTH) {
          sub.append((char) 0x2026);
          sub.append(text, curPosition - WORDS_SPACE_LENGTH, curPosition);
        } else {
          sub.append(text, 0, curPosition);
        }

        if (positions.size() == 2) {
          sub.append(text.substring(curPosition));
        }
      }

      // found terms' start positions
      if (index % 2 == 0 && index >= 2) {
        int prePosition = positions.get(index - 1);
        // two terms are too far from each other then add'...' in
        // between
        if (curPosition - prePosition > 2 * WORDS_SPACE_LENGTH) {
          sub.append(text, prePosition, prePosition + WORDS_SPACE_LENGTH);
          sub.append((char) 0x2026);
          sub.append(text, curPosition - WORDS_SPACE_LENGTH, curPosition);
        }
        // two terms are close to each other
        else {
          sub.append(text, prePosition, curPosition);
        }
      }
      // found terms' end positions
      if (index % 2 != 0 && index >= 3) {
        int prePosition = positions.get(index - 1);
        // handle the ending
        if (index == positions.size() - 1) {
          sub.append(text.substring(prePosition));
        }
        // concatenate search terms
        else {
          sub.append(text, prePosition, curPosition);
        }
      }
    }

    String fraction = sub.toString();
    if (!Check.isEmpty(fraction)) {
      if (fraction.length() > maxLength) {
        return fraction.substring(0, maxLength) + (char) 0x2026;
      }
      return fraction;
    }

    return text.substring(0, maxLength) + (char) 0x2026;
  }

  private String queryWords(Collection<String> terms) {
    StringBuilder b = new StringBuilder();
    for (String term : terms) {
      String newTerm = term.trim();
      if (newTerm.length() > 0) {
        // Clean up the terms so they don't contain any special regex
        // characters
        newTerm = removeNonWordCharacters(newTerm);
        newTerm = newTerm.trim();
        if (!Check.isEmpty(newTerm)) {
          if (b.length() > 0) {
            b.append('|');
          }
          b.append(newTerm);
        }
      }
    }
    return b.toString();
  }

  private String highlightWords(String input, String styleClass, Collection<String> terms) {
    String wordsOR = queryWords(terms);
    if (wordsOR.length() == 0) {
      return input;
    }

    try {
      Pattern p =
          Pattern.compile(
              "^(.*?)\\b(" + wordsOR + ")\\b(.*)$",
              Pattern.CASE_INSENSITIVE // $NON-NLS-1$ //$NON-NLS-2$
                  | Pattern.DOTALL);
      StringBuilder result = new StringBuilder();
      highlightWords(result, input, p, styleClass);
      return result.toString();

    } catch (Exception e) {
      return input;
    }
  }

  private void highlightWords(StringBuilder result, String input, Pattern p, String styleClass) {
    Matcher m = p.matcher(input);

    if (m.matches()) {
      result.append(m.group(1));
      result.append("<span class=\""); // $NON-NLS-1$
      result.append(styleClass);
      result.append("\">"); // $NON-NLS-1$
      result.append(m.group(2));
      result.append("</span>"); // $NON-NLS-1$
      highlightWords(result, m.group(3), p, styleClass);
    } else {
      result.append(input);
    }
  }

  @SuppressWarnings("nls")
  private String removeNonWordCharacters(String query) {
    return query
        .replaceAll("\\\\", "")
        .replaceAll("\\.", "")
        .replaceAll("\\(", "")
        .replaceAll("\\)", "")
        .replaceAll("\\[", "")
        .replaceAll("\\]", "")
        .replaceAll("\\+", "")
        .replaceAll("\\?", ".?")
        .replaceAll("\\*", "\\\\w*")
        .replaceAll("\\|", "");
  }

  @SuppressWarnings("nls")
  public String ensureWrap(String in, int maxBodyLength, int maxWordLength, boolean html) {
    if (maxBodyLength < 0) {
      maxBodyLength = Integer.MAX_VALUE;
    }

    if (maxWordLength < 0) {
      maxWordLength = maxBodyLength;
    }

    final StringBuilder sbuf = new StringBuilder();
    final Stack<String> tags = new Stack<String>();

    int wordlen = 0;
    int strlen = 0;
    int start = 0;
    while (start < in.length()) {
      final int end = nextChar(in, start, html);
      final String ch = in.substring(start, end);

      boolean wordend = false;
      if (!html || ch.charAt(0) != '<') {
        if (ch.length() == 1 && Character.isSpaceChar(ch.charAt(0))) {
          wordend = true;
        } else {
          wordlen++;
        }
        strlen++;
      } else {
        if (ch.equalsIgnoreCase("<br>") || ch.equalsIgnoreCase("<hr>")) {
          wordend = true;
          strlen++;
        } else {
          if (ch.charAt(1) == '/') {
            // These tags aren't meant to be closed, but it would
            // blow up if they are
            if (!(ch.equalsIgnoreCase("</br>") || ch.equalsIgnoreCase("</hr>"))) {
              tags.pop();
            }
          } else {
            tags.push(ch);
          }
        }
      }

      sbuf.append(ch);

      if (!wordend && wordlen >= maxWordLength) {
        if (strlen < maxBodyLength) {
          sbuf.append(' ');
        }
        wordend = true;
      }

      if (strlen >= maxBodyLength) {
        popTags(tags, sbuf);
        if (html) {
          sbuf.append(ELLIPSIS_ENTITY);
        } else {
          sbuf.append((char) 0x2026);
        }
        break;
      }

      if (wordend) {
        wordlen = 0;
      }

      start = end;
    }
    return sbuf.toString();
  }

  private void popTags(Stack<String> tags, StringBuilder sbuf) {
    while (!tags.isEmpty()) {
      sbuf.append("</"); // $NON-NLS-1$
      sbuf.append(tags.pop().substring(1));
    }
  }

  private int nextChar(String in, int offs, boolean html) {
    if (!html) {
      return offs + 1;
    } else {
      boolean inTag = false;
      boolean inEnt = false;
      while (offs < in.length()) {
        char ch = in.charAt(offs);
        offs++;

        if (!inTag && ch == '<') {
          inTag = true;
        } else if (!inEnt && ch == '&') {
          inEnt = true;
        } else if (inTag && ch == '>') {
          inTag = false;
        } else if (inEnt && ch == ';') {
          inEnt = false;
        }
        if (!inTag && !inEnt) {
          return offs;
        }
      }
      return offs;
    }
  }
}
