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

package com.tle.core.metadata.scripting.types;

import com.tle.common.scripting.types.MapScriptType;
import java.util.List;

public interface MetadataScriptType {
  /**
   * Returns the first key found in any type grouping that matches. If there are duplicate keys
   * within groupings then {@link #get(String, String)} should be used
   *
   * @param key The identifier who's value should be retrieved.
   * @return The first value found for the key provided or null if key is not present
   */
  String get(String key);

  /**
   * Returns a specific key within a type grouping e.g LensID for XMP would be get("XMP", "LensID").
   * This method should be used to get specific values when there are duplicate keys in the metadata
   *
   * @param type The identifier of a type grouping
   * @param key The ID of a specific key in a group to get the value of
   * @return A specific value for a type or null if either type or key is not present
   */
  String get(String type, String key);

  /**
   * Returns a list of all type groupings e.g EXIF, XMP, File etc
   *
   * @return A list of available type groupings
   */
  List<String> getTypesAvailable();

  /**
   * Returns all key values for a specific type e.g getAllForType("XMP") will return all the
   * key/values for the type grouping XMP. If the type does not exist null will be returned
   *
   * @param type The type to retrieve all key values for
   * @return All key/values for a specific type grouping
   */
  MapScriptType getAllForType(String type);

  /**
   * Returns true if there is no metadata available
   *
   * @return true if there is no metadata available
   */
  boolean isEmpty();
}
