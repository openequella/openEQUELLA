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

package com.tle.core.i18n;

import com.dytech.common.io.UnicodeReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.ResourceBundle;

public class PluginResourcesBundle extends ResourceBundle {
  private final Map<String, Object> lookup = new HashMap<String, Object>();

  @Override
  public Enumeration<String> getKeys() {
    final Iterator<String> keyIter = lookup.keySet().iterator();
    final Enumeration<String> parentEnum = (parent != null) ? parent.getKeys() : null;

    return new Enumeration<String>() {
      private String next = null;

      @Override
      public boolean hasMoreElements() {
        if (next == null) {
          if (keyIter.hasNext()) {
            next = keyIter.next();
          } else if (parentEnum != null) {
            while (next == null && parentEnum.hasMoreElements()) {
              next = parentEnum.nextElement();
              if (lookup.containsKey(next)) {
                next = null;
              }
            }
          }
        }
        return next != null;
      }

      @Override
      public String nextElement() {
        if (hasMoreElements()) {
          String result = next;
          next = null;
          return result;
        } else {
          throw new NoSuchElementException();
        }
      }
    };
  }

  @Override
  protected Object handleGetObject(String key) {
    return lookup.get(key);
  }

  public boolean isEmpty() {
    return lookup.isEmpty();
  }

  public void addProperties(InputStream in, String prepend) throws IOException {
    addProperties(in, prepend, false);
  }

  @SuppressWarnings("nls")
  public void addProperties(InputStream in, String prepend, boolean isXml) throws IOException {
    Properties properties = new Properties();
    if (isXml) {
      properties.loadFromXML(in);
    } else {
      properties.load(new UnicodeReader(in, "UTF-8"));
    }

    for (Entry<Object, Object> entry : properties.entrySet()) {
      String key = (String) entry.getKey();
      if (prepend != null) {
        if (key == null || key.length() == 0) {
          throw new Error("Missing key on language string in " + prepend + " plugin");
        }
        if (key.charAt(0) == '/') {
          key = key.substring(1);
        } else {
          key = prepend + key;
        }
      }
      lookup.put(key, entry.getValue());
    }
  }
}
