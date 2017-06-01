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

package com.tle.admin.workflow.tree;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import com.tle.common.i18n.LangUtils;
import com.tle.common.workflow.node.SerialNode;
import com.tle.common.workflow.node.WorkflowItem;
import com.tle.common.workflow.node.WorkflowNode;
import com.tle.common.workflow.node.WorkflowTreeNode;

public class WorkflowTreeModel implements TreeModel
{
	private WorkflowTreeNode root;
	protected EventListenerList listenerList;

	public WorkflowTreeModel()
	{
		// Root is never null, but the first child is the only one allowed
		listenerList = new EventListenerList();
	}

	public void setRoot(WorkflowTreeNode root)
	{
		if( root == null )
		{
			root = new SerialNode(
				LangUtils.createTempLangugageBundle("com.tle.admin.workflow.tree.workflowtreemodel.start")); //$NON-NLS-1$
		}
		this.root = root;
	}

	public WorkflowTreeNode getRootNode()
	{
		return root;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#getRoot()
	 */
	@Override
	public Object getRoot()
	{
		return root;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
	 */
	@Override
	public int getChildCount(Object parent)
	{
		if( parent instanceof WorkflowItem )
		{
			return 0;
		}
		WorkflowTreeNode node = (WorkflowTreeNode) parent;
		return node.numberOfChildren();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
	 */
	@Override
	public boolean isLeaf(Object node)
	{
		WorkflowNode wnode = (WorkflowNode) node;
		boolean isLeaf = true;
		if( wnode.getType() != WorkflowNode.ITEM_TYPE )
		{
			WorkflowTreeNode tnode = (WorkflowTreeNode) wnode;
			isLeaf = tnode.numberOfChildren() == 0;
		}
		return isLeaf;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#addTreeModelListener(javax.swing.event.
	 * TreeModelListener)
	 */
	@Override
	public void addTreeModelListener(TreeModelListener l)
	{
		listenerList.add(TreeModelListener.class, l);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * javax.swing.tree.TreeModel#removeTreeModelListener(javax.swing.event.
	 * TreeModelListener)
	 */
	@Override
	public void removeTreeModelListener(TreeModelListener l)
	{
		listenerList.remove(TreeModelListener.class, l);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
	 */
	@Override
	public Object getChild(Object parent, int index)
	{
		WorkflowTreeNode node = (WorkflowTreeNode) parent;
		return node.getChild(index);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object,
	 * java.lang.Object)
	 */
	@Override
	public int getIndexOfChild(Object parent, Object child)
	{
		WorkflowTreeNode node = (WorkflowTreeNode) parent;
		WorkflowNode child2 = (WorkflowNode) child;
		return node.indexOfChild(child2);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath,
	 * java.lang.Object)
	 */
	@Override
	public void valueForPathChanged(TreePath path, Object newValue)
	{
		nodeChanged((WorkflowNode) newValue);
	}

	public void add(WorkflowTreeNode parent, WorkflowNode node)
	{
		if( parent == null )
		{
			parent = root;
		}
		add(parent, node, parent.numberOfChildren());
	}

	public void add(WorkflowNode parent, WorkflowNode node, int index)
	{
		if( node != null )
		{
			if( parent == null )
			{
				parent = root;
			}
			parent.addChild(index, node);
			node.setParent(parent);
			nodesWereInserted(parent, new int[]{index});
		}
	}

	public void remove(WorkflowNode selectedNode)
	{
		if( !selectedNode.equals(root) )
		{
			WorkflowNode parent = selectedNode.getParent();
			if( parent != null )
			{
				int index = parent.indexOfChild(selectedNode);
				parent.removeChild(selectedNode);
				nodesWereRemoved(parent, new int[]{index}, new Object[]{selectedNode});
			}
		}
	}

	public WorkflowNode[] getPathToRoot(WorkflowNode node)
	{
		return getPathToRoot(node, 0);
	}

	protected WorkflowNode[] getPathToRoot(WorkflowNode aNode, int depth)
	{
		WorkflowNode[] retNodes;
		if( aNode == null )
		{
			if( depth == 0 )
			{
				return null;
			}
			retNodes = new WorkflowNode[depth];

		}
		else
		{
			depth++;
			if( aNode.equals(root) )
			{
				retNodes = new WorkflowNode[depth];
			}
			else
			{
				retNodes = getPathToRoot(aNode.getParent(), depth);
			}
			retNodes[retNodes.length - depth] = aNode;
		}
		return retNodes;
	}

	public void nodesWereInserted(WorkflowNode node, int[] childIndices)
	{
		if( listenerList != null && node != null && childIndices != null && childIndices.length > 0 )
		{
			int cCount = childIndices.length;
			Object[] newChildren = new Object[cCount];

			for( int counter = 0; counter < cCount; counter++ )
			{
				newChildren[counter] = node.getChild(childIndices[counter]);
			}
			fireTreeNodesInserted(this, getPathToRoot(node), childIndices, newChildren);
		}
	}

	public void nodesWereRemoved(WorkflowNode node, int[] childIndices, Object[] removedChildren)
	{
		if( node != null && childIndices != null )
		{
			fireTreeNodesRemoved(this, getPathToRoot(node), childIndices, removedChildren);
		}
	}

	protected void fireTreeNodesInserted(Object source, Object[] path, int[] childIndices, Object[] children)
	{
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		TreeModelEvent e = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for( int i = listeners.length - 2; i >= 0; i -= 2 )
		{
			if( listeners[i] == TreeModelListener.class )
			{
				// Lazily create the event:
				if( e == null )
				{
					e = new TreeModelEvent(source, path, childIndices, children);
				}
				((TreeModelListener) listeners[i + 1]).treeNodesInserted(e);
			}
		}
	}

	protected void fireTreeNodesRemoved(Object source, Object[] path, int[] childIndices, Object[] children)
	{
		Object[] listeners = listenerList.getListenerList();
		TreeModelEvent e = null;
		for( int i = listeners.length - 2; i >= 0; i -= 2 )
		{
			if( listeners[i] == TreeModelListener.class )
			{
				if( e == null )
				{
					e = new TreeModelEvent(source, path, childIndices, children);
				}
				((TreeModelListener) listeners[i + 1]).treeNodesRemoved(e);
			}
		}
	}

	public void nodeChanged(WorkflowNode node)
	{
		if( listenerList != null && node != null )
		{
			WorkflowNode parent = node.getParent();

			if( parent != null )
			{
				int anIndex = parent.indexOfChild(node);
				if( anIndex != -1 )
				{
					int[] cIndexs = new int[1];

					cIndexs[0] = anIndex;
					nodesChanged(parent, cIndexs);
				}
			}
			else if( node == getRoot() )
			{
				nodesChanged(root, null);
			}
		}
	}

	public void nodesChanged(WorkflowNode node, int[] childIndices)
	{
		if( node != null )
		{
			if( childIndices != null )
			{
				int cCount = childIndices.length;

				if( cCount > 0 )
				{
					Object[] cChildren = new Object[cCount];

					for( int counter = 0; counter < cCount; counter++ )
					{
						cChildren[counter] = node.getChild(childIndices[counter]);
					}
					fireTreeNodesChanged(this, getPathToRoot(node), childIndices, cChildren);
				}
			}
			else if( node == getRoot() )
			{
				fireTreeNodesChanged(this, getPathToRoot(node), null, null);
			}
		}
	}

	protected void fireTreeNodesChanged(Object source, Object[] path, int[] childIndices, Object[] children)
	{
		Object[] listeners = listenerList.getListenerList();
		TreeModelEvent e = null;
		for( int i = listeners.length - 2; i >= 0; i -= 2 )
		{
			if( listeners[i] == TreeModelListener.class )
			{
				if( e == null )
				{
					e = new TreeModelEvent(source, path, childIndices, children);
				}
				((TreeModelListener) listeners[i + 1]).treeNodesChanged(e);
			}
		}
	}

	public void up(WorkflowNode node)
	{
		if( node == null )
		{
			return;
		}

		WorkflowNode parent = node.getParent();
		int index = parent.indexOfChild(node);
		if( index == 0 )
		{
			WorkflowNode pparent = parent.getParent();
			index = pparent.indexOfChild(parent);
			parent = pparent;
		}
		else
		{
			index--;

			WorkflowNode child = parent.getChild(index);
			if( child.canAddChildren() )
			{
				parent = child;
				index = parent.numberOfChildren();
			}
		}
		remove(node);
		add(parent, node, index);
	}

	public void down(WorkflowNode node)
	{
		if( node == null )
		{
			return;
		}

		WorkflowNode parent = node.getParent();
		int index = parent.indexOfChild(node);
		if( index == parent.numberOfChildren() - 1 )
		{
			WorkflowNode pparent = parent.getParent();
			index = pparent.indexOfChild(parent) + 1;
			parent = pparent;
		}
		else
		{
			index++;

			WorkflowNode child = parent.getChild(index);
			if( child.canAddChildren() )
			{
				parent = child;
				index = 0;
			}
		}
		remove(node);
		add(parent, node, index);
	}
}
