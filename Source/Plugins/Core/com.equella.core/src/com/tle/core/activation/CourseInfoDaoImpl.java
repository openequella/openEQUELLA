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

package com.tle.core.activation;

import com.tle.beans.activation.ActivateRequest;
import com.tle.beans.item.cal.request.CourseInfo;
import com.tle.common.Check;
import com.tle.core.entity.EnumerateOptions;
import com.tle.core.entity.dao.impl.AbstractEntityDaoImpl;
import com.tle.core.guice.Bind;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Singleton;
import org.hibernate.Query;

@Bind(CourseInfoDao.class)
@Singleton
public class CourseInfoDaoImpl extends AbstractEntityDaoImpl<CourseInfo> implements CourseInfoDao {
  public CourseInfoDaoImpl() {
    super(CourseInfo.class);
  }

  @Override
  @SuppressWarnings({"unchecked", "nls"})
  public List<Class<?>> getReferencingClasses(long id) {
    List<Class<?>> classes = new ArrayList<Class<?>>();

    if (((List<Long>)
                getHibernateTemplate()
                    .findByNamedParam(
                        "select count(*) from ActivateRequest a where a.course.id = :id", "id", id))
            .get(0)
        != 0) {
      classes.add(ActivateRequest.class);
    }
    return classes;
  }

  @Override
  protected DefaultSearchListCallback getSearchListCallback(
      final ListCallback nestedCallback, final EnumerateOptions options) {
    ListCallback callback = nestedCallback;
    final Boolean includeDisabled = options.isIncludeDisabled();
    if (includeDisabled != null) {
      callback = new EnabledCallback(callback, !includeDisabled);
    }
    if (options.getOffset() != 0 || options.getMax() != -1) {
      callback = new PagedListCallback(callback, options.getOffset(), options.getMax());
    }
    return new CourseSearchListCallback(
        callback, options.getQuery(), (String) options.getParameters().get("code"));
  }

  public static class CourseSearchListCallback extends DefaultSearchListCallback {
    final String code;

    public CourseSearchListCallback(ListCallback wrappedCallback, String freetext, String code) {
      super(wrappedCallback, freetext);
      this.code = code;
    }

    @Override
    public String createAdditionalWhere() {
      String where = null;
      if (freetext != null) {
        // CAST required for SQLServer
        where =
            "LOWER(CAST(ns.text AS string)) LIKE :freetext"
                + " OR LOWER(CAST(ds.text AS string)) LIKE :freetext"
                + " OR LOWER(be.code) LIKE :freetext";
      }
      if (code != null) {
        where = concat(where, "LOWER(be.code) LIKE :code", " OR ");
      }
      return (Check.isEmpty(where) ? where : "(" + where + ")");
    }

    @Override
    public String createOrderBy() {
      return "LOWER(be.code)";
    }

    @Override
    public void processQuery(Query query) {
      super.processQuery(query);
      if (code != null) {
        query.setParameter("code", code);
      }
    }
  }
}
