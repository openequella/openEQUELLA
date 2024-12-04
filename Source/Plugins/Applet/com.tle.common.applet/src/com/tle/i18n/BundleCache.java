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

package com.tle.i18n;

import com.tle.beans.Language;
import com.tle.beans.NameId;
import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.item.Item;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.common.i18n.BundleReference;
import com.tle.core.remoting.RemoteLanguageService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class BundleCache {
  private static final Map<Long, String> names = new HashMap<Long, String>();
  private static final long INVALID_KEY = -1;

  private static Set<Locale> languages;
  private static RemoteLanguageService languageService;

  public static void initialise(RemoteLanguageService langService) {
    BundleCache.languageService = langService;
    refreshLanguages();
  }

  public static synchronized void refreshLanguages() {
    languages = null;
  }

  public static synchronized Set<Locale> getLanguages() {
    if (languages == null) {
      languages = new HashSet<Locale>();
      for (Language lang : languageService.getLanguages()) {
        languages.add(lang.getLocale());
      }
      languages = Collections.unmodifiableSet(languages);
    }
    return languages;
  }

  public static List<Language> getStoredLanguages() {
    return languageService.getLanguages();
  }

  public static <T> String getString(T t, GetBundleId<T> getter) {
    ensureCached(Collections.singleton(t), getter);
    return names.get(getter.getBundleId(t));
  }

  public static String getString(LanguageBundle bundle) {
    return getString(bundle, LANGUAGE_BUNDLE_GETTER);
  }

  public static String getString(BundleReference ref) {
    return getString(ref, BUNDLE_REF_GETTER);
  }

  public static NameValue getNameValue(BundleReference ref) {
    return new NameValue(getString(ref), ref.getValue());
  }

  public static List<NameValue> getNameValues(Collection<? extends BundleReference> refs) {
    ensureCached(refs, BUNDLE_REF_GETTER);

    List<NameValue> nvs = new ArrayList<NameValue>();
    for (BundleReference ref : refs) {
      nvs.add(new NameValue(names.get(ref.getBundleId()), ref.getValue()));
    }
    return nvs;
  }

  public static List<NameValue> getNameUuidValues(Collection<BaseEntityLabel> labels) {
    ensureCached(labels, BUNDLE_REF_GETTER);

    List<NameValue> nvs = new ArrayList<NameValue>();
    for (BaseEntityLabel label : labels) {
      nvs.add(new NameValue(names.get(label.getBundleId()), label.getUuid()));
    }
    return nvs;
  }

  public static List<NameId> getNameIds(Collection<? extends BundleReference> refs) {
    ensureCached(refs, BUNDLE_REF_GETTER);

    List<NameId> nis = new ArrayList<NameId>();
    for (BundleReference ref : refs) {
      nis.add(new NameId(names.get(ref.getBundleId()), ref.getIdValue()));
    }
    return nis;
  }

  public static void invalidate(BundleReference ref) {
    names.remove(ref.getBundleId());
  }

  public static void invalidate(long bundleId) {
    names.remove(bundleId);
  }

  public static <T> void ensureCached(Collection<? extends BundleReference> refs) {
    ensureCached(refs, BUNDLE_REF_GETTER);
  }

  public static <T> void ensureCached(Collection<? extends T> elems, GetBundleId<T> getter) {
    if (!Check.isEmpty(elems)) {
      Set<Long> notCached = new HashSet<Long>();
      for (T elem : elems) {
        long bundleId = getter.getBundleId(elem);
        if (bundleId != INVALID_KEY && !names.containsKey(bundleId)) {
          notCached.add(bundleId);
        }
      }

      if (!notCached.isEmpty()) {
        names.putAll(languageService.getNames(notCached));
      }
    }
  }

  public abstract static class GetBundleId<T> {
    public long getBundleId(T t) {
      LanguageBundle bundle = getLanguageBundle(t);
      return bundle != null ? bundle.getId() : INVALID_KEY;
    }

    protected LanguageBundle getLanguageBundle(T t) {
      return null;
    }
  }

  public static final GetBundleId<BundleReference> BUNDLE_REF_GETTER =
      new GetBundleId<BundleReference>() {
        @Override
        public long getBundleId(BundleReference t) {
          return t.getBundleId();
        }
      };

  public static final GetBundleId<LanguageBundle> LANGUAGE_BUNDLE_GETTER =
      new GetBundleId<LanguageBundle>() {
        @Override
        protected LanguageBundle getLanguageBundle(LanguageBundle t) {
          return t;
        }
      };

  public static final GetBundleId<BaseEntity> BASE_ENTITY_GETTER =
      new GetBundleId<BaseEntity>() {
        @Override
        protected LanguageBundle getLanguageBundle(BaseEntity t) {
          return t.getName();
        }
      };

  public static final GetBundleId<Item> ITEM_NAME_GETTER =
      new GetBundleId<Item>() {
        @Override
        protected LanguageBundle getLanguageBundle(Item t) {
          return t.getName();
        }
      };

  public static final GetBundleId<Item> ITEM_DESCRIPTION_GETTER =
      new GetBundleId<Item>() {
        @Override
        protected LanguageBundle getLanguageBundle(Item t) {
          return t.getDescription();
        }
      };

  private BundleCache() {
    throw new Error();
  }
}
