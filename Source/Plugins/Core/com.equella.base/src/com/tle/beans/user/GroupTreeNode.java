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

package com.tle.beans.user;

import com.dytech.common.text.NumberStringComparator;
import com.tle.common.Check;
import com.tle.common.Check.FieldEquality;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

public class GroupTreeNode extends DefaultMutableTreeNode implements FieldEquality<GroupTreeNode> {
  private String id;
  private String name;

  public GroupTreeNode() {
    setAllowsChildren(true);
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return Check.nullToEmpty(name);
  }

  @Override
  public int hashCode() {
    return Check.getHashCode(id, name);
  }

  @Override
  public boolean equals(Object obj) {
    return Check.commonEquals(this, obj);
  }

  @Override
  public boolean checkFields(GroupTreeNode rhs) {
    return Objects.equals(id, rhs.id);
  }

  public void sortChildren() {
    if (children != null) {
      List<TreeNode> childNodes = Collections.list(children.elements());

      childNodes.stream()
          .map(child -> (GroupTreeNode) child)
          .sorted(SORTER)
          .collect(Collectors.toCollection(ArrayList::new))
          .forEach(GroupTreeNode::sortChildren);
    }
  }

  private static final NumberStringComparator<GroupTreeNode> SORTER =
      new NumberStringComparator<GroupTreeNode>() {
        private static final long serialVersionUID = 1L;

        @Override
        public String convertToString(GroupTreeNode t) {
          return t.getName();
        }
      };
}
