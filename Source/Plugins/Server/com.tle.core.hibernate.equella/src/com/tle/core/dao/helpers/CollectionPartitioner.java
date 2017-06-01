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

package com.tle.core.dao.helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

public abstract class CollectionPartitioner<T, RESULT> implements HibernateCallback
{
	// Oracle has a list size limit of 1000
	private static final int MAX_COLLECTION_SIZE = 900;

	private final Collection<T> collection;

	public CollectionPartitioner(Collection<T> collection)
	{
		this.collection = collection;
	}

	@Override
	public Object doInHibernate(Session session) throws HibernateException
	{
		return withSession(session);
	}

	public List<RESULT> withSession(Session session)
	{
		// Optimisation for when target size is less than the max
		if( collection.size() <= MAX_COLLECTION_SIZE )
		{
			return doQuery(session, collection);
		}

		// We need a list, so cast it if possible, otherwise recreate (urgh). We
		// may be able to optimise this by creating a SubCollectionView type
		// thing - possible?
		List<T> list = (collection instanceof List) ? (List<T>) collection : new ArrayList<T>(collection);

		List<RESULT> results = new ArrayList<RESULT>();

		final int fullSegments = (int) Math.floor(list.size() / (double) MAX_COLLECTION_SIZE);
		for( int i = 0; i < fullSegments; i++ )
		{
			final int start = i * MAX_COLLECTION_SIZE;
			results.addAll(doQuery(session, list.subList(start, start + MAX_COLLECTION_SIZE)));
		}

		final int finalSegmentSize = list.size() % MAX_COLLECTION_SIZE;
		if( finalSegmentSize > 0 )
		{
			final int start = fullSegments * MAX_COLLECTION_SIZE;
			results.addAll(doQuery(session, list.subList(start, start + finalSegmentSize)));
		}

		return results;
	}

	public abstract List<RESULT> doQuery(Session session, Collection<T> collection);
}
