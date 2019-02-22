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

import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.entity.LanguageBundle;
import com.tle.common.NameValue;
import com.tle.common.i18n.BundleReference;
import java.util.ArrayList;
import java.util.List;

public class BundleNameValue extends NameValue {
  private static final long serialVersionUID = 1L;
  private BundleCache cache;
  private Object name;

  public BundleNameValue(String name, String value) {
    this(name, value, null);
  }

  public BundleNameValue(Object name, String value, BundleCache cache) {
    super("", value); // $NON-NLS-1$
    this.name = name;
    this.cache = cache;
    if (cache == null && !(name instanceof String)) {
      throw new RuntimeException("Must be a key value when cache is null"); // $NON-NLS-1$
    }
    if (cache != null) {
      if (name instanceof LanguageBundle) {
        cache.addBundle(name);
      } else if (name instanceof Long) {
        cache.addBundleId((Long) name);
      }
    }
  }

  @Override
  public String getName() {
    return TextBundle.getLocalString(name, cache, null, ""); // $NON-NLS-1$
  }

  @Override
  public String getValue() {
    return getSecond();
  }

  @Override
  public String getLabel() {
    return TextBundle.getLocalString(name, cache, null, ""); // $NON-NLS-1$
  }

  public static List<BundleNameValue> convertList(
      List<? extends BundleReference> refList, BundleCache cache) {
    List<BundleNameValue> namesValues = new ArrayList<BundleNameValue>(refList.size());
    for (BundleReference reference : refList) {
      BundleNameValue nv =
          new BundleNameValue(reference.getBundleId(), reference.getValue(), cache);
      namesValues.add(nv);
    }
    return namesValues;
  }

  public static List<BundleNameValue> convertListUuid(
      List<BaseEntityLabel> refList, BundleCache cache) {
    List<BundleNameValue> namesValues = new ArrayList<BundleNameValue>(refList.size());
    for (BaseEntityLabel reference : refList) {
      BundleNameValue nv = new BundleNameValue(reference.getBundleId(), reference.getUuid(), cache);
      namesValues.add(nv);
    }
    return namesValues;
  }
}
