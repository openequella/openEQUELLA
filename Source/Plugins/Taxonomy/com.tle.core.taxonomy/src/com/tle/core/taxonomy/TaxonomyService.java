package com.tle.core.taxonomy;

import java.util.List;
import java.util.Map;

import com.dytech.edge.exceptions.IllegalOperationException;
import com.dytech.edge.exceptions.InvalidDataException;
import com.tle.common.Pair;
import com.tle.common.taxonomy.RemoteTaxonomyService;
import com.tle.common.taxonomy.SelectionRestriction;
import com.tle.common.taxonomy.Taxonomy;
import com.tle.core.services.entity.AbstractEntityService;
import com.tle.core.services.entity.EntityEditingBean;

public interface TaxonomyService extends AbstractEntityService<EntityEditingBean, Taxonomy>, RemoteTaxonomyService
{
	/**
	 * Get term by full term path
	 * 
	 * @param taxonomyUuid
	 * @param fullTermPath
	 * @return
	 */
	TermResult getTerm(String taxonomyUuid, String fullTermPath);
	/**
	 * @param parentFullTermPath use null or empty string to retrieve root
	 *            terms.
	 */
	List<TermResult> getChildTerms(String taxonomyUuid, String parentFullTermPath);

	/**
	 * @return A pair containing the number of available results (if the search
	 *         were to be unlimited) and the list of TermResults
	 */
	Pair<Long, List<TermResult>> searchTerms(String taxonomyUuid, String query, SelectionRestriction restriction,
		int limit, boolean searchFullTerm);

	/**
	 * Retrieve custom data from a taxonomy node.
	 * 
	 * @param taxonomyUuid
	 * @param fullTermPath
	 * @param key
	 * @return
	 */
	String getDataForTerm(String taxonomyUuid, String fullTermPath, String key);

	/**
	 * @param taxonomyUuid
	 * @param parentFullTermPath
	 * @param termValue
	 * @param createHierarchy Create as many terms as required to ensure each
	 *            part of parentFullTermPath exists
	 * @throws IllegalOperationException If the data source does not support
	 *             term addition
	 */
	TermResult addTerm(String taxonomyUuid, String parentFullTermPath, String termValue, boolean createHierarchy);

	/**
	 * @param taxonomyUuid
	 * @param parentFullTermPath
	 * @param termValue
	 * @throws InvalidDataException
	 */
	void validateTerm(String taxonomyUuid, String parentFullTermPath, String termValue, boolean requireParent)
		throws InvalidDataException;

	/**
	 * Is taxonomy readonly
	 * 
	 * @param taxonomyUuid
	 * @return boolean
	 */
	boolean isTaxonomyReadonly(String taxonomyUuid);

	/**
	 * Get TermResult by term uuid
	 * 
	 * @param taxonomy
	 * @param termUuid
	 * @return
	 */
	TermResult getTermResultByUuid(String taxonomyUuid, String termUuid);

	/**
	 * Get a data value for a key against a term
	 * 
	 * @param taxonomy
	 * @param termUuid
	 * @param dataKey
	 * @return String
	 */
	String getDataByTermUuid(String taxonomyUuid, String termUuid, String dataKey);

	/**
	 * Get all data for a term
	 * 
	 * @param taxonomyUuid
	 * @param termUuid
	 * @return
	 */
	Map<String, String> getAllDataByTermUuid(String taxonomyUuid, String termUuid);
}
