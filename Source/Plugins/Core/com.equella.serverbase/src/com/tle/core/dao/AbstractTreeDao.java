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

package com.tle.core.dao;

import java.util.List;

import com.tle.common.institution.TreeNodeInterface;
import com.tle.core.hibernate.dao.GenericDao;

/**
 * @author Nicholas Read
 */
public interface AbstractTreeDao<T extends TreeNodeInterface<T>> extends GenericDao<T, Long>
{
	void updateWithNoParentChange(T entity);

	void delete(T node, DeleteAction<T> action);

	List<T> getRootNodes(String orderBy);

	List<T> getChildrenForNode(T node, String orderBy);

	List<Long> getChildrenIdsForNodeId(long nodeId, String orderBy);

	List<T> getAllSubnodeForNode(T node);

	int countRootNodes();

	int countSubnodesForNode(T node);

	List<T> listAll();

	/**
	 * Lists node IDs in top-down order. The only ordering guarantee is that a
	 * node is guaranteed to show before any node with more parents. For
	 * example, all the root nodes are guaranteed to be before any of the first
	 * level nodes, which are all guaranteed to be before any second level
	 * nodes, and so on. Nodes at the same depth but from different parents may
	 * be mixed together.
	 */
	List<Long> listIdsInOrder();

	List<Long> enumerateIdsInOrder();

	void deleteInOrder(Long node);

	interface DeleteAction<T>
	{
		void beforeDelete(T node);
	}
}
