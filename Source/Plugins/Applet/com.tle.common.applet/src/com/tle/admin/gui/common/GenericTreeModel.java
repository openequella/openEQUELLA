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

package com.tle.admin.gui.common;

import java.io.Serializable;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

public class GenericTreeModel<T extends DefaultMutableTreeNode> implements Serializable {
  private static final long serialVersionUID = 1L;
  private final DefaultTreeModel model;
  private UpDownPolicyImplementor<T> upDownImplementor;

  public GenericTreeModel(T root) {
    model = new DefaultTreeModel(root);
  }

  public GenericTreeModel(T root, boolean asksAllowsChildren) {
    model = new DefaultTreeModel(root, asksAllowsChildren);
  }

  public DefaultTreeModel getUnderlyingTreeModel() {
    return model;
  }

  protected UpDownPolicyImplementor<T> getUpDownPolicyImplementor() {
    if (upDownImplementor == null) {
      upDownImplementor = getDefaultUpDownPolicyImplementor();
    }
    return upDownImplementor;
  }

  protected UpDownPolicyImplementor<T> getDefaultUpDownPolicyImplementor() {
    return new UpDownWithInParentPolicy<T>();
  }

  public void setUpDownPolicyImplementor(UpDownPolicyImplementor<T> upDownImplementor) {
    this.upDownImplementor = upDownImplementor;
  }

  @SuppressWarnings("unchecked")
  public T getRoot() {
    return (T) model.getRoot();
  }

  public void setRoot(T root) {
    model.setRoot(root);
  }

  @SuppressWarnings("unchecked")
  public T getChild(T parent, int index) {
    return (T) model.getChild(parent, index);
  }

  public int getChildCount(T parent) {
    return model.getChildCount(parent);
  }

  public boolean isLeaf(T node) {
    return model.isLeaf(node);
  }

  public void nodeChanged(T node) {
    model.nodeChanged(node);
  }

  public void nodeStructureChanged(T node) {
    model.nodeStructureChanged(node);
  }

  public int getIndexOfChild(T parent, T child) {
    return model.getIndexOfChild(parent, child);
  }

  public void addTreeModelListener(TreeModelListener l) {
    model.addTreeModelListener(l);
  }

  public void removeTreeModelListener(TreeModelListener l) {
    model.removeTreeModelListener(l);
  }

  public void add(T node, T parent) {
    insert(node, parent, parent.getChildCount());
  }

  public void insert(T node, T parent, int index) {
    model.insertNodeInto(node, parent, index);
  }

  public void remove(T node) {
    model.removeNodeFromParent(node);
  }

  public boolean canMoveUp(T node) {
    if (node == null) {
      return false;
    }
    return getUpDownPolicyImplementor().canMoveUp(this, node);
  }

  public boolean canMoveDown(T node) {
    if (node == null) {
      return false;
    }
    return getUpDownPolicyImplementor().canMoveDown(this, node);
  }

  public void moveUp(T node) {
    getUpDownPolicyImplementor().moveUp(this, node);
  }

  public void moveDown(T node) {
    getUpDownPolicyImplementor().moveDown(this, node);
  }

  /**
   * Defines a policy to decide whether a node can be moved up or down.
   *
   * @author Nicholas Read
   */
  public interface UpDownPolicy<T extends DefaultMutableTreeNode> {
    boolean canMoveUp(GenericTreeModel<T> model, T node);

    boolean canMoveDown(GenericTreeModel<T> model, T node);
  }

  /**
   * Defines a policy implementaion to also move the node.
   *
   * @author Nicholas Read
   */
  public interface UpDownPolicyImplementor<T extends DefaultMutableTreeNode>
      extends UpDownPolicy<T>, Serializable {
    void moveUp(GenericTreeModel<T> model, T node);

    void moveDown(GenericTreeModel<T> model, T node);
  }

  /**
   * Implements a policy where children can only move up and down in their current siblings,
   * restricted to the one parent.
   *
   * @author Nicholas Read
   */
  public static class UpDownWithInParentPolicy<T extends DefaultMutableTreeNode>
      implements UpDownPolicyImplementor<T> {
    private static final long serialVersionUID = 1L;

    /*
     * (non-Javadoc)
     * @see
     * com.tle.admin.workflow.tree.AbstractTreeModel.UpDownPolicy#canMoveUp
     * (com.tle.admin.workflow.tree.AbstractTreeModel, null)
     */
    @Override
    public boolean canMoveUp(GenericTreeModel<T> model, T node) {
      return node.getPreviousSibling() != null;
    }

    /*
     * (non-Javadoc)
     * @see
     * com.tle.admin.workflow.tree.AbstractTreeModel.UpDownPolicy#canMoveDown
     * (com.tle.admin.workflow.tree.AbstractTreeModel, null)
     */
    @Override
    public boolean canMoveDown(GenericTreeModel<T> model, T node) {
      return node.getNextSibling() != null;
    }

    /*
     * (non-Javadoc)
     * @see
     * com.tle.admin.workflow.tree.AbstractTreeModel.UpDownImplementor#moveUp
     * (com.tle.admin.workflow.tree.AbstractTreeModel, null)
     */
    @Override
    public void moveUp(GenericTreeModel<T> model, T node) {
      MutableTreeNode parent = (MutableTreeNode) node.getParent();
      parent.insert(node, parent.getIndex(node) - 1);
    }

    /*
     * (non-Javadoc)
     * @see
     * com.tle.admin.workflow.tree.AbstractTreeModel.UpDownImplementor#moveDown
     * (com.tle.admin.workflow.tree.AbstractTreeModel, null)
     */
    @Override
    public void moveDown(GenericTreeModel<T> model, T node) {
      MutableTreeNode parent = (MutableTreeNode) node.getParent();
      parent.insert(node, parent.getIndex(node) - 1);
    }
  }

  /**
   * Implements a policy where children can move up and down through their siblings, and will become
   * the previous sibling of their parent if moving up and is the first child, or the next sibling
   * of their parent if moving down and is the last child.
   *
   * @author Nicholas Read
   */
  public static class UpDownDepthFirstPolicy<T extends DefaultMutableTreeNode>
      implements UpDownPolicyImplementor<T> {
    private static final long serialVersionUID = 1L;

    /*
     * (non-Javadoc)
     * @see
     * com.tle.admin.workflow.tree.AbstractTreeModel.UpDownPolicy#canMoveUp
     * (com.tle.admin.workflow.tree.AbstractTreeModel, null)
     */
    @Override
    public boolean canMoveUp(GenericTreeModel<T> model, T node) {
      TreeNode parent = node.getParent();
      if (parent == null) {
        return false;
      } else {
        int index = parent.getIndex(node);
        return index > 0 || parent.getParent() != null;
      }
    }

    /*
     * (non-Javadoc)
     * @see
     * com.tle.admin.workflow.tree.AbstractTreeModel.UpDownPolicy#canMoveDown
     * (com.tle.admin.workflow.tree.AbstractTreeModel, null)
     */
    @Override
    public boolean canMoveDown(GenericTreeModel<T> model, T node) {
      TreeNode parent = node.getParent();
      if (parent == null) {
        return false;
      } else {
        int index = parent.getIndex(node);
        return index < parent.getChildCount() - 1 || parent.getParent() != null;
      }
    }

    /*
     * (non-Javadoc)
     * @see
     * com.tle.admin.workflow.tree.AbstractTreeModel.UpDownImplementor#moveUp
     * (com.tle.admin.workflow.tree.AbstractTreeModel, null)
     */
    @Override
    @SuppressWarnings("unchecked")
    public void moveUp(GenericTreeModel<T> model, T node) {
      T parent = (T) node.getParent();
      int index = parent.getIndex(node);

      if (index == 0) {
        T grandParent = (T) parent.getParent();
        index = grandParent.getIndex(parent);
        parent = grandParent;
      } else {
        index--;

        T possibleParent = (T) parent.getChildAt(index);
        if (shouldDescendInto(model, node, possibleParent)) {
          parent = possibleParent;
          index = parent.getChildCount();
        }
      }

      model.remove(node);
      model.insert(node, parent, index);
    }

    /*
     * (non-Javadoc)
     * @see
     * com.tle.admin.workflow.tree.AbstractTreeModel.UpDownImplementor#moveDown
     * (com.tle.admin.workflow.tree.AbstractTreeModel, null)
     */
    @Override
    @SuppressWarnings("unchecked")
    public void moveDown(GenericTreeModel<T> model, T node) {
      T parent = (T) node.getParent();
      int index = parent.getIndex(node);

      if (node.getNextSibling() == null) {
        T grandParent = (T) parent.getParent();
        index = grandParent.getIndex(parent) + 1;
        parent = grandParent;
      } else {
        index++;

        T possibleParent = (T) parent.getChildAt(index);
        if (shouldDescendInto(model, node, possibleParent)) {
          parent = possibleParent;
          index = 0;
        }
      }
      model.remove(node);
      model.insert(node, parent, index);
    }

    protected boolean shouldDescendInto(GenericTreeModel<T> model, T node, T possibleParent) {
      if (model.getUnderlyingTreeModel().asksAllowsChildren()) {
        return possibleParent.getAllowsChildren();
      } else {
        return true;
      }
    }
  }

  /**
   * Implements a policy where nothing can be moved up or down. Sounds useless, but it can be handy.
   *
   * @author Nicholas Read
   */
  public static class DisabledUpDownPolicy<T extends DefaultMutableTreeNode>
      implements UpDownPolicyImplementor<T> {
    private static final long serialVersionUID = 1L;

    /*
     * (non-Javadoc)
     * @see
     * com.tle.admin.workflow.tree.AbstractTreeModel.UpDownPolicy#canMoveUp
     * (com.tle.admin.workflow.tree.AbstractTreeModel, null)
     */
    @Override
    public boolean canMoveUp(GenericTreeModel<T> model, T node) {
      return false;
    }

    /*
     * (non-Javadoc)
     * @see
     * com.tle.admin.workflow.tree.AbstractTreeModel.UpDownPolicy#canMoveDown
     * (com.tle.admin.workflow.tree.AbstractTreeModel, null)
     */
    @Override
    public boolean canMoveDown(GenericTreeModel<T> model, T node) {
      return false;
    }

    /*
     * (non-Javadoc)
     * @see
     * com.tle.admin.workflow.tree.AbstractTreeModel.UpDownImplementor#moveUp
     * (com.tle.admin.workflow.tree.AbstractTreeModel, null)
     */
    @Override
    public void moveUp(GenericTreeModel<T> model, T node) {
      throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * @see
     * com.tle.admin.workflow.tree.AbstractTreeModel.UpDownImplementor#moveDown
     * (com.tle.admin.workflow.tree.AbstractTreeModel, null)
     */
    @Override
    public void moveDown(GenericTreeModel<T> model, T node) {
      throw new UnsupportedOperationException();
    }
  }
}
