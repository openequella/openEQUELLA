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

package com.tle.core.i18n.dao.impl;

import com.tle.beans.Language;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.LocaleUtils;
import com.tle.core.dao.helpers.CollectionPartitioner;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericInstitionalDaoImpl;
import com.tle.core.i18n.dao.LanguageDao;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.inject.Singleton;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

@Bind(LanguageDao.class)
@Singleton
public class LanguageDaoImpl extends GenericInstitionalDaoImpl<Language, Long>
    implements LanguageDao {
  private enum QueryType {
    CLOSEST("getClosest"),
    MORE_SPECIFIC("getMoreSpecific"),
    LEAST_SPECIFIC("getLeastSpecific"); // $NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    private final String queryName;

    private QueryType(String queryName) {
      this.queryName = queryName;
    }

    public String getQueryName() {
      return queryName;
    }
  }

  public LanguageDaoImpl() {
    super(Language.class);
  }

  /*
   * (non-Javadoc)
   * @see com.tle.core.dao.LanguageDao#getNames(java.util.Collection)
   */
  @Override
  @SuppressWarnings("unchecked")
  public Map<Long, String> getNames(final Collection<Long> bundleRefs) {
    if (bundleRefs.isEmpty()) {
      return Collections.emptyMap();
    }

    return (Map<Long, String>)
        getHibernateTemplate()
            .execute(
                new HibernateCallback() {
                  @Override
                  public Object doInHibernate(Session session) throws HibernateException {
                    Map<Long, String> nameMap = new HashMap<Long, String>();

                    // Find the closest locale matches for the bundles
                    populateNameMap(session, bundleRefs, QueryType.CLOSEST, nameMap);

                    // If some bundles were not found...
                    Collection<Long> missing = getMissingBundleRefs(bundleRefs, nameMap);
                    if (missing != null) {
                      // ...try to find more specific locales for the user's
                      // locale
                      populateNameMap(session, missing, QueryType.MORE_SPECIFIC, nameMap);

                      // If some bundles were still not found...
                      missing = getMissingBundleRefs(bundleRefs, nameMap);
                      if (missing != null) {
                        // ...find the lowest prioritised string for the bundle
                        populateNameMap(session, missing, QueryType.LEAST_SPECIFIC, nameMap);
                      }
                    }

                    return nameMap;
                  }
                });
  }

  private Collection<Long> getMissingBundleRefs(
      Collection<Long> bundleRefs, Map<Long, String> nameMap) {
    final int missingCount = bundleRefs.size() - nameMap.size();

    // we should always have the same or more of the original bundleRefs
    assert missingCount >= 0;

    List<Long> missingRefs = null;
    if (missingCount > 0) {
      // We need to search for the default values
      missingRefs = new ArrayList<Long>();
      for (Long ref : bundleRefs) {
        if (!nameMap.containsKey(ref)) {
          missingRefs.add(ref);
        }
      }
    }
    return missingRefs;
  }

  private void populateNameMap(
      final Session session,
      Collection<Long> bundleRefs,
      final QueryType queryType,
      final Map<Long, String> nameMap) {
    new CollectionPartitioner<Long, Object>(bundleRefs) {
      @Override
      @SuppressWarnings("unchecked")
      public List<Object> doQuery(Session session, Collection<Long> collection) {
        Query query = session.getNamedQuery(queryType.getQueryName());
        query.setParameterList("bundles", collection); // $NON-NLS-1$
        Locale locale = CurrentLocale.getLocale();
        switch (queryType) {
          case CLOSEST:
            query.setParameterList(
                "locales", LocaleUtils.getAllPossibleKeys(locale)); // $NON-NLS-1$
            break;

          case MORE_SPECIFIC:
            String langOnly = locale.getLanguage() + '_';
            String moreSpecific = langOnly + locale.getCountry();
            query.setParameter("locale", langOnly + '%'); // $NON-NLS-1$
            query.setParameter("locale2", moreSpecific + '%'); // $NON-NLS-1$
            break;

          default:
            // LEAST_SPECIFIC ... nfa
            break;
        }

        List<Object[]> results = query.list();
        for (Object[] res : results) {
          Object text = res[1];
          nameMap.put(((Number) res[0]).longValue(), (String) text);
        }
        return Collections.emptyList();
      }
    }.withSession(session);
  }

  @Override
  public void deleteBundles(Collection<Long> bundles) {
    if (!bundles.isEmpty()) {
      StringBuilder query = new StringBuilder("("); // $NON-NLS-1$
      for (int i = 0; i < bundles.size(); i++) {
        if (i > 0) {
          query.append(',');
        }
        query.append('?');
      }
      query.append(')');
      getHibernateTemplate()
          .bulkUpdate(
              "delete LanguageString where bundle.id in " + query.toString(), // $NON-NLS-1$
              bundles.toArray());
      getHibernateTemplate()
          .bulkUpdate(
              "delete LanguageBundle where id in " + query.toString(), // $NON-NLS-1$
              bundles.toArray());
    }
  }
}
