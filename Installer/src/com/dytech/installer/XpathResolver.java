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

package com.dytech.installer;

import com.dytech.common.text.Resolver;
import com.dytech.devlib.PropBagEx;

public class XpathResolver implements Resolver {
  protected PropBagEx resultBag;

  public XpathResolver(PropBagEx resultBag) {
    this.resultBag = resultBag;
  }

  @Override
  public String valueOf(String s) {
    String[] values = s.split("#");

    String content = resultBag.getNode(values[0]);
    for (int i = 1; i < values.length; i++) {
      if ("f".equals(values[i])) {
        if ("win32".equals(resultBag.getNode("installer/platform"))) {
          content = process(values[i], content);
        }
      } else {
        content = process(values[i], content);
      }
    }
    return content;
  }

  protected String process(String command, String value) {
    switch (command.charAt(0)) {
      // Translate: Replace all instances of first character with second
      // character.
      case 't':
        return value.replace(command.charAt(1), command.charAt(2));

      // Conditional: Sets value to nothing unless the parameter
      // evaluates to 'true'.
      case 'c':
        boolean not = command.charAt(1) == '!';
        if (not) command = command.substring(2);
        else command = command.substring(1);

        String result = resultBag.getNode(command);
        if (result.equals("true") || (not && result.equals("false"))) return value;
        else return "";

      // Literal: Replace current value with this literal
      case 'l':
        return command.substring(1);

      // File: check for whitespace and wrap in quotes if found
      case 'f':
        if (value.indexOf(" ") != -1) {
          value = value.replace(" ", "\" \"");
        }
        return value;

      default:
        return value;
    }
  }
}
