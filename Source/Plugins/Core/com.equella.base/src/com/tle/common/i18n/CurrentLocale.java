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

package com.tle.common.i18n;

import com.tle.beans.entity.LanguageBundle;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.core.plugins.AbstractPluginService;
import com.tle.core.plugins.PluginTracker;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CurrentLocale {
  private static Logger LOGGER = LoggerFactory.getLogger(CurrentLocale.class);

  private static AbstractCurrentLocale implementation;

  private static synchronized AbstractCurrentLocale getImpl() {
    if (implementation == null) {
      implementation =
          new PluginTracker<AbstractCurrentLocale>(
                  AbstractPluginService.get(), "com.tle.common.i18n", "currentLocaleImpl", null)
              .setBeanKey("bean")
              .getBeanList()
              .get(0);
    }
    return implementation;
  }

  public static String get(final String key, final Object... values) {
    return getImpl().get(key, values);
  }

  public static String getFormatForKey(String key) {
    return getImpl().resolveKey(key).getSecond();
  }

  public static String get(LanguageBundle bundle) {
    return getImpl().get(bundle);
  }

  public static String get(LanguageBundle bundle, String defaultResult) {
    return getImpl().get(bundle, defaultResult);
  }

  public static <T> T get(Map<String, T> values) {
    return getImpl().get(values);
  }

  public static Locale getLocale() {
    return getImpl().getLocale();
  }

  public static ResourceBundle getResourceBundle() {
    return getImpl().getResourceBundle();
  }

  public static boolean isRightToLeft() {
    return getImpl().isRightToLeft();
  }

  public static String toString(long num) {
    return getImpl().toString(num);
  }

  public static String toString(double num) {
    return getImpl().toString(num);
  }

  public abstract static class AbstractCurrentLocale {
    public abstract Locale getLocale();

    public abstract ResourceBundle getResourceBundle();

    public abstract boolean isRightToLeft();

    protected abstract Pair<Locale, String> resolveKey(String key);

    public final String get(LanguageBundle bundle) {
      return LangUtils.getString(bundle, getLocale());
    }

    public final String get(LanguageBundle bundle, String defaultResult) {
      return LangUtils.getString(bundle, getLocale(), defaultResult);
    }

    public final <T> T get(Map<String, T> values) {
      return LangUtils.getClosestObjectForLocale(values, getLocale());
    }

    @SuppressWarnings("nls")
    public final String get(final String key, final Object... values) {
      if (key == null) {
        return null;
      }

      try {
        Pair<Locale, String> pair = resolveKey(key);
        String result = pair.getSecond();
        if (!Check.isEmpty(values)) {
          // ICU MessageFormat for proper I18Ningness.
          result = new MessageFormat(result, pair.getFirst()).format(values);
        }
        return result;
      } catch (MissingResourceException e) {
        LOGGER.error("Missing language string for '" + key + "'", new Error());
        return "???" + key + "???";
      }
    }

    public String toString(long num) {
      // TODO: We should ever be doing this once the applets support other
      // numeral characters
      return Long.toString(num);
    }

    public String toString(double num) {
      // TODO: We should ever be doing this once the applets support other
      // numeral characters
      return Double.toString(num);
    }
  }

  private CurrentLocale() {
    throw new Error();
  }

  public static void initialise(AbstractCurrentLocale localeImpl) {
    implementation = localeImpl;
  }
}
