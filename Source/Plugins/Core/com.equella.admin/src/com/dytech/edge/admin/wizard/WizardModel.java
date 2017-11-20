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

package com.dytech.edge.admin.wizard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import com.dytech.edge.admin.wizard.model.Control;
import com.tle.admin.controls.repository.ControlDefinition;
import com.tle.admin.controls.repository.ControlRepository;

/**
 * @author Nicholas Read
 */
public class WizardModel implements TreeModel
{
	/**
	 * A list of event listeners.
	 */
	private EventListenerList listenerList;

	/**
	 * The root control of the wizard model.
	 */
	private Control root;

	/**
	 * Indicates whether a metadata control is present in the tree.
	 */
	private boolean metadataControl;

	/**
	 * A reference to the current control repository.
	 */
	private ControlRepository repository;

	/**
	 * Constructs a new WizardModel.
	 * 
	 * @param configuration the admini tool configuration object.
	 */
	public WizardModel(ControlRepository repository)
	{
		this.repository = repository;
		listenerList = new EventListenerList();
		clearWizard();
	}

	/**
	 * Deletes the current wizard tree and creates a new root for the model.
	 */
	public void clearWizard()
	{
		root = null;
	}

	/**
	 * Loads a wizard from the given XML.
	 * 
	 * @param xml the XML wizard.
	 */
	public void loadWizard(Object wizard)
	{
		String id = repository.getIdForWizardObject(wizard);
		root = (Control) repository.getModelForControl(id);
		root.setWrappedObject(wizard);
		createChildren(root);
	}

	private void createChildren(Control parent)
	{
		List<?> childObjects = parent.getChildObjects();
		if( childObjects != null )
		{
			for( Object object : childObjects )
			{
				String id = repository.getIdForWizardObject(object);
				Control child = (Control) repository.getModelForControl(id);
				child.setWrappedObject(object);
				appendTo(child, parent);
				if( WizardHelper.isMetadata(child) )
				{
					metadataControl = true;
				}
				createChildren(child);
			}
		}
	}

	/**
	 * Constructs a new control for the given control definition.
	 */
	public Control constructControl(ControlDefinition definition)
	{
		return (Control) repository.getModelForControl(definition.getId());
	}

	/**
	 * Invoke this to delete a control and all of it's subtree.
	 */
	public void removeControl(Control control)
	{
		Control parent = control.getParent();
		if( parent != null )
		{
			// Remove the control from the tree.
			removeFromParent(control);

			if( WizardHelper.isMetadata(control) )
			{
				metadataControl = false;
			}
		}
	}

	/**
	 * Invoke this to add a control of the given definitions type to the parent.
	 * 
	 * @param parent the parent control.
	 * @param childDefinition the control definition of the child to create.
	 * @return the newly created control.
	 */
	public Control addControl(Control parent, ControlDefinition childDefinition)
	{
		// Create the child and add it to the tree.
		Control child = constructControl(childDefinition);
		Object wrapped = repository.getNewWrappedObject(childDefinition.getId());
		child.setWrappedObject(wrapped);
		appendTo(child, parent);

		if( WizardHelper.isMetadata(child) )
		{
			metadataControl = true;
		}

		return child;
	}

	/**
	 * Raises a nodes position up by one. If the control is the first control on
	 * a page, then it will move to the last element of the previous page, if
	 * the previous page exists.
	 * 
	 * @param control the control to raise.
	 */
	public void raiseControl(Control control)
	{
		if( control.equals(root) )
		{
			return;
		}

		Control parent = control.getParent();
		Control sibling = control.getPreviousSibling();

		if( sibling == null )
		{
			Control previousPage = parent.getPreviousSibling();
			if( previousPage != null )
			{
				appendTo(control, previousPage);
			}
		}
		else
		{
			int index = getIndexOfChild(parent, sibling);
			insertInto(control, parent, index);
		}
	}

	/**
	 * Lowers a nodes position up by one. If the control is the last control on
	 * a page, then it will move to the first element of the following page, if
	 * the following page exists.
	 * 
	 * @param control the control to lower.
	 */
	public void lowerControl(Control control)
	{
		if( control.equals(root) )
		{
			return;
		}

		Control parent = control.getParent();
		Control sibling = control.getNextSibling();

		if( sibling == null )
		{
			Control followingPage = parent.getNextSibling();
			if( followingPage != null )
			{
				insertInto(control, followingPage, 0);
			}
		}
		else
		{
			int index = getIndexOfChild(parent, sibling);
			insertInto(control, parent, index);
		}
	}

	/**
	 * Appends the child as the last child of the parent.
	 */
	public void appendTo(Control child, Control parent)
	{
		insertInto(child, parent, parent.getChildren().size());
	}

	/**
	 * Inserts a child to the parent at the given index.
	 */
	public void insertInto(Control child, Control parent, int index)
	{
		// Make sure it's not part of the tree already.
		removeFromParent(child);

		// Link it in the tree
		child.setParent(parent);
		parent.getChildren().add(index, child);

		// Fire the event
		fireTreeNodeInserted(parent, index, child);
	}

	/**
	 * Removes a child from it's parent.
	 */
	private void removeFromParent(Control child)
	{
		Control parent = child.getParent();
		if( parent != null )
		{
			child.setParent(null);
			int index = getIndexOfChild(parent, child);
			parent.getChildren().remove(index);
			fireTreeNodeRemoved(parent, index, child);
		}
	}

	/**
	 * Gets the root of the wizard tree.
	 */
	public Control getRootControl()
	{
		return root;
	}

	/**
	 * @return Returns the metadataControl.
	 */
	public boolean hasMetadataControl()
	{
		return metadataControl;
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
	 * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
	 */
	@Override
	public boolean isLeaf(Object node)
	{
		Control control = (Control) node;
		return control.getChildren().isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
	 */
	@Override
	public Object getChild(Object parent, int index)
	{
		Control control = (Control) parent;
		return control.getChildren().get(index);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
	 */
	@Override
	public int getChildCount(Object parent)
	{
		Control control = (Control) parent;
		return control.getChildren().size();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object,
	 * java.lang.Object)
	 */
	@Override
	public int getIndexOfChild(Object parent, Object child)
	{
		Control control = (Control) parent;
		return control.getChildren().indexOf(child);
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
	 * @see
	 * javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath,
	 * java.lang.Object)
	 */
	@Override
	public void valueForPathChanged(TreePath path, Object newValue)
	{
		Control control = (Control) path.getLastPathComponent();
		controlChanged(control);
	}

	/**
	 * Invoke this method after you've changed/saved a control.
	 */
	public void controlChanged(Control control)
	{
		if( control != null )
		{
			if( control.equals(root) )
			{
				fireTreeNodesChanged(getTreePath(control), null, null);
			}
			else
			{
				Control parent = control.getParent();
				int index = getIndexOfChild(parent, control);
				fireTreeNodesChanged(getTreePath(parent), new int[]{index}, new Object[]{control});
			}
		}
	}

	/**
	 * Builds the parents of node up to and including the root node, where the
	 * original node is the last element in the returned array. The length of
	 * the returned array gives the node's depth in the tree.
	 * 
	 * @param control the Control to get the path for
	 */
	public TreePath getTreePath(Control control)
	{
		if( control != null )
		{
			Collection<Control> path = buildUpPathToRoot(control);
			return new TreePath(path.toArray());
		}
		else
		{
			return null;
		}
	}

	/**
	 * Builds the parents of node up to and including the root node, where the
	 * original node is the last element in the returned collection. The length
	 * of the returned collection gives the node's depth in the tree.
	 * 
	 * @param control the Control to get the path for
	 */
	private Collection<Control> buildUpPathToRoot(Control control)
	{
		if( control == null )
		{
			return null;
		}
		else
		{
			Collection<Control> c = buildUpPathToRoot(control.getParent());
			if( c == null )
			{
				c = new ArrayList<Control>();
			}
			c.add(control);
			return c;
		}
	}

	/**
	 * Notifies all listeners that have registered interest for notification on
	 * this event type. The event instance is lazily created using the
	 * parameters passed into the fire method.
	 * 
	 * @param source the node being changed
	 * @param path the path to the root node
	 * @param childIndices the indices of the changed elements
	 * @param children the changed elements
	 * @see EventListenerList
	 */
	private void fireTreeNodesChanged(TreePath path, int[] childIndices, Object[] children)
	{
		TreeModelListener[] listeners = listenerList.getListeners(TreeModelListener.class);
		if( listeners != null && listeners.length > 0 )
		{
			TreeModelEvent e = new TreeModelEvent(this, path, childIndices, children);
			for( TreeModelListener listener : listeners )
			{
				listener.treeNodesChanged(e);
			}
		}
	}

	/**
	 * Notifies all listeners that have registered interest for notification on
	 * this event type. The event instance is lazily created using the
	 * parameters passed into the fire method.
	 * 
	 * @param parent the parent of the child
	 * @param index the index of the inserted element
	 * @param child the inserted element
	 * @see EventListenerList
	 */
	private void fireTreeNodeInserted(Control parent, int index, Control child)
	{
		fireTreeNodesInserted(getTreePath(parent), new int[]{index}, new Object[]{child});
	}

	/**
	 * Notifies all listeners that have registered interest for notification on
	 * this event type. The event instance is lazily created using the
	 * parameters passed into the fire method.
	 * 
	 * @param source the node being changed
	 * @param path the path to the root node
	 * @param childIndices the indices of the changed elements
	 * @param children the changed elements
	 * @see EventListenerList
	 */
	private void fireTreeNodesInserted(TreePath path, int[] childIndices, Object[] children)
	{
		TreeModelListener[] listeners = listenerList.getListeners(TreeModelListener.class);
		if( listeners != null && listeners.length > 0 )
		{
			TreeModelEvent e = new TreeModelEvent(this, path, childIndices, children);
			for( TreeModelListener listener : listeners )
			{
				listener.treeNodesInserted(e);
			}
		}
	}

	/**
	 * Notifies all listeners that have registered interest for notification on
	 * this event type. The event instance is lazily created using the
	 * parameters passed into the fire method.
	 * 
	 * @param parent the parent of the child
	 * @param index the index of the changed element
	 * @param child the changed element
	 * @see EventListenerList
	 */
	private void fireTreeNodeRemoved(Control parent, int index, Control child)
	{
		fireTreeNodesRemoved(getTreePath(parent), new int[]{index}, new Object[]{child});
	}

	/**
	 * Notifies all listeners that have registered interest for notification on
	 * this event type. The event instance is lazily created using the
	 * parameters passed into the fire method.
	 * 
	 * @param path the path to the root node
	 * @param childIndices the indices of the changed elements
	 * @param children the changed elements
	 * @see EventListenerList
	 */
	private void fireTreeNodesRemoved(TreePath path, int[] childIndices, Object[] children)
	{
		TreeModelListener[] listeners = listenerList.getListeners(TreeModelListener.class);
		if( listeners != null && listeners.length > 0 )
		{
			TreeModelEvent e = new TreeModelEvent(this, path, childIndices, children);
			for( TreeModelListener listener : listeners )
			{
				listener.treeNodesRemoved(e);
			}
		}
	}
}
