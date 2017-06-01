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

package com.tle.core.hibernate.dao;

import java.io.Serializable;
import java.util.List;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;

/**
 * @author Nicholas Read
 */
@NonNullByDefault
public interface GenericDao<T, ID extends Serializable>
{
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

	<A> List<A> findAnyByCriteria(final DetachedCriteria criteria, @Nullable Integer firstResult,
		@Nullable Integer maxResults);

	@Nullable
	T findById(ID id);

	@Nullable
	T findByCriteria(Criterion... criterion);

	long countByCriteria(Criterion... criterion);

	List<T> findAllByCriteria(Criterion... criterion);

	/**
	 * @param order can be null if no order is required
	 * @param maxResults can be -1 to retrieve all results.
	 */
	List<T> findAllByCriteria(@Nullable Order order, int maxResults, Criterion... criterion);

	/**
	 * @param order can be null if no order is required
	 * @param maxResults can be -1 to retrieve all results.
	 */
	List<T> findAllByCriteria(@Nullable Order order, int firstResult, int maxResults, Criterion... criterion);

	void flush();

	void clear();

	void evict(T object);
}
