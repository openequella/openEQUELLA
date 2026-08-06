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

package com.tle.common.institution;

import com.tle.beans.Institution;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

public final class CurrentInstitution {
  /** The name of the attribute by which institution owned entities refer to their institution. */
  private static final String INSTITUTION_ATTRIBUTE = "institution";

  private static final ThreadLocal<Institution> local = new ThreadLocal<Institution>();

  public static Institution get() {
    return local.get();
  }

  public static void set(Institution institution) {
    local.set(institution);
  }

  public static void remove() {
    local.remove();
  }

  /**
   * @return a Hibernate criterion that filters by the current institution - based on property name
   *     of "institution"
   */
  public static Criterion equalityCriteria() {
    return Restrictions.eq(INSTITUTION_ATTRIBUTE, get());
  }

  /**
   * The JPA criteria counterpart of {@link #equalityCriteria()}, for queries built with a {@code
   * CriteriaBuilder} rather than the legacy Hibernate {@code Criteria} API.
   *
   * @param criteriaBuilder the builder the query is being built with
   * @param path the root (or join) of the institution owned entity being filtered
   * @return a predicate that filters by the current institution - based on property name of
   *     "institution"
   */
  public static Predicate equalityPredicate(CriteriaBuilder criteriaBuilder, Path<?> path) {
    return criteriaBuilder.equal(path.get(INSTITUTION_ATTRIBUTE), get());
  }

  private CurrentInstitution() {
    throw new Error();
  }
}
