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

package com.tle.common.i18n;

public interface StringLookup {

  String text(String key, Object... vals);

  String key(String local);

  StringLookup prefix(String prefix);

  static StringLookup prefixed(String pfx) {
    return new StringLookup() {
      @Override
      public String text(String key, Object... vals) {
        return CurrentLocale.get(key(key), vals);
      }

      @Override
      public String key(String local) {
        return pfx + "." + local;
      }

      @Override
      public StringLookup prefix(String prefix) {
        return StringLookup.prefixed(key(prefix));
      }
    };
  }
}
