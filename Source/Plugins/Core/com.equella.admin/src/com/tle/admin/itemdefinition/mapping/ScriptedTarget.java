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

package com.tle.admin.itemdefinition.mapping;

import com.tle.admin.schema.SchemaNode;
import java.util.ArrayList;
import java.util.List;

class ScriptedTarget {
  private SchemaNode target;
  private String cachedPath;
  private List<ScriptedRule> rules;

  public ScriptedTarget() {
    super();
    rules = new ArrayList<ScriptedRule>();
  }

  public void destroy() {
    if (target != null) {
      target.unlock();
    }
  }

  /**
   * @return Returns the rules.
   */
  public List<ScriptedRule> getRules() {
    return rules;
  }

  /**
   * @param rules The rules to set.
   */
  public void setRules(List<ScriptedRule> rules) {
    this.rules = rules;
  }

  /**
   * @return Returns the target.
   */
  public SchemaNode getTarget() {
    return target;
  }

  /**
   * @param target The target to set.
   */
  public void setTarget(SchemaNode newTarget) {
    if (target != null) {
      target.unlock();
    }
    target = newTarget;
    target.lock();
    cachedPath = target.toString();
  }

  @Override
  public String toString() {
    return cachedPath;
  }

  @Override
  public int hashCode() {
    if (target != null) {
      return cachedPath.hashCode();
    } else {
      return super.hashCode();
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ScriptedTarget) {
      ScriptedTarget rhs = (ScriptedTarget) obj;
      return rhs.cachedPath.equals(cachedPath);
    } else {
      return false;
    }
  }
}
