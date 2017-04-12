package com.tle.core.dao;

import java.util.List;

import com.tle.beans.tree.TreeNodeInterface;
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
