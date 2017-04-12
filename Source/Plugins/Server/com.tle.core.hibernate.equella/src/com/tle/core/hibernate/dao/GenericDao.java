/*
 * Created on Oct 25, 2005
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
