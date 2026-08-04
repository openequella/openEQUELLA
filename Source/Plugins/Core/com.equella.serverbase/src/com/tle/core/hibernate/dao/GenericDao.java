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

package com.tle.core.hibernate.dao;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.core.dao.helpers.Pagination;
import java.io.Serializable;
import java.util.List;
import java.util.function.Function;
import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;

@NonNullByDefault
public interface GenericDao<T, ID extends Serializable> {
  Class<T> getPersistentClass();

  ID save(T entity);

  void update(T entity);

  T merge(T entity);

  <O> O mergeAny(O entity);

  void saveOrUpdate(T entity);

  void saveAny(Object entity);

  void delete(T entity);

  void deleteAny(@Nullable Object object);

  void unlinkFromSession(Object obj);

  <A> A findAnyById(Class<A> clazz, Serializable id);

  <A> List<A> findAnyByCriteria(
      final DetachedCriteria criteria, @Nullable Integer firstResult, @Nullable Integer maxResults);

  @Nullable
  T findById(ID id);

  /**
   * Find an entity by criteria.
   *
   * @param criterion the criteria to filter by
   * @return the entity matching the criteria, or null if no entity matches
   */
  @Nullable
  T findByCriteria(Criterion... criterion);

  long countByCriteria(Criterion... criterion);

  List<T> findAllByCriteria(Criterion... criterion);

  Object findByDetachedCriteria(
      DetachedCriteria criteria, final Function<Criteria, Object> process);

  /**
   * @param order can be null if no order is required
   * @param maxResults can be -1 to retrieve all results.
   */
  List<T> findAllByCriteria(@Nullable Order order, int maxResults, Criterion... criterion);

  /**
   * Find all entities by criteria with optional order, first result and max results.
   *
   * @param order can be null if no order is required
   * @param maxResults can be -1 to retrieve all results.
   * @param firstResult can be -1 to have no offset.
   * @param criterion the criteria to filter by
   * @return the list of entities matching the criteria
   * @deprecated Use {@link #findAllByCriteria(Order, Pagination, Criterion...)} instead.
   */
  @Deprecated(forRemoval = false)
  List<T> findAllByCriteria(
      @Nullable Order order, int firstResult, int maxResults, Criterion... criterion);

  /**
   * Find all entities by criteria with optional order and pagination.
   *
   * @param order can be null if no order is required
   * @param pagination the pagination to use
   * @param criterion the criteria to filter by
   * @return the list of entities matching the criteria
   */
  List<T> findAllByCriteria(@Nullable Order order, Pagination pagination, Criterion... criterion);

  void flush();

  void clear();

  void evict(T object);
}
