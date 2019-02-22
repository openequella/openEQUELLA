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

package com.tle.core.security;

import java.util.Set;

public interface SecurityTargetHandler {
  /**
   * Add all applicable labels for a target to the set of labels. For example, an item target would
   * add a label for itself, its status, its collection (or a transformer could deal with this),
   * etc... This is used when gathering all the applicable permissions that apply to a given target.
   *
   * @param labels a set of labels that should be added to.
   * @param target the target object to generate labels for.
   */
  void gatherAllLabels(Set<String> labels, Object target);

  /**
   * Add only the primary label for a target, ignoring any "parent" labels that may apply. For
   * example, an item target would return "I:{uuid}" only. This is used when getting/setting target
   * lists to the database.
   *
   * @param target the target object to generate the primary label for.
   * @return the primary label.
   */
  String getPrimaryLabel(Object target);

  /**
   * Transforms a security target into another type. For example, an ItemPack target can be
   * transformed into an Item object that would then get processed by other handlers. To try and
   * avoid circular transformation loops try to transform targets "up" the hierarchy, eg, Item to
   * Collection. This typically makes sense anyway as "parent" targets won't have the information
   * regarding which "child" target to transform to.
   *
   * @param target the target object to transform.
   * @return the transformed object.
   */
  Object transform(Object target);

  /**
   * @param target the object to determine ownership of.
   * @param userId the user identifier we're checking for ownership.
   * @return true if the target is owner by the userId.
   */
  boolean isOwner(Object target, String userId);
}
