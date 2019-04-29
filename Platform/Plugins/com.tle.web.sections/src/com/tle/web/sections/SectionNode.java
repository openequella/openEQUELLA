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

package com.tle.web.sections;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@code SectionNode} is a tree node useful for inserting into a {@link SectionTree}.
 *
 * <p>It contains a {@link Section}, an optional preferred id, an optional place holder id, and a
 * list of children (which can either be {@link Section}s or other {@code SectionNode}s.
 *
 * @author jmaginnis
 */
public class SectionNode {
  private List<Object> children;
  private List<Object> innerChildren;
  private Section section;
  private String id;
  private String placeHolderId;

  public SectionNode() {
    // for spring
  }

  public SectionNode(String rootId) {
    this.id = rootId;
  }

  public SectionNode(String id, Section section) {
    this.id = id;
    this.section = section;
  }

  /**
   * Gets the optional place holder id.
   *
   * @return The place holder id
   * @see SectionTree#getPlaceHolder(String)
   */
  public String getPlaceHolderId() {
    return placeHolderId;
  }

  public void setPlaceHolderId(String placeHolderId) {
    this.placeHolderId = placeHolderId;
  }

  /**
   * Return the list of child {@code Section}s.
   *
   * @return A list of children (which can either be {@code Section}s or other {@code SectionNode}s.
   */
  public List<Object> getChildren() {
    return children;
  }

  /**
   * Grr, must be <? extends Object> otherwise Spring tries to autowire it
   *
   * @param children
   */
  public void setChildren(List<? extends Object> children) {
    if (children != null) {
      this.children = new ArrayList<Object>(children);
    }
  }

  public Section getSection() {
    return section;
  }

  public void setSection(Section section) {
    this.section = section;
  }

  /**
   * Get the preferred id of the <code>Section</code>.
   *
   * @return The preferred id to register the {@code Section} with. If set to <code>null</code> use
   *     the {@link Section#getDefaultPropertyName()}
   * @see Section#getDefaultPropertyName()
   */
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public List<Object> getInnerChildren() {
    return innerChildren;
  }

  /**
   * Grr, must be <? extends Object> otherwise Spring tries to autowire it
   *
   * @param innerChildren
   */
  public void setInnerChildren(List<? extends Object> innerChildren) {
    if (innerChildren != null) {
      this.innerChildren = new ArrayList<Object>(innerChildren);
    }
  }
}
