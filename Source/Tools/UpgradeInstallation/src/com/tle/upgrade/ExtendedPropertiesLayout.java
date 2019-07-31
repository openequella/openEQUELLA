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

package com.tle.upgrade;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration.PropertiesWriter;
import org.apache.commons.configuration.PropertiesConfigurationLayout;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

public class ExtendedPropertiesLayout extends PropertiesConfigurationLayout {
  /** The list of possible key/value separators */
  private static final char[] SEPARATORS = new char[] {'=', ':'};

  /** The white space characters used as key/value separators. */
  private static final char[] WHITE_SPACE = new char[] {' ', '\t', '\f'};

  public ExtendedPropertiesLayout(PropertiesConfiguration config) {
    super(config);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void save(Writer out) throws ConfigurationException {
    try {
      char delimiter =
          getConfiguration().isDelimiterParsingDisabled()
              ? 0
              : getConfiguration().getListDelimiter();
      PropertiesConfiguration.PropertiesWriter writer =
          new PropertiesConfiguration.PropertiesWriter(out, delimiter);
      if (getHeaderComment() != null) {
        writer.writeln(getCanonicalHeaderComment(true));
        writer.writeln(null);
      }

      for (Iterator<String> it = getKeys().iterator(); it.hasNext(); ) {
        String key = it.next();
        if (getConfiguration().containsKey(key)) {

          // Output blank lines before property
          for (int i = 0; i < getBlancLinesBefore(key); i++) {
            writer.writeln(null);
          }

          // Output the comment
          if (getComment(key) != null) {
            writer.writeln(getCanonicalComment(key, true));
          }

          // Output the property and its value
          boolean singleLine =
              (isForceSingleLine() || isSingleLine(key))
                  && !getConfiguration().isDelimiterParsingDisabled();
          writeProperty(writer, key, getConfiguration().getProperty(key), singleLine);
        }
      }
      writer.flush();
    } catch (IOException ioex) {
      throw new ConfigurationException(ioex);
    }
  }

  @SuppressWarnings("nls")
  public void writeProperty(
      PropertiesWriter writer, String key, Object value, boolean forceSingleLine)
      throws IOException {
    String v = escapeValue(value);
    writer.write(escapeKey(key));
    writer.write(" = ");
    writer.write(v);
    writer.writeln(null);
  }

  @SuppressWarnings("nls")
  private String escapeValue(Object value) {
    String escapedValue = StringEscapeUtils.escapeJava(String.valueOf(value));
    return StringUtils.replace(escapedValue, "\\/", "/");
  }

  private String escapeKey(String key) {
    StringBuilder newkey = new StringBuilder();

    for (int i = 0; i < key.length(); i++) {
      char c = key.charAt(i);

      if (ArrayUtils.contains(SEPARATORS, c) || ArrayUtils.contains(WHITE_SPACE, c)) {
        // escape the separator
        newkey.append('\\');
        newkey.append(c);
      } else {
        newkey.append(c);
      }
    }

    return newkey.toString();
  }
}
