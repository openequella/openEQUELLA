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

package com.tle.web.sections.equella.component;

import com.dytech.common.text.NumberStringComparator;
import com.dytech.edge.common.Constants;
import com.tle.annotation.NonNullByDefault;
import com.tle.beans.Language;
import com.tle.common.Check;
import com.tle.common.i18n.LocaleUtils;
import com.tle.common.i18n.beans.LanguageBundleBean;
import com.tle.common.i18n.beans.LanguageStringBean;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.service.LanguageService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.component.model.MultiEditBoxState;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.AbstractDisablerComponent;
import com.tle.web.sections.standard.MappedStrings;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlValueState;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import javax.inject.Inject;

@Bind
@NonNullByDefault
public class MultiEditBox extends AbstractDisablerComponent<MultiEditBoxState> {
  @Inject private LanguageService langService;

  @Component private MappedStrings langMap;

  private int size;

  public MultiEditBox() {
    super("multieditbox"); // $NON-NLS-1$
  }

  private final Comparator<Locale> localeComparator =
      new NumberStringComparator<Locale>() {
        private static final long serialVersionUID = 1L;

        @Override
        public String convertToString(Locale locale) {
          return locale.getDisplayName();
        }
      };

  @Override
  public void registered(String id, SectionTree tree) {
    super.registered(id, tree);
    langMap.setStateful(isStateful());
  }

  @Override
  protected void prepareModel(RenderContext info) {
    super.prepareModel(info);
    final MultiEditBoxState state = getState(info);
    Map<String, HtmlValueState> map = new LinkedHashMap<String, HtmlValueState>();

    TreeSet<Locale> languages = new TreeSet<Locale>(localeComparator);
    languages.addAll(getContributeLocales());
    languages.addAll(getBundleLocales(info));

    for (Locale locale : languages) {
      String id = locale.toString();
      HtmlValueState valState = langMap.getValueState(info, id);

      // Build format into: Language [(country)] where country
      String displayString = Constants.BLANK;

      String displayLanguage = locale.getDisplayLanguage();
      String displayCountry = locale.getCountry();
      displayString =
          !Check.isEmpty(displayCountry)
              ? MessageFormat.format("{0} ({1})", displayLanguage, displayCountry)
              : displayLanguage; //$NON-NLS-1$

      valState.setLabel(new TextLabel(displayString));
      map.put(id, valState);
    }
    state.setSize(size);
    state.setLocaleMap(map);
  }

  private Set<Locale> getContributeLocales() {
    Set<Locale> locales = new TreeSet<Locale>(localeComparator);
    List<Language> languages = langService.getLanguages();
    for (Language language : languages) {
      locales.add(language.getLocale());
    }
    return locales;
  }

  private Set<Locale> getBundleLocales(SectionInfo info) {
    Set<Locale> locales = new TreeSet<Locale>(localeComparator);
    Set<Entry<String, String>> bundleLangs = langMap.getValuesMap(info).entrySet();
    for (Entry<String, String> bundleLang : bundleLangs) {
      if (!Check.isEmpty(bundleLang.getValue())) {
        Locale l = LocaleUtils.parseLocale(bundleLang.getKey());
        locales.add(l);
      }
    }
    return locales;
  }

  @Override
  public Class<MultiEditBoxState> getModelClass() {
    return MultiEditBoxState.class;
  }

  public Map<String, String> getLangMap(SectionInfo info) {
    HashMap<String, String> map = new HashMap<String, String>();
    Map<String, String> valuesMap = langMap.getValuesMap(info);
    for (Map.Entry<String, String> entry : valuesMap.entrySet()) {
      String str = entry.getValue();
      if (!Check.isEmpty(str)) {
        map.put(entry.getKey(), str);
      }
    }
    return map;
  }

  public void setLangMap(SectionInfo info, Map<String, String> map) {
    langMap.setValuesMap(info, map);
  }

  public LanguageBundleBean getLanguageBundle(SectionInfo info) {
    LanguageBundleBean bundle = new LanguageBundleBean();
    Map<String, LanguageStringBean> strings = new HashMap<String, LanguageStringBean>();
    for (Map.Entry<String, String> entry : langMap.getValuesMap(info).entrySet()) {
      String str = entry.getValue();
      if (!Check.isEmpty(str)) {
        String key = entry.getKey();

        LanguageStringBean langString = new LanguageStringBean();
        // langString.setBundle(bundle);
        langString.setLocale(key);
        langString.setText(str);

        strings.put(key, langString);
      }
    }
    bundle.setStrings(strings);

    return bundle;
  }

  public void setLanguageBundle(SectionInfo info, LanguageBundleBean bundle) {
    Map<String, LanguageStringBean> strings = bundle.getStrings();
    Map<String, String> map = new HashMap<String, String>();
    for (LanguageStringBean langString : strings.values()) {
      if (!Check.isEmpty(langString.getText())) {
        map.put(langString.getLocale(), langString.getText());
      }
    }
    langMap.setValuesMap(info, map);
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }
}
