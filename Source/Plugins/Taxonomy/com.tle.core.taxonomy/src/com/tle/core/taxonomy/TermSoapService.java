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

package com.tle.core.taxonomy;

/**
 * A SOAP interface for editing locally stored taxonomy terms.
 */
public interface TermSoapService
{
	/**
	 * Before terms can be edited, this method must be invoked to aquire an
	 * editing lock on the taxonomy. An error will be raised if the lock cannot
	 * be acquired, most likely because it has already been locked by a
	 * different user.
	 * 
	 * @param taxonomyUuid The UUID of the taxonomy to you want to edit.
	 */
	void lockTaxonomyForEditing(String taxonomyUuid);

	/**
	 * Unlock a taxonomy that you previously acquired a lock on. You can also
	 * choose to forcefully unlock the taxonomy, which will remove any existing
	 * lock, even if it has been made by another user. It is recommended that
	 * the force option is used with care.
	 * 
	 * @param taxonomyUuid The UUID of the taxonomy that you have finished
	 *            editing.
	 * @param force true if the taxonomy should be unlocked, even if your
	 *            session is not the lock owner. It is recommended that you
	 *            should you used false in nearly all case.
	 */
	void unlockTaxonomy(String taxonomyUuid, boolean force);

	/**
	 * List the child terms for a parent term. This will only list the immediate
	 * children of the parent, ie, not grand-children, great grand-children,
	 * etc...
	 * 
	 * @param taxonomyUuid The UUID of the taxonomy.
	 * @param parentFullPath The full path of the parent term the new term will
	 *            be added to, eg, Mammalia\Felidae\Panthera. Passing in an
	 *            empty string will list the root terms.
	 * @return An array of immediate child terms, eg, [Tiger, Lion, Jaguar,
	 *         Leopard]
	 */
	String[] listTerms(String taxonomyUuid, String parentFullPath);

	/**
	 * Insert a new term into the taxonomy.
	 * <p>
	 * This method requires an editing lock to have been acquired for the
	 * taxonomy.
	 * 
	 * @param taxonomyUuid The UUID of the taxonomy.
	 * @param parentFullPath The full path of the parent term the new term will
	 *            be added to, eg, Mammalia\Felidae\Panthera.
	 * @param termValue The term to be added, eg, Tiger.
	 * @param index The index the term should be inserted into in relation to
	 *            its siblings. Zero inserts it as the first sibling, one as the
	 *            second sibling, and so on. If the index is less than zero or
	 *            greater than the number of siblings minus one, then the term
	 *            will be added as the last sibling.
	 */
	void insertTerm(String taxonomyUuid, String parentFullPath, String term, int index);

	/**
	 * Moves a term to a (possibly) new parent term and child index. Children of
	 * the term are also moved. The user should always remember that changing
	 * the lineage of a term will also change the lineage of child terms, and
	 * depending on the number of terms that require modification, could be an
	 * expensive operation. Leaving the term under the same parent term, but
	 * changing the index, does <b>not</b> change the lineage of the term or its
	 * children.
	 * <p>
	 * This method requires an editing lock to have been acquired for the
	 * taxonomy.
	 * 
	 * @param taxonomyUuid The UUID of the taxonomy.
	 * @param termToMove The full path of the term to move, eg,
	 *            Aves\Falconiformes\Accipitridae\Flying Fish
	 * @param moveToParent The full path of the parent term it should be moved
	 *            to, eg, Actinopterygii\Exocoetidae. Null or an empty string
	 *            will move the term to the root of the taxonomy.
	 * @param index The index the term should be placed in relation to its
	 *            siblings. Zero inserts it as the first sibling, one as the
	 *            second sibling, and so on. If the index is less than zero or
	 *            greater than the number of siblings minus one, then the term
	 *            will be added as the last sibling.
	 */
	void move(String taxonomyUuid, String termToMove, String newParent, int index);

	/**
	 * Renames a term. The user should always remember that renaming a term will
	 * change the lineage of child terms, and depending on the number of terms
	 * that require modification, could be an expensive operation.
	 * <p>
	 * This method requires an editing lock to have been acquired for the
	 * taxonomy.
	 * 
	 * @param taxonomyUuid The UUID of the taxonomy.
	 * @param termToRename the full path of the term to rename, eg,
	 *            Aves\Falconiformes\Accipitridae\Tigger
	 * @param newValue the new value that the term should become, eg, Tiger
	 */
	void renameTermValue(String taxonomyUuid, String termToRename, String newValue);

	/**
	 * Delete a term from the taxonomy. Child terms will also be deleted.
	 * <p>
	 * This method requires an editing lock to have been acquired for the
	 * taxonomy.
	 * 
	 * @param taxonomyUuid The UUID of the taxonomy.
	 * @param termFullPath The full path of the term to delete, eg,
	 *            Mammalia\Felidae\Panthera\Tiger.
	 */
	void deleteTerm(String taxonomyUuid, String termFullPath);

	/**
	 * Retrieve an stored data value for a key against a term.
	 * 
	 * @param taxonomyUuid The UUID of the taxonomy.
	 * @param termFullPath The full path of the term to retrieve the data for,
	 *            eg, Mammalia\Felidae\Panthera\Tiger.
	 * @param dataKey The key for the data to be retrieved.
	 * @return The data stored for that key and term.
	 */
	String getData(String taxonomyUuid, String termFullPath, String dataKey);

	/**
	 * Set an arbitrary data value for a key against a term.
	 * <p>
	 * This method requires an editing lock to have been acquired for the
	 * taxonomy.
	 * 
	 * @param taxonomyUuid The UUID of the taxonomy.
	 * @param termFullPath The full path of the term to store the data for, eg,
	 *            Mammalia\Felidae\Panthera\Tiger
	 * @param dataKey The key that the data should be stored for.
	 * @param dataValue The arbitrary data to store.
	 */
	void setData(String taxonomyUuid, String termFullPath, String dataKey, String dataValue);
}
