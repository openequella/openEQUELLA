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

package com.tle.common.taxonomy.terms;

import com.tle.common.taxonomy.Taxonomy;
import java.util.List;
import java.util.Map;

public interface RemoteTermService {
  /**
   * List the child terms for a parent term. This will only list the immediate children of the
   * parent, ie, not grand-children, great grand-children, etc...
   *
   * @param taxonomy The taxonomy the terms are being edited for.
   * @param parentFullPath The full path of the parent term the new term will be added to, eg,
   *     Mammalia/Felidae/Panthera. Passing in an empty string will list the root terms.
   * @return The list the immediate child terms, eg, [Tiger, Lion, Jaguar, Leopard]
   */
  List<String> listTerms(Taxonomy taxonomy, String parentFullPath);

  /**
   * Insert a new term into the taxonomy.
   *
   * <p>This method requires an editing lock to have been aquired for the taxonomy.
   *
   * @param taxonomy The taxonomy the terms are being edited for.
   * @param parentUuid The UUID of the parent term the new term will be added to
   * @param termValue The term to be added, eg, Tiger.
   * @param index The index the term should be inserted into in relation to its siblings. Zero
   *     inserts it as the first sibling, one as the second sibling, and so on. If the index is less
   *     than zero or greater than the number of siblings minus one, then the term will be added as
   *     the last sibling.
   * @return The full path of the new term (may have been slightly adjusted/fixed)
   */
  String insertTerm(Taxonomy taxonomy, String parentUuid, String termValue, int index);

  /**
   * Moves a term to a (possibly) new parent term and child index. Children of the term are also
   * moved. The user should always remember that changing the lineage of a term will also change the
   * lineage of child terms, and depending on the number of terms that require modification, could
   * be an expensive operation. Leaving the term under the same parent term, but changing the index,
   * does <b>not</b> change the lineage of the term or its children.
   *
   * <p>This method requires an editing lock to have been aquired for the taxonomy.
   *
   * @param taxonomy The taxonomy the terms are being edited for.
   * @param termToMove The full path of the term to move, eg, Aves/Falconiformes/Accipitridae/Flying
   *     Fish
   * @param moveToParent The full path of the parent term it should be moved to, eg,
   *     Actinopterygii/Exocoetidae. Null or an empty string will move the term to the root of the
   *     taxonomy.
   * @param index The index the term should be placed in relation to its siblings. Zero inserts it
   *     as the first sibling, one as the second sibling, and so on. If the index is less than zero
   *     or greater than the number of siblings minus one, then the term will be added as the last
   *     sibling.
   */
  void move(Taxonomy taxonomy, String termToMove, String moveToParent, int index);
  /**
   * Renames a term. The user should always remember that renaming a term will change the lineage of
   * child terms, and depending on the number of terms that require modification, could be an
   * expensive operation.
   *
   * <p>This method requires an editing lock to have been aquired for the taxonomy.
   *
   * @param taxonomy The taxonomy the terms are being edited for.
   * @param termToRename the full path of the term to rename, eg,
   *     Aves/Falconiformes/Accipitridae/Tigger
   * @param newValue the new value that the term should become, eg, Tiger
   */
  void renameTermValue(Taxonomy taxonomy, String termToRename, String newValue);

  void updateTerm(
      Taxonomy taxonomy, String termUuid, String moveToParent, int index, String newValue);

  /**
   * Delete a term from the taxonomy. Child terms will also be deleted.
   *
   * <p>This method requires an editing lock to have been aquired for the taxonomy.
   *
   * @param taxonomy The taxonomy the terms are being edited for.
   * @param fullTermPath The full path of the term to delete, eg, Mammalia/Felidae/Panthera/Tiger.
   */
  void deleteTerm(Taxonomy taxonomy, String fullTermPath);

  /**
   * Retrieve an stored data value for a key against a term.
   *
   * @param taxonomy The taxonomy the terms are being edited for.
   * @param fullTermPath The full path of the term to retrieve the data for, eg,
   *     Mammalia/Felidae/Panthera/Tiger.
   * @param dataKey The key for the data to be retrieved.
   * @return The data stored for that key and term.
   */
  String getData(Taxonomy taxonomy, String fullTermPath, String dataKey);

  /**
   * Set an arbitrary data value for a key against a term.
   *
   * <p>This method requires an editing lock to have been aquired for the taxonomy.
   *
   * @param taxonomy The taxonomy the terms are being edited for.
   * @param fullTermPath The full path of the term to store the data for, eg,
   *     Mammalia/Felidae/Panthera/Tiger
   * @param dataKey The key that the data should be stored for.
   * @param dataValue The arbitrary data to store.
   */
  void setData(Taxonomy taxonomy, String fullTermPath, String dataKey, String dataValue);

  Map<String, String> getAllData(Taxonomy taxonomy, String fullTermPath);

  void setAllData(Taxonomy taxonomy, String fullTermPath, Map<String, String> data);

  /**
   * Get a data value for a key against a term
   *
   * @param taxonomy
   * @param termUuid
   * @param dataKey
   * @return String
   */
  String getDataByTermUuid(Taxonomy taxonomy, String termUuid, String dataKey);

  /**
   * Set an arbitrary data for a key against a term This method requires an editing lock to have
   * been aquired for the taxonomy.
   *
   * @param taxonomy
   * @param termUuid
   * @param dataKey
   * @param dataValue
   */
  void setDataByTermUuid(Taxonomy taxonomy, String termUuid, String dataKey, String dataValue);

  /**
   * Get all data against a term
   *
   * @param taxonomy
   * @param termUuid
   * @return
   */
  Map<String, String> getAllDataByTermUuid(Taxonomy taxonomy, String termUuid);

  /**
   * Set all data against a term
   *
   * @param taxonomy
   * @param termUuid
   * @param data
   */
  void setAllDataByTermUuid(Taxonomy taxonomy, String termUuid, Map<String, String> data);

  /**
   * Alphabetically sort the immediate children of the given term.
   *
   * @param taxonomy The taxonomy containng the terms to sort
   * @param fullTermPath The term to sort the children of
   */
  void sortChildren(Taxonomy taxonomy, String fullTermPath);
}
