package com.tle.core.taxonomy.scripting.types;

import java.io.Serializable;
import java.util.List;

/**
 * Retrieve one of these objects with the TaxonomyServiceScriptObject ('data')
 * E.g. var myTaxonomy = data.getTaxonomyByName('myTaxonomy')
 * 
 * @author aholland
 */
public interface TaxonomyScriptType extends Serializable
{
	/**
	 * @param fullTermPath The full path of the term to find.
	 * @return The term at fullTermPath or null if no term found.
	 */
	TermScriptType getTerm(String fullTermPath);

	/**
	 * @param term The term to find the children of.
	 * @return Gets all immediate child terms of the given term. (That is: not
	 *         grandchild terms)
	 */
	List<TermScriptType> getChildTerms(TermScriptType term);

	/**
	 * @param term The term to find the parent of.
	 * @return Gets the parent of the given term.
	 */
	TermScriptType getParentTerm(TermScriptType term);

	/**
	 * Search for the full term paths in this taxonomy matching the partial
	 * query
	 * 
	 * @param query The query to match terms against. You may use a wildcard
	 *            character '*', but there will always be an implied wildcard at
	 *            the end of the query.
	 * @return A list of full term paths matching the query.
	 */
	List<TermScriptType> searchTerms(String query);

	/**
	 * Add a new term to a taxonomy. If the taxonomy does not support term
	 * addition an exception will be thrown. Use supportsTermAddition to check
	 * this first.
	 * 
	 * @param parentFullPath The full path of the parent node. Depending on the
	 *            taxonomy data source implementation an exception may be thrown
	 *            if the parent node cannot be found.
	 * @param termValue The node name of the new term.
	 */
	TermScriptType insertTerm(String parentFullPath, String termValue);

	/**
	 * @return true if this taxonomy supports the addition of new terms. This is
	 *         dependent on the data source of the taxonomy.
	 */
	boolean supportsTermAddition();

	/**
	 * @return true if this taxonomy supports term searching. This is dependent
	 *         on the data source of the taxonomy (although this will almost
	 *         always return true).
	 */
	boolean supportsTermSearching();

	/**
	 * @return true if this taxonomy supports term browsing. This is dependent
	 *         on the data source of the taxonomy.
	 */
	boolean supportsTermBrowsing();
}
