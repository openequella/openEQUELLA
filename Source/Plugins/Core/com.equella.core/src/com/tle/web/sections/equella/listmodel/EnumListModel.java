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

package com.tle.web.sections.equella.listmodel;

import java.util.Arrays;

import com.tle.web.sections.equella.utils.KeyDescriptionOption;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;

public class EnumListModel<T extends Enum<T>> extends SimpleHtmlListModel<T> {
  protected final String keyPrefix;
  private final boolean toLowerCase;
  private final String descriptionPostfix;

  public EnumListModel(String keyPrefix, T... values) {
    this(keyPrefix, null, false, values);
  }

  public EnumListModel(String keyPrefix, boolean toLowerCase, T... values) {
    this(keyPrefix, null, toLowerCase, values);
  }

  public EnumListModel(
      String keyPrefix, String descriptionPostfix, boolean toLowerCase, T... values) {
    this.keyPrefix = keyPrefix;
    this.toLowerCase = toLowerCase;
    this.descriptionPostfix = descriptionPostfix;
    addAll(Arrays.asList(values));
  }

  @Override
  protected Option<T> convertToOption(T enumObj) {
    String name = enumObj.name();
    Label description = null;
    String key = getKey(name);
    if (descriptionPostfix != null) {
      description = new KeyLabel(key + descriptionPostfix);
    }
    return new KeyDescriptionOption<T>(key, description, getValue(name), enumObj);
  }

  protected String getKey(String name) {
    return keyPrefix + name.toLowerCase();
  }

  protected String getValue(String name) {
    if (toLowerCase) {
      return name.toLowerCase();
    }
    return name;
  }
}
