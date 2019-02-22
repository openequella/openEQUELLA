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

package com.tle.upgrade;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class PropertyMover {
  private File dest;
  private File source;
  private String prefix;
  private Pattern pattern;

  public PropertyMover(File source, File dest, Pattern pattern, String prefix) {
    this.source = source;
    this.dest = dest;
    this.pattern = pattern;
    this.prefix = prefix;
  }

  public void move(UpgradeResult result) throws IOException, ConfigurationException {
    final Map<String, String> optionMap = new HashMap<String, String>();

    LineFileModifier modifier =
        new LineFileModifier(source, result) {

          @Override
          protected String processLine(String line) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
              boolean comment = matcher.group(1).trim().startsWith("#"); // $NON-NLS-1$
              if (!comment) {
                optionMap.put(prefix + matcher.group(2), matcher.group(3));
              }
              return null;
            }
            return line;
          }
        };
    modifier.update();

    if (dest != null) {
      if (!optionMap.isEmpty()) {
        new PropertyFileModifier(dest) {
          @Override
          protected boolean modifyProperties(PropertiesConfiguration props) {
            for (String key : optionMap.keySet()) {
              String value = optionMap.get(key);
              props.setProperty(key, value);
            }
            return true;
          }
        }.updateProperties();
      }
    }
  }
}
